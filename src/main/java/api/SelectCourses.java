package api;

import static utils.Nyu.*;

import database.courses.SelectRows;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.*;

public class SelectCourses {
  public static List<Course> selectFullCourses(Connection conn, Term term,
                                               List<String> codes) {
    return codes.stream()
        .flatMap(code -> selectFullCourses(conn, term, code))
        .collect(Collectors.toList());
  }

  public static Stream<Course> selectFullCourses(Connection conn, Term term,
                                                 String code) {
    try {
      return RowsToCourses.fullRowsToCourses(
          SelectRows.selectFullRows(conn, term, code));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
