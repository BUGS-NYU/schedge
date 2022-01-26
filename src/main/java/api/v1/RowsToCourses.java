package api.v1;

import api.v1.models.Course;
import api.v1.models.Section;
import database.models.FullRow;
import database.models.Row;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Abbrev for Course Section Rows
public class RowsToCourses {

  public static Stream<Course> fullRowsToCourses(Stream<FullRow> rows) {
    HashMap<Integer, Section> sections = new HashMap<>();
    HashMap<Integer, Course> courses = new HashMap<>();

    List<FullRow> recitationRecords =
        rows.map(row -> {
              if (!courses.containsKey(row.courseId))
                courses.put(row.courseId,
                            new Course(row.name, row.deptCourseId,
                                       row.description, row.subject,
                                       new ArrayList<>()));
              if (row.associatedWith == null) {
                Section s = Section.fromFullRow(row);
                sections.put(row.sectionId, s);
                courses.get(row.courseId).getSections().add(s);
              }
              return row;
            })
            .filter(i -> i.associatedWith != null)
            .collect(Collectors.toList());

    recitationRecords.stream().forEach(
        row
        -> sections.get(row.associatedWith)
               .addRecitation(Section.fromFullRow(row)));

    return courses.entrySet().stream().map(entry -> entry.getValue());
  }

  public static Stream<Course> rowsToCourses(Stream<Row> rows) {

    HashMap<Integer, Section> sections = new HashMap<>();
    HashMap<Integer, Course> courses = new HashMap<>();

    List<Row> recitationRecords =
        rows.map(row -> {
              if (!courses.containsKey(row.courseId))
                courses.put(row.courseId,
                            new Course(row.name, row.deptCourseId, null,
                                       row.subject, new ArrayList<>()));
              if (row.associatedWith == null) {
                Section s = Section.fromRow(row);
                sections.put(row.sectionId, s);
                courses.get(row.courseId).getSections().add(s);
              }
              return row;
            })
            .filter(i -> i.associatedWith != null)
            .collect(Collectors.toList());

    recitationRecords.stream().forEach(
        row
        -> sections.get(row.associatedWith)
               .addRecitation(Section.fromRow(row)));

    return courses.entrySet().stream().map(entry -> entry.getValue());
  }
}
