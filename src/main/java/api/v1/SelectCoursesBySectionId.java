package api.v1;

import api.v1.models.Course;

import java.util.List;
import java.util.stream.Collectors;

import org.jooq.DSLContext;

import static api.v1.CSRowsToCourses.csRowsToCourses;
import static database.courses.SelectCSRsBySectionId.selectCSRsBySectionId;

public class SelectCoursesBySectionId {

  public static List<Course>
  selectCoursesBySectionId(DSLContext context, int epoch,
                           List<Integer> sectionIds) {
    return csRowsToCourses(selectCSRsBySectionId(context, epoch, sectionIds)).collect(
        Collectors.toList());
  }
}
