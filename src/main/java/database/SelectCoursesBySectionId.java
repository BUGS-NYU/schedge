package database;

import static database.generated.Tables.*;

import api.models.Course;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import database.models.CourseSectionRow;
import org.jooq.DSLContext;

public class SelectCoursesBySectionId {

  public static List<Course>
  selectCoursesBySectionId(DSLContext context, int epoch,
                           List<Integer> sectionIds) {
    Stream<CourseSectionRow> rows =
        SelectCourseSectionRows.selectCourseSectionRows(
            context, COURSES.EPOCH.eq(epoch), SECTIONS.ID.in(sectionIds));
    return CourseSectionRowsToCourses.courseSectionRowsToCourses(rows).collect(
        Collectors.toList());
  }
}
