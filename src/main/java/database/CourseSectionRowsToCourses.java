package database;

import api.models.Course;
import api.models.Section;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

public class CourseSectionRowsToCourses {
  public static Stream<Course> courseSectionRowsToCourses(
      Stream<SelectCourseSectionRows.CourseSectionRow> rows) {

    HashMap<Integer, Section> sections = new HashMap<>();
    HashMap<Integer, Course> courses = new HashMap<>();

    List<SelectCourseSectionRows.CourseSectionRow> recitationRecords =
        rows.map(row -> {
              if (!courses.containsKey(row.courseId))
                courses.put(row.courseId,
                            new Course(row.name, row.deptCourseId, row.subject,
                                       new ArrayList<>()));
              if (row.associatedWith == null) {
                Section s = row.getSection();
                sections.put(row.sectionId, s);
                courses.get(row.courseId).getSections().add(s);
              }
              return row;
            })
            .filter(i -> i.associatedWith != null)
            .collect(Collectors.toList());

    recitationRecords.stream().forEach(
        row
        -> sections.get(row.associatedWith).addRecitation(row.getSection()));

    return courses.entrySet().stream().map(entry -> entry.getValue());
  }
}
