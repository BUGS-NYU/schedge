package api.v1;

import api.v1.models.Course;
import api.v1.models.Section;
import database.models.Row;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Abbrev for Course Section Rows
public class CSRowsToCourses {
  public static Stream<Course>
  csRowsToCourses(Stream<Row> rows) {

    HashMap<Integer, Section> sections = new HashMap<>();
    HashMap<Integer, Course> courses = new HashMap<>();

    List<Row> recitationRecords =
        rows.map(row -> {
              if (!courses.containsKey(row.courseId))
                courses.put(row.courseId,
                            new Course(row.name, row.deptCourseId, row.subject,
                                       new ArrayList<>()));
              if (row.associatedWith == null) {
                Section s = Section.fromCSR(row);
                sections.put(row.sectionId, s);
                courses.get(row.courseId).getSections().add(s);
              }
              return row;
            })
            .filter(i -> i.associatedWith != null)
            .collect(Collectors.toList());

    recitationRecords.stream().forEach(
        row
        -> sections.get(row.associatedWith).addRecitation(Section.fromCSR(row)));

    return courses.entrySet().stream().map(entry -> entry.getValue());
  }
}
