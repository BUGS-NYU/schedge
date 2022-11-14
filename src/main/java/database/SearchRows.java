package database;

import database.models.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Stream;
import utils.Nyu;
import utils.Utils;

public final class SearchRows {
  public static final class RankedId {}

  public static ArrayList<Row> searchRows(Connection conn, Nyu.Term term,
                                          String query, int limit)
      throws SQLException {
    if (limit > 50) {
      limit = 50;
    }
    if (limit < 1) {
      limit = 1;
    }

    ArrayList<String> fields = new ArrayList<>();
    fields.add("course_vec @@ q.query");
    fields.add("section_vec @@ q.query");

    var rankWeights = "'{1.0, 0.00000001, 1.0, 1.0}'";
    var ranking =
        "ts_rank_cd(" + rankWeights + ", course_vec, q.query)";

    var subquery =
        "SELECT DISTINCT ON (cid) courses.id cid, " + ranking + " rank FROM "
        + "q, courses JOIN sections ON courses.id = sections.course_id "
        + "WHERE courses.term = ? AND ((" + String.join(") OR (", fields) + "))";

    var stmtText = "WITH q (query) AS (SELECT plainto_tsquery(?)) "
                   + "SELECT cid FROM (" + subquery +
                   ") course_ids ORDER BY rank DESC LIMIT " + limit;
    var idStmt = conn.prepareStatement(stmtText);
    Utils.setArray(idStmt, query, term.json());

    var result = new ArrayList<Integer>();
    var rs = idStmt.executeQuery();
    while (rs.next()) {
      result.add(rs.getInt("cid"));
    }
    idStmt.close();

    var dataSql = "WITH q (query) AS (SELECT plainto_tsquery(?)) "
                  + "SELECT courses.*, sections.id AS section_id, "
                  + "sections.registration_number, sections.section_code, "
                  + "sections.section_type, sections.section_status, "
                  + "sections.associated_with, sections.waitlist_total, "
                  +
                  "sections.min_units, sections.max_units, sections.location, "
                  + "sections.campus, sections.instruction_mode, "
                  + "sections.grading, sections.notes, "
                  + "sections.instructors "
                  + "FROM q, courses LEFT JOIN sections "
                  + "ON courses.id = sections.course_id "
                  + "WHERE courses.id = ANY (?) "
                  + "ORDER BY " + ranking + " DESC";

    var rowStmt = conn.prepareStatement(dataSql);
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

    rowStmt.close();
    return rows;
  }
}
