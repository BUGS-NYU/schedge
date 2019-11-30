package services;

import database.generated.tables.Courses;
import database.generated.tables.Sections;
import models.*;
import org.jooq.DSLContext;
import org.jooq.InsertQuery;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import database.generated.Tables;

public class InsertCourses {
  public static void insertCourses(Logger logger, Term term,
                                   List<Course> courses) throws SQLException {
    try (Connection conn = GetConnection.getConnection()) {
      DSLContext context = DSL.using(conn, SQLDialect.POSTGRES);
      Courses COURSES = Tables.COURSES;
      for (Course c : courses) {
        int id = context
                     .insertInto(COURSES, COURSES.NAME, COURSES.SCHOOL,
                                 COURSES.SUBJECT, COURSES.DEPT_COURSE_NUMBER,
                                 COURSES.TERM_ID)
                     .values(c.getName(), c.getSchool(), c.getSubject(),
                             c.getDeptCourseNumber(), term.getId())
                     .returning(COURSES.ID)
                     .fetchOne()
                     .getValue(COURSES.ID);

        insertSections(logger, context, c.getSections());
      }
    }
  }

  public static void insertSections(Logger logger, DSLContext context,
                                    List<Section> sections)
      throws SQLException {
    Sections SECTIONS = Tables.SECTIONS;
    for (Section s : sections) {
      context.insertInto(SECTIONS, SECTIONS.REGISTRATION_NUMBER,
                         SECTIONS.COURSE_ID, SECTIONS.SECTION_CODE);
    }
  }

  public static void insertMeetings(Logger logger, DSLContext context,
                                    List<Meeting> meetings)
      throws SQLException {}
}
