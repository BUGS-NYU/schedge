package database.courses;

import database.models.SectionID;
import java.sql.*;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import models.*;
import nyu.Meeting;
import nyu.SubjectCode;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

/**
 * This class insert courses into the Postgresql database based on
 * the data scraped from Albert Mobile
 */
public class InsertFullCourses {
  private static Logger logger =
      LoggerFactory.getLogger("database.courses.InsertCourses");

  public static void insertCourses(Connection conn, Term term, int epoch,
                                   List<Course> courses) throws SQLException {
    PreparedStatement stmt =
        conn.prepareStatement("INSERT INTO courses "
                                  + "(epoch, name, name_vec, school, subject, "
                                  + "dept_course_id, term_id) "
                                  + "VALUES (?, ?, to_tsvector(?), ?, ?, ?, ?)",
                              Statement.RETURN_GENERATED_KEYS);

    for (Course c : courses) {
      Utils.setArray(stmt, epoch, c.name, c.name, c.subjectCode.school,
                     c.subjectCode.code, c.deptCourseId, term.getId());

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
      insertSections(conn, courseId, c.subjectCode, c.sections, null);
    }
  }

  private static String placeholders = String.join(
      ",", new String[] {"?", "to_tsvector(?)", "?", "?", "?", "?", "?", "?",
                         "?", "?", "to_tsvector(?)", "?", "to_tsvector(?)", "?",
                         "?", "?", "?", "?", "?"});

  private static String fieldNames = String.join(
      ",", new String[] {"name", "name_vec", "registration_number", "campus",
                         "min_units", "max_units", "instruction_mode",
                         "location", "grading", "notes", "notes_vec",
                         "prerequisites", "prerequisites_vec", "course_id",
                         "section_code", "section_type", "section_status",
                         "waitlist_total", "associated_with"});

  public static void
  insertSections(Connection conn, int courseId, SubjectCode subjectCode,
                 List<Section> sections, Integer associatedWith)
      throws SQLException {
    PreparedStatement stmt =
        conn.prepareStatement("INSERT INTO sections (" + fieldNames +
                                  ") VALUES (" + placeholders + ")",
                              Statement.RETURN_GENERATED_KEYS);

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
          s.type.ordinal(),
          s.status.ordinal(),
          Utils.nullable(Types.INTEGER, s.waitlistTotal),
          Utils.nullable(Types.INTEGER, associatedWith),
      };

      try {
        Utils.setArray(stmt, s.registrationNumber, courseId, s.code,
                       s.type.ordinal(), s.status.ordinal(),
                       Utils.nullable(Types.INTEGER, s.waitlistTotal));

        if (stmt.executeUpdate() == 0)
          throw new RuntimeException("inserting section=" + s.toString() +
                                     " failed, no rows affected.");

        ResultSet rs = stmt.getGeneratedKeys();
        if (!rs.next())
          throw new RuntimeException("inserting section=" + s.toString() +
                                     "failed");

        int sectionId = rs.getInt(1);
        rs.close();

        insertMeetings(conn, sectionId, s.meetings);
        if (s.recitations != null) {
          if (associatedWith != null)
            throw new RuntimeException("why did this happen?");

          insertSections(conn, courseId, subjectCode, s.recitations, sectionId);
        }
      } catch (Exception e) {
        logger.error("throwing with section={}", s.toString());
        throw e;
      }
    }
  }

  public static void insertMeetings(Connection conn, int sectionId,
                                    List<Meeting> meetings)
      throws SQLException {
    PreparedStatement deleteStmt =
        conn.prepareStatement("DELETE FROM meetings WHERE section_id = ?");
    deleteStmt.setInt(1, sectionId);
    deleteStmt.execute();

    PreparedStatement stmt = conn.prepareStatement(
        "INSERT INTO meetings (section_id, begin_date, duration, end_date) VALUES (?, ?, ?, ?)");

    for (Meeting m : meetings) {
      Utils.setArray(stmt, sectionId, m.beginDate, m.minutesDuration,
                     m.endDate);

      if (stmt.executeUpdate() == 0)
        throw new RuntimeException("Why did this fail?");
    }
  }
}
