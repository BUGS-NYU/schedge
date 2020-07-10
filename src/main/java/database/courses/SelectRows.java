package database.courses;

import database.models.FullRow;
import database.models.Row;
import java.sql.*;
import java.util.*;
import java.util.stream.Stream;
import nyu.Meeting;
import nyu.SubjectCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

public class SelectRows {

  private static Logger logger =
      LoggerFactory.getLogger("database.courses.SelectRows");

  public static Stream<Row> selectRows(Connection conn, int epoch,
                                       SubjectCode code) throws SQLException {
    return selectRows(
        conn,
        "courses.epoch = ? AND courses.school = ? AND courses.subject = ?",
        epoch, code.school, code.code);
  }

  public static Stream<Row> selectRow(Connection conn, int epoch,
                                      int registrationNumber)
      throws SQLException {
    PreparedStatement sectionIdStmt = conn.prepareStatement(
        "SELECT sections.id FROM courses JOIN sections ON courses.id = sections.course_id "
        + "WHERE sections.registration_number = ? AND courses.epoch = ?");

    Utils.setArray(sectionIdStmt, registrationNumber, epoch);
    ResultSet rs = sectionIdStmt.executeQuery();
    if (!rs.next())
      return Stream.empty();

    int sectionId = rs.getInt(1);
    Integer[] intSectionIds = new Integer[]{sectionId};
    Array sectionIds = conn.createArrayOf("integer", intSectionIds);
    rs.close();
    return selectRows(
        conn, "sections.id = ANY (?) OR sections.associated_with = ANY (?)",
        sectionIds, sectionIds);
  }

  public static Stream<Row> selectRowsBySectionId(Connection conn, int epoch,
                                                  List<Integer> sectionIds)
      throws SQLException {
    Array idArray = conn.createArrayOf("INTEGER", sectionIds.toArray());
    return SelectRows.selectRows(
        conn,
        "courses.epoch = ? AND (sections.id = ANY (?) OR sections.associated_with = ANY (?))",
        epoch, idArray, idArray);
  }

  public static Stream<Row> selectRows(Connection conn, String conditions,
                                       Object... objects) throws SQLException {
    Map<Integer, List<Meeting>> meetingsList =
        selectMeetings(conn, conditions, objects);

    PreparedStatement stmt = conn.prepareStatement(
        "SELECT courses.*, sections.id AS section_id,"
        + "sections.registration_number, sections.section_code,"
        + "sections.section_type, sections.section_status, "
        + "array_to_string(array_agg(its.instructor_name),';') "
        + "AS section_instructors, sections.associated_with, "
        + "sections.waitlist_total, sections.name as section_name, "
        + "sections.min_units, sections.max_units, sections.location,"
        + "sections.instruction_mode "
        + "FROM courses JOIN sections ON courses.id = sections.course_id "
        + "JOIN is_teaching_section its on sections.id = its.section_id "
        + "WHERE " + conditions + " GROUP BY courses.id, sections.id");
    Utils.setArray(stmt, objects);

    ResultSet rs = stmt.executeQuery();
    ArrayList<Row> rows = new ArrayList<>();
    while (rs.next()) {
      rows.add(new Row(rs, meetingsList.get(rs.getInt("section_id"))));
    }

    return rows.stream();
  }

  public static Stream<FullRow> selectFullRows(Connection conn, int epoch,
                                               SubjectCode code)
      throws SQLException {
    return selectFullRows(
        conn,
        "courses.epoch = ? AND courses.school = ? AND courses.subject = ?",
        epoch, code.school, code.code);
  }

  public static Stream<FullRow> selectFullRow(Connection conn, int epoch,
                                              int registrationNumber)
      throws SQLException {
    PreparedStatement sectionIdStmt = conn.prepareStatement(
        "SELECT sections.id FROM courses JOIN sections ON courses.id = sections.course_id "
        + "WHERE sections.registration_number = ? AND courses.epoch = ?");

    Utils.setArray(sectionIdStmt, registrationNumber, epoch);
    ResultSet rs = sectionIdStmt.executeQuery();
    ArrayList<Integer> sectionIds = new ArrayList<>();
    if (!rs.next())
      return Stream.empty();

    int sectionId = rs.getInt(1);
    rs.close();
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
        + "array_to_string(array_agg(its.instructor_name),';') "
        + "AS section_instructors, sections.associated_with, "
        + "sections.waitlist_total, sections.name AS section_name, "
        + "sections.min_units, sections.max_units, sections.location, "
        + "sections.campus, sections.instruction_mode, "
        + "sections.grading, sections.notes, sections.prerequisites "
        + "FROM courses JOIN sections ON courses.id = sections.course_id "
        + "JOIN is_teaching_section its on sections.id = its.section_id "
        + "WHERE " + conditions + " GROUP BY courses.id, sections.id");

    Utils.setArray(stmt, objects);

    ResultSet rs = stmt.executeQuery();
    ArrayList<FullRow> rows = new ArrayList<>();
    while (rs.next()) {
      rows.add(new FullRow(rs, meetingsList.get(rs.getInt("section_id"))));
    }

    return rows.stream();
  }

  public static List<Meeting>
  meetingList(String beginString, String durationString, String endString) {
    if (beginString == null)
      return Collections.emptyList();
    String[] beginDates = beginString.split(";");
    String[] durations = durationString.split(";");
    String[] endDates = endString.split(";");
    ArrayList<Meeting> meetings = new ArrayList<>(beginDates.length);
    for (int i = 0; i < beginDates.length; i++) {
      meetings.add(new Meeting(beginDates[i], durations[i], endDates[i]));
    }
    return meetings;
  }

  public static Map<Integer, List<Meeting>>
  selectMeetings(Connection conn, String conditions, Object... objects)
      throws SQLException {
    PreparedStatement stmt = conn.prepareStatement(
        "SELECT sections.id as section_id, "
        + "array_to_string(array_agg(meetings.begin_date),';'), "
        + "array_to_string(array_agg(meetings.duration),';'), "
        + "array_to_string(array_agg(meetings.end_date),';') "
        + "FROM courses JOIN sections ON courses.id = sections.course_id "
        + "JOIN meetings ON sections.id = meetings.section_id "
        + "WHERE " + conditions + " GROUP BY sections.id");
    Utils.setArray(stmt, objects);

    ResultSet rs = stmt.executeQuery();
    HashMap<Integer, List<Meeting>> meetings = new HashMap<>();
    while (rs.next()) {
      meetings.put(
          rs.getInt("section_id"),
          meetingList(rs.getString(2), rs.getString(3), rs.getString(4)));
    }
    rs.close();
    return meetings;
  }
}
