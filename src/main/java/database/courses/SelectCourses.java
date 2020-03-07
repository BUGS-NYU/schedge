package database.courses;

import api.models.Course;
import nyu.SubjectCode;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectCourses {

  private static Logger logger =
      LoggerFactory.getLogger("database.courses.SelectCourses");

  public static List<Course> selectCourses(DSLContext context, int epoch,
                                           List<SubjectCode> codes) {
    return codes.stream()
        .flatMap(code -> selectCourses(context, epoch, code))
        .collect(Collectors.toList());
  }

  public static Stream<Course> selectCourses(DSLContext context, int epoch,
                                             SubjectCode code) {
    return CourseSectionRowsToCourses.courseSectionRowsToCourses(
        SelectCourseSectionRows.selectCourseSectionRows(context, epoch, code));
  }
}
