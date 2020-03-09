package api.v1;

import static api.v1.RowsToCourses.rowsToCourses;
import static database.courses.SelectRowsBySectionId.selectRowsBySectionId;

import api.v1.models.Course;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.DSLContext;

public class SelectCoursesBySectionId {

  public static List<Course>
  selectCoursesBySectionId(DSLContext context, int epoch,
                           List<Integer> sectionIds) {
    return rowsToCourses(selectRowsBySectionId(context, epoch, sectionIds))
        .collect(Collectors.toList());
  }
}
