package database.courses;

import database.models.SectionID;
import java.sql.*;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.models.Course;
import scraping.models.Meeting;
import scraping.models.Section;

/**
 * This class insert courses into the Postgresql database based on
 * the data scraped from Albert Mobile
 */
public class InsertCourses {

  private static Logger logger =
      LoggerFactory.getLogger("database.courses.InsertCourses");

  public static ArrayList<SectionID>
  insertCourses(Connection conn, Term term, int epoch, List<Course> courses)
      throws SQLException {
    ArrayList<SectionID> states = new ArrayList<>();
    PreparedStatement stmt =
        conn.prepareStatement("INSERT INTO courses "
                                  + "(epoch, name, name_vec, school, subject, "
                                  + "dept_course_id, term_id) "
                                  + "VALUES (?, ?, to_tsvector(?), ?, ?, ?, ?)",
                              Statement.RETURN_GENERATED_KEYS);
    for (Course c : courses) {
      stmt.setInt(1, epoch);
      stmt.setString(2, c.name);
      stmt.setString(3, c.name);
      stmt.setString(4, c.subjectCode.school);
      stmt.setString(5, c.subjectCode.code);
      stmt.setString(6, c.deptCourseId);
      stmt.setInt(7, term.getId());
      if (stmt.executeUpdate() == 0)
        throw new RuntimeException("inserting course=" + c.toString() +
                                   " failed, no rows affected.");

      ResultSet rs = stmt.getGeneratedKeys();
      if (!rs.next())
        throw new RuntimeException("inserting course failed for course=" +
                                   c.toString());

      int courseId = rs.getInt(1);
      rs.close();
      insertSections(conn, courseId, c.sections, null, states);
    }
    return states;
  }

  public static void
  insertSections(Connection conn, int courseId, List<Section> sections,
                 Integer associatedWith, ArrayList<SectionID> states)
      throws SQLException {
    PreparedStatement stmt;

    if (associatedWith != null) {
      stmt = conn.prepareStatement(
          "INSERT INTO sections "
          + "(registration_number, course_id, section_code, section_type, "
          + "section_status, waitlist_total, associated_with) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
      stmt.setInt(7, associatedWith);
    } else {
      stmt = conn.prepareStatement(
          "INSERT INTO sections "
              + "(registration_number, course_id, section_code, section_type, "
              + "section_status, waitlist_total) VALUES (?, ?, ?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
    }
    for (Section s : sections) {
      try {
        stmt.setInt(1, s.registrationNumber);
        stmt.setInt(2, courseId);
        stmt.setString(3, s.sectionCode);
        stmt.setInt(4, s.type.ordinal());
        stmt.setInt(5, s.status.ordinal());
          if (s.waitlistTotal == null) {
              stmt.setNull(6, Types.INTEGER);
          } else {
              stmt.setInt(6, s.waitlistTotal);
          }

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
        insertMeetings(conn, sectionId, s.meetings);
        if (s.recitations != null)
          insertSections(conn, courseId, s.recitations, sectionId, states);
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
      stmt.setInt(1, sectionId);
      stmt.setTimestamp(2,
                        Timestamp.from(m.beginDate.toInstant(ZoneOffset.UTC)));
      stmt.setLong(3, m.duration);
      stmt.setTimestamp(4, Timestamp.from(m.endDate.toInstant(ZoneOffset.UTC)));
      if (stmt.executeUpdate() == 0)
        throw new RuntimeException("Why did this fail?");
    }
  }
}
