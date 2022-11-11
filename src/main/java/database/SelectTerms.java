package database;

import static utils.Nyu.*;

import java.sql.*;
import java.util.*;
import org.slf4j.*;

public final class SelectTerms {
  static final String SELECT_TERMS = "SELECT DISTINCT term from schools";
  public static ArrayList<String> selectTerms(Connection conn)
      throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      var rs = stmt.executeQuery(SELECT_TERMS);
      var out = new ArrayList<String>();
      while (rs.next()) {
        var term = rs.getString("term");
        out.add(term);
      }

      return out;
    }
  }
}
