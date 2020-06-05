package database.courses;

import static scraping.query.QuerySection.querySectionAsync;

import database.instructors.UpsertInstructor;
import database.models.SectionID;
import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import nyu.SubjectCode;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.models.SectionAttribute;
import scraping.parse.ParseSection;
import utils.SimpleBatchedFutureEngine;
import utils.Utils;

/**
 * This class insert courses into the Postgresql database based on
 * the data scraped from Albert Mobile
 */
public class UpdateSections {
  private static Logger logger =
      LoggerFactory.getLogger("database.courses.UpdateSections");

  private static class SaveState {
    SubjectCode code;
    int id;
    int registrationNumber;
    String data;
    SaveState(SubjectCode c, int i, int r, String d) {
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
    Iterator<SaveState> sectionAttributes = new SimpleBatchedFutureEngine<>(
        sectionIds, batchSizeNullable == null ? 40 : batchSizeNullable,
        (sectionID, __)
            -> querySectionAsync(
                term, sectionID.registrationNumber,
                str
                -> new SaveState(sectionID.subjectCode, sectionID.id,
                                 sectionID.registrationNumber, str)

                    ));

    HashMap<Integer, String> courseDescriptions = new HashMap<>();
    while (sectionAttributes.hasNext()) {
      SaveState save = sectionAttributes.next();

      if (save == null)
        continue;

      SectionAttribute s;
      try {
        s = ParseSection.parse(save.data);
      } catch (NullPointerException e) {
        logger.warn("Parse error on registrationNumber=" +
                    save.registrationNumber);
        throw e;
      }
      if (s == null) {
        logger.warn("Parse error on registrationNumber=" +
                    save.registrationNumber);
        continue;
      }

      logger.debug("Adding section information...");

      for (String i : s.instructors) {
        if (i.equals("Staff"))
          continue;
        UpsertInstructor.upsertInstructor(conn, save.code, save.id, i);
      }

      PreparedStatement stmt =
          conn.prepareStatement("UPDATE sections "
                                + "SET name = ?, "
                                + "name_vec = to_tsvector(?), "
                                + "campus = ?, "
                                + "instruction_mode = ?, "
                                + "min_units = ?, "
                                + "max_units = ?, "
                                + "location = ?, "
                                + "grading = ?, "
                                + "notes = ?, "
                                + "notes_vec = to_tsvector(?), "
                                + "prerequisites = ?, "
                                + "prereqs_vec = to_tsvector(?) "
                                + "WHERE sections.id = ?"
                                + "RETURNING sections.course_id");

      Utils.setArray(stmt, s.sectionName,
                     save.code.toString() + ' ' + s.sectionName, s.campus,
                     Utils.nullable(Types.VARCHAR, s.instructionMode),
                     s.minUnits, s.maxUnits, s.location, s.grading,
                     Utils.nullable(Types.VARCHAR, s.notes),
                     Utils.nullable(Types.VARCHAR, s.notes),
                     Utils.nullable(Types.VARCHAR, s.prerequisites),
                     Utils.nullable(Types.VARCHAR, s.prerequisites), save.id);
      stmt.execute();
      ResultSet rs = stmt.getResultSet();
      if (!rs.next()) {
        throw new RuntimeException("why");
      }
      int courseId = rs.getInt(1);
      rs.close();

      if (!courseDescriptions.containsKey(courseId) && s.description != null) {
        courseDescriptions.put(courseId, s.description);
      }
    }

    PreparedStatement stmt = conn.prepareStatement(
        "UPDATE courses SET description = ? WHERE id = ?");
    for (Map.Entry<Integer, String> entry : courseDescriptions.entrySet()) {
      Utils.setArray(stmt, entry.getValue(), entry.getKey());
      if (stmt.executeUpdate() == 0)
        throw new RuntimeException("why did this fail?");
    }
  }
}
