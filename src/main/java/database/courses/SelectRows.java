package database.courses;

import database.models.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;
import org.slf4j.*;
import types.*;
import utils.Utils;

public class SelectRows {

  private static Logger logger =
      LoggerFactory.getLogger("database.courses.SelectRows");

  public static Stream<Row> selectRows(Connection conn, Term term, String code)
      throws SQLException {
    return selectRows(conn, "courses.term = ? AND courses.subject_code = ?",
                      term.json(), code);
  }

  public static Stream<Row> selectRow(Connection conn, Term term,
                                      int registrationNumber)
      throws SQLException {
    PreparedStatement sectionIdStmt = conn.prepareStatement(
        "SELECT sections.id FROM courses JOIN sections ON courses.id = sections.course_id "
        + "WHERE sections.registration_number = ? AND courses.term = ?");

    Utils.setArray(sectionIdStmt, registrationNumber, term.json());

    ResultSet rs = sectionIdStmt.executeQuery();
    if (!rs.next())
      return Stream.empty();

    int sectionId = rs.getInt(1);
    Integer[] intSectionIds = new Integer[] {sectionId};
    Array sectionIds = conn.createArrayOf("integer", intSectionIds);

    rs.close();
    sectionIdStmt.close();

    return selectRows(
        conn, "sections.id = ANY (?) OR sections.associated_with = ANY (?)",
        sectionIds, sectionIds);
  }

  public static Stream<Row> selectRowsBySectionId(Connection conn, Term term,
                                                  List<Integer> sectionIds)
      throws SQLException {
    Array idArray = conn.createArrayOf("INTEGER", sectionIds.toArray());

    return SelectRows.selectRows(
        conn,
        "courses.term = ? AND (sections.id = ANY (?) OR sections.associated_with = ANY (?))",
        term.json(), idArray, idArray);
  }

  public static Stream<Row> selectRows(Connection conn, String conditions,
                                       Object... objects) throws SQLException {
    Map<Integer, List<Meeting>> meetingsList =
        selectMeetings(conn, conditions, objects);

    PreparedStatement stmt = conn.prepareStatement(
        "SELECT courses.*, sections.id AS section_id,"
        + "sections.registration_number, sections.section_code,"
        + "sections.section_type, sections.section_status, "
        + "sections.instructors, sections.associated_with, "
        + "sections.waitlist_total, sections.name as section_name, "
        + "sections.min_units, sections.max_units, sections.location, "
        + "sections.instruction_mode "
        + "FROM courses JOIN sections ON courses.id = sections.course_id "
        + "WHERE " + conditions);
    Utils.setArray(stmt, objects);

    ResultSet rs = stmt.executeQuery();

    List<Meeting> empty = new ArrayList<>();
    ArrayList<Row> rows = new ArrayList<>();

    while (rs.next()) {
      int id = rs.getInt("section_id");
      Row row = new Row(rs, meetingsList.getOrDefault(id, empty));
      rows.add(row);
    }

    rs.close();
    stmt.close();

    return rows.stream();
  }

  public static Stream<FullRow> selectFullRows(Connection conn, Term term,
                                               String code)
      throws SQLException {
    return selectFullRows(conn, "courses.term = ? AND courses.subject_code = ?",
                          term.json(), code);
  }

  public static Stream<FullRow> selectFullRow(Connection conn, Term term,
                                              int registrationNumber)
      throws SQLException {
    PreparedStatement sectionIdStmt = conn.prepareStatement(
        "SELECT sections.id FROM courses JOIN sections ON courses.id = sections.course_id "
        + "WHERE sections.registration_number = ? AND courses.term = ?");

    Utils.setArray(sectionIdStmt, registrationNumber, term.json());
    ResultSet rs = sectionIdStmt.executeQuery();
    ArrayList<Integer> sectionIds = new ArrayList<>();
    if (!rs.next()) {
      rs.close();
      return Stream.empty();
    }

    int sectionId = rs.getInt(1);

    rs.close();
    sectionIdStmt.close();

    return selectFullRows(conn,
                          "sections.associated_with = ? OR sections.id = ?",
                          sectionId, sectionId);
  }

  public static Stream<FullRow>
  selectFullRows(Connection conn, String conditions, Object... objects)
      throws SQLException {
    Map<Integer, List<Meeting>> meetingsList =
        selectMeetings(conn, conditions, objects);

    PreparedStatement stmt = conn.prepareStatement(
        "SELECT courses.*, sections.id AS section_id, sections.registration_number, sections.section_code, "
        + "sections.section_type, sections.section_status, "
        + "sections.instructors, sections.associated_with, "
        + "sections.waitlist_total, sections.name AS section_name, "
        + "sections.min_units, sections.max_units, sections.location, "
        + "sections.campus, sections.instruction_mode, "
        + "sections.grading, sections.notes, sections.prerequisites "
        + "FROM courses JOIN sections ON courses.id = sections.course_id "
        + "WHERE " + conditions);

    Utils.setArray(stmt, objects);

    ResultSet rs = stmt.executeQuery();

    List<Meeting> empty = new ArrayList<>();
    ArrayList<FullRow> rows = new ArrayList<>();
    while (rs.next()) {
      int id = rs.getInt("section_id");
      FullRow row = new FullRow(rs, meetingsList.getOrDefault(id, empty));
      rows.add(row);
    }

    rs.close();
    stmt.close();

    return rows.stream();
  }

  public static Map<Integer, List<Meeting>>
  selectMeetings(Connection conn, String conditions, Object... objects)
      throws SQLException {
    PreparedStatement stmt = conn.prepareStatement(
        "SELECT sections.id as section_id, "
        + "meetings.begin_date, meetings.duration, meetings.end_date "
        + "FROM courses JOIN sections ON courses.id = sections.course_id "
        + "JOIN meetings ON sections.id = meetings.section_id "
        + "WHERE " + conditions);
    Utils.setArray(stmt, objects);

    HashMap<Integer, List<Meeting>> meetingsBySection = new HashMap<>();

    ResultSet rs = stmt.executeQuery();
    while (rs.next()) {
      Integer sectionId = rs.getInt("section_id");

      List<Meeting> meetings = meetingsBySection.get(sectionId);
      if (meetings == null) {
        meetings = new ArrayList<>();

        meetingsBySection.put(sectionId, meetings);
      }

      Meeting meeting = new Meeting();
      meeting.beginDate = rs.getObject(2, LocalDateTime.class);
      meeting.minutesDuration = rs.getInt(3);
      meeting.endDate = rs.getObject(4, LocalDateTime.class);

      meetings.add(meeting);
    }

    rs.close();
    stmt.close();

    return meetingsBySection;
  }
}
