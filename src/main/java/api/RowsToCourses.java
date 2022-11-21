package api;

import static utils.Nyu.*;

import database.models.*;
import java.util.*;
import java.util.stream.*;

public class RowsToCourses {
  public static Stream<Course> rowsToCourses(ArrayList<Row> rows) {
    HashMap<Integer, Section> sections = new HashMap<>();
    HashMap<Integer, Course> courses = new HashMap<>();
    var out = new ArrayList<Course>();

    var recitationRecords = new ArrayList<Row>();
    for (var row : rows) {
      if (!courses.containsKey(row.courseId)) {
        Course c = new Course();
        c.name = row.name;
        c.deptCourseId = row.deptCourseId;
        c.description = row.description;
        c.subjectCode = row.subject;
        c.sections = new ArrayList<>();

        courses.put(row.courseId, c);
        out.add(c);
      }

      if (row.associatedWith == null) {
        Section s = Section.fromRow(row);
        sections.put(row.sectionId, s);
        courses.get(row.courseId).sections.add(s);
      } else {
        recitationRecords.add(row);
      }
    }

    for (var row : recitationRecords) {
      Section s = sections.get(row.associatedWith);

      if (s != null) {
        if (s.recitations == null) {
          s.recitations = new ArrayList<>();
        }

        s.recitations.add(Section.fromRow(row));
        continue;
      }

      // Orphans get added to course regardless
      courses.get(row.courseId).sections.add(Section.fromRow(row));
    }

    return out.stream();
  }
}
