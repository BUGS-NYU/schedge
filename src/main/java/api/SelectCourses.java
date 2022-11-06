package api;

import static utils.Nyu.*;

import database.courses.SelectRows;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.*;

public class SelectCourses {
  public static List<Course> selectCourses(Connection conn, Term term,
                                           List<String> codes) {
    return codes.stream()
        .flatMap(code -> selectCourses(conn, term, code))
        .collect(Collectors.toList());
  }

  public static Stream<Course> selectCourses(Connection conn, Term term,
                                             String code) {
    try {
      return RowsToCourses.rowsToCourses(
          SelectRows.selectRows(conn, term, code));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
