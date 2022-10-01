package database;

import static types.Nyu.*;

import java.sql.*;
import java.util.*;
import org.slf4j.*;
import utils.Utils;

public final class UpdateSchools {
  private static Logger logger =
      LoggerFactory.getLogger("database.UpdateSubjects");

  private static final String DELETE_TERM =
      "DELETE FROM schools WHERE term = ?";
  private static final String INSERT_SCHOOL =
      "INSERT INTO schools (term, name) VALUES (?, ?)";
  private static final String INSERT_SUBJECT =
      "INSERT INTO subjects (code, name, school) VALUES (?, ?, ?)";

  public static void updateSchoolsForTerm(Connection conn, Term term,
                                          ArrayList<School> schools)
      throws SQLException {
    try (var stmt = conn.prepareStatement(DELETE_TERM);
         var school = conn.prepareStatement(INSERT_SCHOOL);
         var subject = conn.prepareStatement(INSERT_SUBJECT)) {
      stmt.execute();
    }
  }
}
