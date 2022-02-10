package api;

import database.models.*;
import java.util.*;
import java.util.stream.*;
import types.*;

public class RowsToCourses {
  public static Stream<Course> fullRowsToCourses(Stream<FullRow> rows) {
    HashMap<Integer, Section> sections = new HashMap<>();
    HashMap<Integer, Course> courses = new HashMap<>();

    List<FullRow> recitationRecords =
        rows.map(row -> {
              if (!courses.containsKey(row.courseId)) {
                Course c = new Course();
                c.name = row.name;
                c.deptCourseId = row.deptCourseId;
                c.description = row.description;
                c.subjectCode = row.subject;
                c.sections = new ArrayList<>();

                courses.put(row.courseId, c);
              }

              if (row.associatedWith == null) {
                Section s = Section.fromFullRow(row);
                sections.put(row.sectionId, s);
                courses.get(row.courseId).sections.add(s);
              }

              return row;
            })
            .filter(i -> i.associatedWith != null)
            .collect(Collectors.toList());

    recitationRecords.stream().forEach(row -> {
      Section s = sections.get(row.associatedWith);

      if (s != null) {
        if (s.recitations == null) {
          s.recitations = new ArrayList<>();
        }

        s.recitations.add(Section.fromRow(row));
        return;
      }

      // Orphans get added to course regardless
      courses.get(row.courseId).sections.add(Section.fromFullRow(row));
    });

    return courses.entrySet().stream().map(entry -> entry.getValue());
  }

  public static Stream<Course> rowsToCourses(Stream<Row> rows) {

    HashMap<Integer, Section> sections = new HashMap<>();
    HashMap<Integer, Course> courses = new HashMap<>();

    List<Row> recitationRecords =
        rows.map(row -> {
              if (!courses.containsKey(row.courseId)) {
                Course c = new Course();
                c.name = row.name;
                c.deptCourseId = row.deptCourseId;
                c.description = null;
                c.subjectCode = row.subject;
                c.sections = new ArrayList<>();

                courses.put(row.courseId, c);
              }

              if (row.associatedWith == null) {
                Section s = Section.fromRow(row);
                sections.put(row.sectionId, s);
                courses.get(row.courseId).sections.add(s);
              }

              return row;
            })
            .filter(i -> i.associatedWith != null)
            .collect(Collectors.toList());

    recitationRecords.stream().forEach(row -> {
      Section s = sections.get(row.associatedWith);

      if (s != null) {
        if (s.recitations == null) {
          s.recitations = new ArrayList<>();
        }

        s.recitations.add(Section.fromRow(row));
        return;
      }

      // Orphans get added to course regardless
      courses.get(row.courseId).sections.add(Section.fromRow(row));
    });

    return courses.entrySet().stream().map(entry -> entry.getValue());
  }
}
