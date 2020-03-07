package database;

import static database.generated.Tables.*;

import api.models.Course;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

public class SelectCoursesBySectionId {

  public static List<Course>
  selectCoursesBySectionId(DSLContext context, int epoch,
                           List<Integer> sectionIds) {
    Stream<SelectCourseSectionRows.CourseSectionRow> rows =
        SelectCourseSectionRows.selectCourseSectionRows(
            context, COURSES.EPOCH.eq(epoch), SECTIONS.ID.in(sectionIds));
    return CourseSectionRowsToCourses.courseSectionRowsToCourses(rows).collect(
        Collectors.toList());
  }
}
