package database;

import static utils.Nyu.*;

import database.models.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import utils.Utils;

public class SelectRows {
  public static ArrayList<Row> selectRows(Connection conn, Term term, String code)
      throws SQLException {
    return selectRows(conn, "courses.term = ? AND courses.subject_code = ?", term.json(), code);
  }

  public static ArrayList<Row> selectRow(Connection conn, Term term, int registrationNumber)
      throws SQLException {
    PreparedStatement sectionIdStmt =
        conn.prepareStatement(
            "SELECT sections.id FROM courses JOIN sections ON courses.id = sections.course_id "
                + "WHERE sections.registration_number = ? AND courses.term = ?");

    Utils.setArray(sectionIdStmt, registrationNumber, term.json());
    ResultSet rs = sectionIdStmt.executeQuery();
    if (!rs.next()) {
      rs.close();
      return new ArrayList<>();
    }

    int sectionId = rs.getInt(1);

    rs.close();
    sectionIdStmt.close();

    return selectRows(
        conn, "sections.associated_with = ? OR sections.id = ?", sectionId, sectionId);
  }

  public static ArrayList<Row> selectRows(Connection conn, String conditions, Object... objects)
      throws SQLException {
    Map<Integer, List<Meeting>> meetingsList = selectMeetings(conn, conditions, objects);

    var stmt =
        conn.prepareStatement(
            "SELECT courses.*, sections.id AS section_id, sections.registration_number, "
                + "sections.section_code, sections.name AS section_name, "
                + "sections.section_type, sections.section_status, "
                + "sections.instructors, sections.associated_with, "
                + "sections.waitlist_total, "
                + "sections.min_units, sections.max_units, sections.location, "
                + "sections.campus, sections.instruction_mode, "
                + "sections.grading, sections.notes "
                + "FROM courses JOIN sections ON courses.id = sections.course_id "
                + "WHERE "
                + conditions);

    Utils.setArray(stmt, objects);

    ResultSet rs = stmt.executeQuery();

    List<Meeting> empty = new ArrayList<>();
    ArrayList<Row> rows = new ArrayList<>();
    while (rs.next()) {
      int id = rs.getInt("section_id");
      var row = new Row(rs, meetingsList.getOrDefault(id, empty));
      rows.add(row);
    }

    stmt.close();

    return rows;
  }

  public static Map<Integer, List<Meeting>> selectMeetings(
      Connection conn, String conditions, Object... objects) throws SQLException {
    PreparedStatement stmt =
        conn.prepareStatement(
            "SELECT sections.id as section_id, "
                + "meetings.begin_date, meetings.duration, meetings.end_date "
                + "FROM courses JOIN sections ON courses.id = sections.course_id "
                + "JOIN meetings ON sections.id = meetings.section_id "
                + "WHERE "
                + conditions);
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
      meeting.beginDate = rs.getObject(2, LocalDateTime.class).atZone(ZoneOffset.UTC);
      meeting.minutesDuration = rs.getInt(3);
      meeting.endDate = rs.getObject(4, LocalDateTime.class).atZone(ZoneOffset.UTC);

      meetings.add(meeting);
    }

    rs.close();
    stmt.close();

    return meetingsBySection;
  }
}
