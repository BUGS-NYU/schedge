package api.v1;

import api.v1.models.Course;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import database.courses.SelectRows;
import nyu.SubjectCode;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectCourses {

  private static Logger logger =
      LoggerFactory.getLogger("api.v1.SelectCourses");

  public static List<Course> selectCourses(DSLContext context, int epoch,
                                           List<SubjectCode> codes) {
    return codes.stream()
        .flatMap(code -> selectCourses(context, epoch, code))
        .collect(Collectors.toList());
  }

  public static Stream<Course> selectCourses(DSLContext context, int epoch,
                                             SubjectCode code) {
    return RowsToCourses.rowsToCourses(
        SelectRows.selectRows(context, epoch, code));
  }
}
