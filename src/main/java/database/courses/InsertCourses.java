package database.courses;

import database.models.SectionID;
import java.sql.*;
import java.util.*;
import org.slf4j.*;
import scraping.models.*;
import types.Meeting;
import types.Term;
import utils.Utils;

/**
 * This class insert courses into the Postgresql database based on
 * the data scraped from Albert Mobile
 */
public class InsertCourses {

  private static Logger logger =
      LoggerFactory.getLogger("database.courses.InsertCourses");

  private static final class Prepared implements AutoCloseable {
    final PreparedStatement courses;
    final PreparedStatement sections;
    final PreparedStatement meetings;

    Prepared(Connection conn) throws SQLException {
      this.courses = conn.prepareStatement(
          "INSERT INTO courses "
              + "(term, name, name_vec, subject_code, dept_course_id) "
              + "VALUES (?, ?, to_tsvector(?), ?, ?)",
          Statement.RETURN_GENERATED_KEYS);

      this.sections = conn.prepareStatement(
          "INSERT INTO sections "
              + "(registration_number, course_id, section_code, section_type, "
              + "section_status, waitlist_total, associated_with) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);

      this.meetings = conn.prepareStatement(
          "INSERT INTO meetings "
          + "(section_id, begin_date, duration, end_date) "
          + "VALUES (?, ?, ?, ?)");
    }

    public void close() throws SQLException {
      this.courses.close();
      this.sections.close();
      this.meetings.close();
    }
  }

  public static void clearPrevious(Connection conn, Term term)
      throws SQLException {
    String sql = "DELETE FROM courses WHERE term = ?";
    try (PreparedStatement deletePrevious = conn.prepareStatement(sql)) {
      deletePrevious.setObject(1, term.json());
      deletePrevious.executeUpdate();
    }
  }

  public static ArrayList<SectionID> insertCourses(Connection conn, Term term,
                                                   List<Course> courses)
      throws SQLException {
    try (Prepared p = new Prepared(conn)) {
      ArrayList<SectionID> ids = insertCourses(p, term, courses);

      return ids;
    }
  }

  private static ArrayList<SectionID> insertCourses(Prepared p, Term term,
                                                    List<Course> courses)
      throws SQLException {
    ArrayList<SectionID> states = new ArrayList<>();
    PreparedStatement stmt = p.courses;

    for (Course c : courses) {
      Utils.setArray(stmt, term.json(), c.name, c.name, c.subjectCode,
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

      insertSections(p, courseId, c.sections, null, states);
    }

    return states;
  }

  private static void
  insertSections(Prepared p, int courseId, List<Section> sections,
                 Integer associatedWith, ArrayList<SectionID> states)
      throws SQLException {
    PreparedStatement stmt = p.sections;

    for (Section s : sections) {
      try {
        Utils.setArray(stmt, s.registrationNumber, courseId, s.sectionCode,
                       s.type.name(), s.status.name(),
                       Utils.nullable(Types.INTEGER, s.waitlistTotal),
                       Utils.nullable(Types.INTEGER, associatedWith));

        if (stmt.executeUpdate() == 0)
          throw new RuntimeException("inserting section=" + s.toString() +
                                     " failed, no rows affected.");

        ResultSet rs = stmt.getGeneratedKeys();
        if (!rs.next())
          throw new RuntimeException("inserting section=" + s.toString() +
                                     "failed");

        int sectionId = rs.getInt(1);
        rs.close();

        states.add(
            new SectionID(s.subjectCode, sectionId, s.registrationNumber));

        insertMeetings(p, sectionId, s.meetings);
        if (!s.recitations.isEmpty()) {
          if (associatedWith != null) {
            throw new RuntimeException("why did this happen?");
          }

          insertSections(p, courseId, s.recitations, sectionId, states);
        }
      } catch (SQLException e) {
        logger.error("throwing with section={}", s.toString());

        throw e;
      }
    }
  }

  private static void insertMeetings(Prepared p, int sectionId,
                                     List<Meeting> meetings)
      throws SQLException {
    PreparedStatement stmt = p.meetings;

    for (Meeting m : meetings) {
      Utils.setArray(stmt, sectionId, m.beginDate, m.minutesDuration,
                     m.endDate);

      if (stmt.executeUpdate() == 0)
        throw new RuntimeException("Why did this fail?");
    }
  }
}
