package services;

import database.generated.Tables;
import database.generated.tables.Courses;
import models.*;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class SelectCourses {

  public static List<Course> selectCourses(Term term, SubjectCode code)
      throws SQLException {
    try (Connection conn = GetConnection.getConnection()) {
      Courses COURSES = Tables.COURSES;
      DSLContext context = DSL.using(conn, SQLDialect.POSTGRES);
      Result<Record> records = context.select()
                                   .from(Tables.COURSES)
                                   .where(COURSES.TERM_ID.eq(term.getId()))
                                   .fetch();
      ArrayList<Course> courses = new ArrayList<>(records.size());

      for (Record r : records) {
        List<Section> sections = selectSections(r.get(COURSES.ID));
        courses.add(new Course(
            r.get(COURSES.NAME), r.get(COURSES.DEPT_COURSE_NUMBER),
            new SubjectCode(r.get(COURSES.SCHOOL), r.get(COURSES.SUBJECT)),
            sections));
      }
      return courses;
    }
  }

  public static List<Section> selectSections(int courseId) { return null; }
}
