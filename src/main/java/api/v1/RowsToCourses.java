package api.v1;

import database.models.*;
import java.util.*;
import java.util.stream.*;
import models.*;

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

    recitationRecords.stream().forEach(row -> {
      Section s = sections.get(row.associatedWith);
      if (s != null)
        s.addRecitation(Section.fromFullRow(row));
      else // Orphans get added to course regardless
        courses.get(row.courseId).getSections().add(Section.fromFullRow(row));
    });

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

    recitationRecords.stream().forEach(row -> {
      Section s = sections.get(row.associatedWith);

      if (s != null)
        s.addRecitation(Section.fromRow(row));
      else // Orphans get added to course regardless
        courses.get(row.courseId).getSections().add(Section.fromRow(row));
    });

    return courses.entrySet().stream().map(entry -> entry.getValue());
  }
}
