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

  public static List<Course> selectCourses(Connection conn, int epoch,
                                           List<Subject> codes) {
    return codes.stream()
        .flatMap(code -> selectCourses(conn, epoch, code))
        .collect(Collectors.toList());
  }

  public static Stream<Course> selectCourses(Connection conn, int epoch,
                                             Subject code) {
    try {
      return rowsToCourses(SelectRows.selectRows(conn, epoch, code));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<Course> selectFullCourses(Connection conn, int epoch,
                                               List<Subject> codes) {
    return codes.stream()
        .flatMap(code -> selectFullCourses(conn, epoch, code))
        .collect(Collectors.toList());
  }

  public static Stream<Course> selectFullCourses(Connection conn, int epoch,
                                                 Subject code) {
    try {
      return RowsToCourses.fullRowsToCourses(
          SelectRows.selectFullRows(conn, epoch, code));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<Course>
  selectCoursesBySectionId(Connection conn, int epoch, List<Integer> sectionIds)
      throws SQLException {
    return rowsToCourses(selectRowsBySectionId(conn, epoch, sectionIds))
        .collect(Collectors.toList());
  }
}
