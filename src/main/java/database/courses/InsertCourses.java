package database.courses;

import static utils.Nyu.*;

import java.sql.*;
import java.util.List;
import org.slf4j.*;
import utils.Utils;

/**
 * This class insert courses into the Postgresql database based on
 * the data scraped from Albert Mobile
 */
public final class InsertCourses {
  private static Logger logger =
      LoggerFactory.getLogger("database.courses.InsertCourses");

  private static final class Prepared implements AutoCloseable {
    final PreparedStatement courses;
    final PreparedStatement sections;
    final PreparedStatement meetings;

    private static final String sectionSql;

    static {
      String[] sectionFields = new String[] {"registration_number",
                                             "campus",
                                             "min_units",
                                             "max_units",
                                             "instruction_mode",
                                             "location",
                                             "grading",
                                             "notes",
                                             "course_id",
                                             "section_code",
                                             "section_type",
                                             "section_status",
                                             "waitlist_total",
                                             "associated_with",
                                             "instructors"};

      StringBuilder builder = new StringBuilder();
      builder.append("INSERT INTO sections (");

      for (int i = 0; i < sectionFields.length; i++) {
        if (i != 0) {
          builder.append(", ");
        }

        builder.append(sectionFields[i]);
      }

      builder.append(") VALUES (");

      for (int i = 0; i < sectionFields.length; i++) {
        if (i != 0) {
          builder.append(", ");
        }

        builder.append('?');
      }

      builder.append(") RETURNING id");

      sectionSql = builder.toString();
    }

    Prepared(Connection conn) throws SQLException {
      this.courses =
          conn.prepareStatement("INSERT INTO courses "
                                + "(term, name, subject_code, "
                                + "dept_course_id, description) "
                                + "VALUES (?, ?, ?, ?, ?) RETURNING id");

      this.sections = conn.prepareStatement(sectionSql);

      this.meetings = conn.prepareStatement(
          "INSERT INTO meetings (section_id, begin_date, duration, end_date) VALUES (?, ?, ?, ?)");
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

  public static void insertCourses(Connection conn, Term term,
                                   List<Course> courses) throws SQLException {
    try (Prepared p = new Prepared(conn)) {
      insertCourses(p, term, courses);
    }
  }

  private static void insertCourses(Prepared p, Term term, List<Course> courses)
      throws SQLException {
    PreparedStatement stmt = p.courses;

    for (Course c : courses) {
      Utils.setArray(stmt, term.json(), c.name, c.subjectCode, c.deptCourseId,
                     c.description);
      stmt.execute();

      ResultSet rs = stmt.getResultSet();
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
      Object[] fieldValues =
          new Object[] {s.registrationNumber,
                        s.campus,
                        s.minUnits,
                        s.maxUnits,
                        s.instructionMode,
                        Utils.nullable(Types.VARCHAR, s.location),
                        s.grading,
                        s.notes,
                        courseId,
                        s.code,
                        s.type,
                        s.status.name(),
                        Utils.nullable(Types.INTEGER, s.waitlistTotal),
                        Utils.nullable(Types.INTEGER, associatedWith),
                        s.instructors};

      try {
        Utils.setArray(stmt, fieldValues);
        stmt.execute();

        ResultSet rs = stmt.getResultSet();
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
