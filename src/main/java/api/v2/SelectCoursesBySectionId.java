package api.v2;

import api.v2.models.Course;
import org.jooq.DSLContext;

import java.util.List;
import java.util.stream.Collectors;

import static api.v2.CSRowsToCourses.csRowsToCourses;
import static database.courses.SelectCSRsBySectionId.selectCSRsBySectionId;

public class SelectCoursesBySectionId {

  public static List<Course>
  selectCoursesBySectionId(DSLContext context, int epoch,
                           List<Integer> sectionIds) {
    return csRowsToCourses(selectCSRsBySectionId(context, epoch, sectionIds)).collect(
        Collectors.toList());
  }
}
