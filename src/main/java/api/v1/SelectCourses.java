package api.v1;

import api.v1.models.Course;
import database.courses.SelectRows;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nyu.SubjectCode;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static api.v1.RowsToCourses.rowsToCourses;
import static database.courses.SelectRows.selectRowsBySectionId;

public class SelectCourses {

  private static Logger logger =
      LoggerFactory.getLogger("api.v1.SelectCourses");

  public static List<Course> selectCourses(Connection conn, int epoch,
                                           List<SubjectCode> codes) {
    return codes.stream()
        .flatMap(code -> selectCourses(conn, epoch, code))
        .collect(Collectors.toList());
  }

  public static Stream<Course> selectCourses(Connection conn, int epoch,
                                             SubjectCode code) {
    try {
      return rowsToCourses(
          SelectRows.selectRows(conn, epoch, code));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<Course> selectFullCourses(Connection conn, int epoch,
                                               List<SubjectCode> codes) {
    return codes.stream()
        .flatMap(code -> selectFullCourses(conn, epoch, code))
        .collect(Collectors.toList());
  }

  public static Stream<Course> selectFullCourses(Connection conn, int epoch,
                                                 SubjectCode code) {
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
