package services;

import database.generated.Tables;
import database.generated.tables.Courses;
import models.*;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SelectCourses {
  public static List<Course> selectCourses(Logger logger, Term term,
                                           List<SubjectCode> codes) {
    return codes.stream()
        .map(code -> {
          try {
            return selectCourses(logger, term, code);
          } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
          }
        })
        .flatMap(item -> item.stream())
        .collect(Collectors.toList());
  }

  public static List<Course> selectCourses(Logger logger, Term term,
                                           SubjectCode code)
      throws SQLException {
    try (Connection conn = GetConnection.getConnection()) {
      Courses COURSES = Tables.COURSES;
      DSLContext context = DSL.using(conn, SQLDialect.POSTGRES);
      Result<Record> records = context.select()
                                   .from(Tables.COURSES)
                                   .where(COURSES.TERM_ID.eq(term.getId()),
                                          COURSES.SUBJECT.eq(code.getSubject()),
                                          COURSES.SCHOOL.eq(code.getSchool()))
                                   .fetch();
      ArrayList<Course> courses = new ArrayList<>(records.size());

      for (Record r : records) {
        List<Section> sections =
            selectSections(logger, context, r.get(COURSES.ID));
        courses.add(new Course(
            r.get(COURSES.NAME), r.get(COURSES.DEPT_COURSE_NUMBER),
            new SubjectCode(r.get(COURSES.SUBJECT), r.get(COURSES.SCHOOL)),
            sections));
      }
      return courses;
    }
  }

  public static List<Section> selectSections(Logger logger, DSLContext context,
                                             int courseId) {
    return new ArrayList<>();
  }

  public static List<Section>
  selectRecitations(Logger logger, DSLContext context, int courseId) {
    return new ArrayList<>();
  }

  public static List<Meeting> selectMeetings(Logger logger, DSLContext context,
                                             int sectionId) {
    return new ArrayList<>();
  }
}
