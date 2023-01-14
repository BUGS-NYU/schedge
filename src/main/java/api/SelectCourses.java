package api;

import static utils.Nyu.*;
import static utils.Try.*;

import database.SelectRows;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

public class SelectCourses {
  public static List<Course> selectCourses(Connection conn, Term term, List<String> codes) {
    return codes.stream()
        .flatMap(code -> selectCourses(conn, term, code))
        .collect(Collectors.toList());
  }

  public static Stream<Course> selectCourses(Connection conn, Term term, String code) {
    return tcPass(
        () -> {
          var rows = SelectRows.selectRows(conn, term, code);
          return RowsToCourses.rowsToCourses(rows);
        });
  }
}
