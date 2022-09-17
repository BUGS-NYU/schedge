package database;

import database.models.SectionID;
import java.sql.*;
import java.util.*;
import org.slf4j.*;
import scraping.models.*;
import types.Meeting;
import types.Term;
import utils.Utils;

public class SelectSubjects {
  private static Logger logger =
      LoggerFactory.getLogger("database.SelectSubjects");

  // https://wiki.postgresql.org/wiki/Loose_indexscan
  private static final String selectAllSubjectsQuery =
      "WITH RECURSIVE t AS ("
      + "  (SELECT subject_code FROM courses ORDER BY subject_code LIMIT 1) "
      + "  UNION ALL SELECT "
      +
      "  (SELECT subject_code FROM courses WHERE subject_code > t.subject_code ORDER BY subject_code LIMIT 1) "
      + "  FROM t WHERE t.subject_code IS NOT NULL"
      + ") SELECT subject_code FROM t WHERE subject_code IS NOT NULL";

  // private static final String selectAllSubjectsQuery =
  //     "WITH RECURSIVE t AS ("
  //     + "  (SELECT col FROM tbl ORDER BY col LIMIT 1) "
  //     + "  UNION ALL SELECT "
  //     + "  (SELECT col FROM tbl WHERE col > t.col ORDER BY col LIMIT 1) "
  //     + "  FROM t WHERE t.col IS NOT NULL"
  //     + ") SELECT col FROM t WHERE col IS NOT NULL";
  //
  private static final String selectSubjectsForTermQuery =
      "SELECT code, name FROM subjects WHERE term = ?";

  public static final class Subject {
    String code;
    String name;
  }

  public static ArrayList<Subject> selectSubjectsForTerm(Connection conn,
                                                         Term term)
      throws SQLException {
    PreparedStatement stmt = conn.prepareStatement(selectSubjectsForTermQuery);
    Utils.setArray(stmt, term.json());

    ResultSet rs = stmt.executeQuery();
    ArrayList<Subject> rows = new ArrayList<>();

    while (rs.next()) {
      Subject subject = new Subject();
      subject.code = rs.getString("code");
      subject.name = rs.getString("name");

      rows.add(subject);
    }

    rs.close();
    stmt.close();

    return rows;
  }
}
