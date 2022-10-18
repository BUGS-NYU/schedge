package api;

import static api.RowsToCourses.rowsToCourses;
import static database.courses.SelectRows.selectRowsBySectionId;
import static utils.Nyu.*;

import database.courses.SelectRows;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.*;
import org.slf4j.*;

public class SelectCourses {

  private static Logger logger =
      LoggerFactory.getLogger("api.v1.SelectCourses");

  public static List<Course> selectCourses(Connection conn, Term term,
                                           List<String> codes) {
    return codes.stream()
        .flatMap(code -> selectCourses(conn, term, code))
        .collect(Collectors.toList());
  }

  public static Stream<Course> selectCourses(Connection conn, Term term,
                                             String code) {
    try {
      return rowsToCourses(SelectRows.selectRows(conn, term, code));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

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

  public static List<Course>
  selectCoursesBySectionId(Connection conn, Term term, List<Integer> sectionIds)
      throws SQLException {
    return rowsToCourses(selectRowsBySectionId(conn, term, sectionIds))
        .collect(Collectors.toList());
  }
}
