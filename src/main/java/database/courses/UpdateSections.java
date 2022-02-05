package database.courses;

import static scraping.query.QuerySection.querySectionAsync;
import static utils.TryCatch.*;

import database.models.SectionID;
import java.sql.*;
import java.util.*;
import org.slf4j.*;
import scraping.parse.ParseSection;
import types.*;
import utils.*;

/**
 * This class insert courses into the Postgresql database based on
 * the data scraped from Albert Mobile
 */
public class UpdateSections {
  private static Logger logger =
      LoggerFactory.getLogger("database.courses.UpdateSections");

  private static final class Prepared implements AutoCloseable {
    final PreparedStatement sections;
    final PreparedStatement descriptions;
    final PreparedStatement getInstructors;
    final PreparedStatement createInstructor;
    final PreparedStatement addTeachingRelation;

    Prepared(Connection conn) throws SQLException {
      this.sections = conn.prepareStatement(
          "UPDATE sections "
          + "SET name = ?, name_vec = to_tsvector(?), "
          + "campus = ?, "
          + "instruction_mode = ?, "
          + "min_units = ?, "
          + "max_units = ?, "
          + "location = ?, "
          + "grading = ?, "
          + "notes = ?, notes_vec = to_tsvector(?), "
          + "prerequisites = ?, prereqs_vec = to_tsvector(?) "
          + "WHERE sections.id = ?"
          + "RETURNING sections.course_id");

      this.descriptions = conn.prepareStatement(
          "UPDATE courses SET description = ? WHERE id = ?");

      this.getInstructors = conn.prepareStatement(
          "SELECT id from instructors WHERE subject_code = ? AND name = ?");

      this.createInstructor = conn.prepareStatement(
          "INSERT INTO instructors (name, subject_code) VALUES (?, ?)",
          Statement.RETURN_GENERATED_KEYS);

      this.addTeachingRelation = conn.prepareStatement(
          "INSERT INTO is_teaching_section "
          + "(instructor_id, section_id, instructor_name) "
          + "VALUES (?, ?, ?)");
    }

    public void close() throws SQLException {
      this.sections.close();
      this.descriptions.close();
      this.getInstructors.close();
      this.createInstructor.close();
      this.addTeachingRelation.close();
    }
  }

  private static class SaveState {
    Subject code;
    int id;
    int registrationNumber;
    String data;
    SaveState(Subject c, int i, int r, String d) {
      code = c;
      id = i;
      registrationNumber = r;
      data = d;
    }
  }

  public static void updateSections(Connection conn, Term term,
                                    Iterator<SectionID> sectionIds,
                                    Integer batchSizeNullable)
      throws SQLException {
    conn.setAutoCommit(false);

    try (Prepared p = new Prepared(conn)) {
      updateSections(p, term, sectionIds, batchSizeNullable);

      conn.commit();
      conn.setAutoCommit(true);
    } catch (SQLException | RuntimeException e) {
      conn.rollback();
      conn.setAutoCommit(true);

      throw e;
    }
  }

  public static void updateSections(Prepared p, Term term,
                                    Iterator<SectionID> sectionIds,
                                    Integer batchSizeNullable)
      throws SQLException {
    Iterator<SaveState> sectionAttributes = new SimpleBatchedFutureEngine<>(
        sectionIds, batchSizeNullable == null ? 40 : batchSizeNullable,
        (sectionID, __)
            -> querySectionAsync(
                term, sectionID.registrationNumber,
                str
                -> new SaveState(sectionID.subjectCode, sectionID.id,
                                 sectionID.registrationNumber, str)));

    HashMap<Integer, String> courseDescriptions = new HashMap<>();

    while (sectionAttributes.hasNext()) {
      SaveState save = sectionAttributes.next();

      if (save == null)
        continue;

      TryCatch tc =
          tcNew(logger, "Parse error on term={}, registrationNumber={}", term,
                save.registrationNumber);

      Section s = tc.pass(() -> nonnull(ParseSection.parse(save.data)));

      logger.debug("Adding section information...");

      for (String i : s.instructors) {
        if (i.equals("Staff"))
          continue;

        upsertInstructor(p, save.code, save.id, i);
      }

      PreparedStatement stmt = p.sections;
      Utils.setArray(stmt, s.name, save.code.toString() + ' ' + s.name,
                     s.campus, Utils.nullable(Types.VARCHAR, s.instructionMode),
                     s.minUnits, s.maxUnits, s.location, s.grading,
                     Utils.nullable(Types.VARCHAR, s.notes),
                     Utils.nullable(Types.VARCHAR, s.notes),
                     Utils.nullable(Types.VARCHAR, s.prerequisites),
                     Utils.nullable(Types.VARCHAR, s.prerequisites), save.id);
      stmt.execute();

      ResultSet rs = stmt.getResultSet();
      if (!rs.next())
        throw new RuntimeException("why");

      int courseId = rs.getInt(1);
      rs.close();

      if (!courseDescriptions.containsKey(courseId) && s.description != null) {
        courseDescriptions.put(courseId, s.description);
      }
    }

    PreparedStatement stmt = p.descriptions;
    for (Map.Entry<Integer, String> entry : courseDescriptions.entrySet()) {
      Utils.setArray(stmt, entry.getValue(), entry.getKey());

      if (stmt.executeUpdate() == 0)
        throw new RuntimeException("why did this fail?");
    }
  }

  public static void upsertInstructor(Prepared p, Subject subject,
                                      int sectionId, String instructor)
      throws SQLException {
    PreparedStatement stmt = p.getInstructors;
    Utils.setArray(stmt, subject.code, instructor);

    ResultSet rs = stmt.executeQuery();
    int instructorId;
    if (!rs.next()) {
      rs.close();

      PreparedStatement createInstructor = p.createInstructor;
      Utils.setArray(createInstructor, instructor, subject.code);

      if (createInstructor.executeUpdate() == 0)
        throw new RuntimeException("Why bro");

      rs = createInstructor.getGeneratedKeys();
      if (!rs.next())
        throw new RuntimeException("man c'mon");
    }

    instructorId = rs.getInt("id");
    rs.close();

    PreparedStatement addTeachingRelation = p.addTeachingRelation;
    Utils.setArray(addTeachingRelation, instructorId, sectionId, instructor);

    if (addTeachingRelation.executeUpdate() != 1)
      throw new RuntimeException("wtf dude");
  }
}
