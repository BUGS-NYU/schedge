package database;

import static utils.Nyu.*;

import java.sql.*;
import java.util.*;
import utils.Utils;

public final class UpdateSchools {
  private static final String DELETE_TERM =
      "DELETE FROM schools WHERE term = ?";
  private static final String INSERT_SCHOOL =
      "INSERT INTO schools (term, name, code) VALUES (?, ?, ?) RETURNING id";
  private static final String INSERT_SUBJECT =
      "INSERT INTO subjects (code, name, school, term) VALUES (?, ?, ?, ?)";

  public static void updateSchoolsForTerm(Connection conn, Term term,
                                          ArrayList<School> schools)
      throws SQLException {
    try (var delete = conn.prepareStatement(DELETE_TERM);
         var schoolInsert = conn.prepareStatement(INSERT_SCHOOL);
         var subjectInsert = conn.prepareStatement(INSERT_SUBJECT)) {
      var termString = term.json();
      Utils.setArray(delete, termString);
      delete.execute();

      for (var school : schools) {
        // @TODO: Remove this call to `Utils.nullable` once the field is no
        // longer nullable
        Utils.setArray(schoolInsert, termString, school.name,
                       Utils.nullable(Types.VARCHAR, school.code));
        schoolInsert.execute();

        int id;
        {
          var rs = schoolInsert.getResultSet();
          if (!rs.next()) {
            throw new RuntimeException("no result from INSERT ... RETURNING");
          }

          id = rs.getInt(1);
        }

        for (var subject : school.subjects) {
          Utils.setArray(subjectInsert, subject.code(), subject.name(), id,
                         termString);
          subjectInsert.addBatch();
        }

        subjectInsert.executeBatch();
      }
    }
  }
}
