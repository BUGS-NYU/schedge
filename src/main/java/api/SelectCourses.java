package api;

import static api.RowsToCourses.rowsToCourses;
import static database.courses.SelectRows.selectRowsBySectionId;

import database.courses.SelectRows;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.*;
import org.slf4j.*;
import types.*;

public class SelectCourses {

  private static Logger logger =
      LoggerFactory.getLogger("api.v1.SelectCourses");

  public static List<Nyu.Course> selectCourses(Connection conn, Nyu.Term term,
                                               List<String> codes) {
    return codes.stream()
        .flatMap(code -> selectCourses(conn, term, code))
        .collect(Collectors.toList());
  }

  public static Stream<Nyu.Course> selectCourses(Connection conn, Nyu.Term term,
                                                 String code) {
    try {
      return rowsToCourses(SelectRows.selectRows(conn, term, code));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<Nyu.Course> selectFullCourses(Connection conn, Nyu.Term term,
                                                   List<String> codes) {
    return codes.stream()
        .flatMap(code -> selectFullCourses(conn, term, code))
        .collect(Collectors.toList());
  }

  public static Stream<Nyu.Course> selectFullCourses(Connection conn, Nyu.Term term,
                                                     String code) {
    try {
      return RowsToCourses.fullRowsToCourses(
          SelectRows.selectFullRows(conn, term, code));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<Nyu.Course>
  selectCoursesBySectionId(Connection conn, Nyu.Term term, List<Integer> sectionIds)
      throws SQLException {
    return rowsToCourses(selectRowsBySectionId(conn, term, sectionIds))
        .collect(Collectors.toList());
  }
}
