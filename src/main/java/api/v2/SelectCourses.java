package api.v2;

import api.v2.models.Course;
import database.courses.SelectCourseSectionRows;
import nyu.SubjectCode;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    return CSRowsToCourses.csRowsToCourses(
        SelectCourseSectionRows.selectCourseSectionRows(context, epoch, code));
  }
}
