package database.courses;

import database.models.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Stream;
import org.slf4j.*;
import utils.Nyu;
import utils.Utils;

public final class SearchRows {

  private static Logger logger =
      LoggerFactory.getLogger("database.courses.SearchCourses");

  public static Stream<Row> searchRows(Connection conn, Nyu.Term term,
                                       String subject, String school,
                                       String query) throws SQLException {
    ArrayList<String> fields = new ArrayList<>();
    fields.add(
        "to_tsvector(courses.name || ' ' || courses.description || ' ' || sections.notes) @@ q.query");

    // @TODO fix all this shit

    if (subject != null)
      subject = subject.toUpperCase();
    if (school != null)
      school = school.toUpperCase();

    String begin = "WITH q (query) AS (SELECT websearch_to_tsquery(?)) "
                   + "SELECT DISTINCT courses.id FROM q, "
                   + "courses JOIN sections ON courses.id = sections.course_id "
                   + "WHERE (" + fields + ") AND ";
    PreparedStatement idStmt;
    if (subject != null && school != null) {
      idStmt = conn.prepareStatement(
          begin + "term = ? AND courses.subject = ? AND courses.school = ?");
      Utils.setArray(idStmt, query, term.json(), subject, school);
    } else if (subject != null) {
      idStmt =
          conn.prepareStatement(begin + "term = ? AND courses.subject = ?");
      Utils.setArray(idStmt, query, term.json(), subject);
    } else if (school != null) {
      idStmt = conn.prepareStatement(begin + "term = ? AND courses.school = ?");
      Utils.setArray(idStmt, query, term.json(), school);
    } else {
      idStmt = conn.prepareStatement(begin + "term = ?");
      Utils.setArray(idStmt, query, term.json());
    }

    ArrayList<Integer> result = new ArrayList<>();
    ResultSet rs = idStmt.executeQuery();
    while (rs.next()) {
      result.add(rs.getInt(1));
    }
    rs.close();

    PreparedStatement rowStmt = conn.prepareStatement(
        "WITH q (query) AS (SELECT websearch_to_tsquery(?)) "
        + "SELECT courses.*, sections.id AS section_id, "
        + "sections.registration_number, sections.section_code, "
        + "sections.section_type, sections.section_status, "
        + "sections.associated_with, sections.waitlist_total, "
        + "sections.name AS section_name, sections.instruction_mode,"
        + "sections.min_units, sections.max_units, sections.location, "
        + "sections.instructors "
        + "FROM q, courses LEFT JOIN sections "
        + "ON courses.id = sections.course_id "
        + "WHERE courses.id = ANY (?)");
    Utils.setArray(rowStmt, query,
                   conn.createArrayOf("INTEGER", result.toArray()));
    Map<Integer, List<Nyu.Meeting>> meetingsList = SelectRows.selectMeetings(
        conn, " courses.id = ANY (?) ",
        conn.createArrayOf("integer", result.toArray()));

    ArrayList<Row> rows = new ArrayList<>();
    rs = rowStmt.executeQuery();
    while (rs.next()) {
      rows.add(new Row(rs, meetingsList.get(rs.getInt("section_id"))));
    }

    rs.close();
    return rows.stream();
  }

  public static Stream<FullRow> searchFullRows(Connection conn, Nyu.Term term,
                                               String subject, String school,
                                               String query)
      throws SQLException {
    ArrayList<String> fields = new ArrayList<>();
    fields.add(
        "to_tsvector(courses.name || ' ' || courses.description || ' ' || sections.notes) @@ q.query");

    if (subject != null)
      subject = subject.toUpperCase();
    if (school != null)
      school = school.toUpperCase();
    String begin = "WITH q (query) AS (SELECT plainto_tsquery(?)) "
                   + "SELECT DISTINCT courses.id FROM q, "
                   + "courses JOIN sections ON courses.id = sections.course_id "
                   + "WHERE (" + String.join(" OR ", fields) + ") AND ";
    PreparedStatement idStmt;
    if (subject != null && school != null) {
      idStmt = conn.prepareStatement(
          begin + "term = ? AND courses.subject = ? AND courses.school = ?");
      Utils.setArray(idStmt, query, term.json(), subject, school);
    } else if (subject != null) {
      idStmt =
          conn.prepareStatement(begin + "term = ? AND courses.subject = ?");
      Utils.setArray(idStmt, query, term.json(), subject);
    } else if (school != null) {
      idStmt = conn.prepareStatement(begin + "term = ? AND courses.school = ?");
      Utils.setArray(idStmt, query, term.json(), school);
    } else {
      idStmt = conn.prepareStatement(begin + "term = ?");
      Utils.setArray(idStmt, query, term.json());
    }

    ArrayList<Integer> result = new ArrayList<>();
    ResultSet rs = idStmt.executeQuery();
    while (rs.next()) {
      result.add(rs.getInt(1));
    }
    rs.close();

    PreparedStatement rowStmt = conn.prepareStatement(
        "WITH q (query) AS (SELECT plainto_tsquery(?)) "
        + "SELECT courses.*, sections.id AS section_id, "
        + "sections.registration_number, sections.section_code, "
        + "sections.section_type, sections.section_status, "
        + "sections.associated_with, sections.waitlist_total, "
        + "sections.name AS section_name, "
        + "sections.min_units, sections.max_units, sections.location, "
        + "sections.campus, sections.instruction_mode, "
        + "sections.grading, sections.notes, sections.prerequisites, "
        + "sections.instructors "
        + "FROM q, courses LEFT JOIN sections "
        + "ON courses.id = sections.course_id "
        + "WHERE courses.id = ANY (?)");
    Utils.setArray(rowStmt, query,
                   conn.createArrayOf("INTEGER", result.toArray()));
    Map<Integer, List<Nyu.Meeting>> meetingsList = SelectRows.selectMeetings(
        conn, " courses.id = ANY (?) ",
        conn.createArrayOf("integer", result.toArray()));

    ArrayList<FullRow> rows = new ArrayList<>();
    rs = rowStmt.executeQuery();
    while (rs.next()) {
      rows.add(new FullRow(rs, meetingsList.get(rs.getInt("section_id"))));
    }

    rs.close();
    return rows.stream();
  }
}
