package database.courses;

import java.sql.*;
import java.util.List;
import org.slf4j.*;
import types.*;
import utils.Utils;

/**
 * This class insert courses into the Postgresql database based on
 * the data scraped from Albert Mobile
 */
public final class InsertFullCourses {
  private static Logger logger =
      LoggerFactory.getLogger("database.courses.InsertCourses");

  private static final class Prepared implements AutoCloseable {
    final PreparedStatement courses;
    final PreparedStatement sections;
    final PreparedStatement meetings;

    private static final String placeholders = String.join(
        ",", new String[] {"?", "to_tsvector(?)", "?", "?", "?", "?", "?", "?",
                           "?", "?", "to_tsvector(?)", "?", "to_tsvector(?)",
                           "?", "?", "?", "?", "?", "?"});

    // @TODO add instructors
    private static final String fieldNames = String.join(
        ",", new String[] {"name", "name_vec", "registration_number", "campus",
                           "min_units", "max_units", "instruction_mode",
                           "location", "grading", "notes", "notes_vec",
                           "prerequisites", "prereqs_vec", "course_id",
                           "section_code", "section_type", "section_status",
                           "waitlist_total", "associated_with"});

    Prepared(Connection conn) throws SQLException {
      this.courses =
          conn.prepareStatement("INSERT INTO courses "
                                    + "(epoch, name, name_vec, subject_code, "
                                    + "dept_course_id) "
                                    + "VALUES (?, ?, to_tsvector(?), ?, ?)",
                                Statement.RETURN_GENERATED_KEYS);

      this.sections =
          conn.prepareStatement("INSERT INTO sections (" + fieldNames +
                                    ") VALUES (" + placeholders + ")",
                                Statement.RETURN_GENERATED_KEYS);

      this.meetings = conn.prepareStatement(
          "INSERT INTO meetings (section_id, begin_date, duration, end_date) VALUES (?, ?, ?, ?)");
    }

    public void close() throws SQLException {
      this.courses.close();
      this.sections.close();
      this.meetings.close();
    }
  }

  public static void insertCourses(Connection conn, Term term, int epoch,
                                   List<Course> courses) throws SQLException {
    conn.setAutoCommit(false);

    try {
      Prepared p = new Prepared(conn);

      insertCourses(p, term, epoch, courses);

      conn.commit();
      conn.setAutoCommit(true);
    } catch (SQLException | RuntimeException e) {
      conn.rollback();
      conn.setAutoCommit(true);

      throw e;
    }
  }

  private static void insertCourses(Prepared p, Term term, int epoch,
                                    List<Course> courses) throws SQLException {
    PreparedStatement stmt = p.courses;

    for (Course c : courses) {
      Utils.setArray(stmt, epoch, c.name, c.name, c.subjectCode.code,
                     c.deptCourseId);

      if (stmt.executeUpdate() == 0) {
        throw new RuntimeException("inserting course=" + c.toString() +
                                   " failed, no rows affected.");
      }

      ResultSet rs = stmt.getGeneratedKeys();
      if (!rs.next())
        throw new RuntimeException("inserting course failed for course=" +
                                   c.toString());

      int courseId = rs.getInt(1);
      rs.close();

      insertSections(p, courseId, c.sections, null);
    }
  }

  private static void insertSections(Prepared p, int courseId,
                                     List<Section> sections,
                                     Integer associatedWith)
      throws SQLException {
    PreparedStatement stmt = p.sections;

    for (Section s : sections) {
      Object[] fieldValues = new Object[] {
          s.name,
          s.name,
          s.registrationNumber,
          s.campus,
          s.minUnits,
          s.maxUnits,
          Utils.nullable(Types.VARCHAR, s.instructionMode),
          s.location,
          s.grading,
          Utils.nullable(Types.VARCHAR, s.notes),
          Utils.nullable(Types.VARCHAR, s.notes),
          Utils.nullable(Types.VARCHAR, s.prerequisites),
          Utils.nullable(Types.VARCHAR, s.prerequisites),
          courseId,
          s.code,
          s.type.name(),
          s.status.name(),
          Utils.nullable(Types.INTEGER, s.waitlistTotal),
          Utils.nullable(Types.INTEGER, associatedWith),
      };

      try {
        Utils.setArray(stmt, fieldValues);

        if (stmt.executeUpdate() == 0)
          throw new RuntimeException("inserting section=" + s.toString() +
                                     " failed, no rows affected.");

        ResultSet rs = stmt.getGeneratedKeys();
        if (!rs.next())
          throw new RuntimeException("inserting section=" + s.toString() +
                                     "failed");

        int sectionId = rs.getInt(1);
        rs.close();

        insertMeetings(p, sectionId, s.meetings);
        if (s.recitations != null) {
          if (associatedWith != null)
            throw new RuntimeException("why did this happen?");

          insertSections(p, courseId, s.recitations, sectionId);
        }
      } catch (Exception e) {
        logger.error("throwing with section={}", s.toString());
        throw e;
      }
    }
  }

  private static void insertMeetings(Prepared p, int sectionId,
                                     List<Meeting> meetings)
      throws SQLException {
    // safety measure for now, because production code is a lil broken
    if (meetings == null)
      return;

    PreparedStatement stmt = p.meetings;

    for (Meeting m : meetings) {
      Utils.setArray(stmt, sectionId, m.beginDate, m.minutesDuration,
                     m.endDate);

      if (stmt.executeUpdate() != 1) {
        throw new RuntimeException("Why did this fail?");
      }
    }
  }
}
