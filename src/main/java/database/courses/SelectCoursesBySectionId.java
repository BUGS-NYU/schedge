package database.courses;

import static database.generated.Tables.COURSES;
import static database.generated.Tables.SECTIONS;

import api.models.Course;
import database.models.CourseSectionRow;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.DSLContext;

public class SelectCoursesBySectionId {

  public static List<Course>
  selectCoursesBySectionId(DSLContext context, int epoch,
                           List<Integer> sectionIds) {
    Stream<CourseSectionRow> rows =
        SelectCourseSectionRows.selectCourseSectionRows(
            context, COURSES.EPOCH.eq(epoch),
            SECTIONS.ID.in(sectionIds)
                .or(SECTIONS.ASSOCIATED_WITH.in(sectionIds)));
    return CourseSectionRowsToCourses.courseSectionRowsToCourses(rows).collect(
        Collectors.toList());
  }
}
