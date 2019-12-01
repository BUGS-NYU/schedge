package services;

import database.generated.tables.Courses;
import database.generated.tables.Sections;
import database.generated.tables.Meetings;
import models.*;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import database.generated.Tables;
import java.sql.Timestamp;

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

        insertSections(logger, context, id, c.getSections(), null);
      }
    }
  }

  public static void insertSections(Logger logger, DSLContext context,
                                    int courseId, List<Section> sections,
                                    Integer associatedWith)
      throws SQLException {
    Sections SECTIONS = Tables.SECTIONS;
    for (Section s : sections) {
      int id;
      try {
        id = context
                 .insertInto(SECTIONS, SECTIONS.REGISTRATION_NUMBER,
                             SECTIONS.COURSE_ID, SECTIONS.SECTION_CODE,
                             SECTIONS.INSTRUCTOR, SECTIONS.SECTION_TYPE,
                             SECTIONS.ASSOCIATED_WITH)
                 .values(s.getRegistrationNumber(), courseId,
                         s.getSectionCode(), s.getInstructor(),
                         s.getType().ordinal(), associatedWith)
                 .returning(SECTIONS.ID)
                 .fetchOne()
                 .getValue(SECTIONS.ID);
      } catch (Exception e) {
        id = context.update(SECTIONS)
                 .set(SECTIONS.INSTRUCTOR, s.getInstructor())
                 .where(
                     SECTIONS.COURSE_ID.eq(courseId),
                     SECTIONS.SECTION_CODE.eq(s.getSectionCode()),
                     SECTIONS.REGISTRATION_NUMBER.eq(s.getRegistrationNumber()))
                 .returning(SECTIONS.ID)
                 .fetchOne()
                 .getValue(SECTIONS.ID);
      }
      insertMeetings(logger, context, id, s.getMeetings());
      if (s.getRecitations() != null)
        insertSections(logger, context, courseId, s.getRecitations(), id);
    }
  }

  public static void insertMeetings(Logger logger, DSLContext context,
                                    int sectionId, List<Meeting> meetings)
      throws SQLException {
    Meetings MEETINGS = Tables.MEETINGS;
    for (Meeting m : meetings) {
      context
          .insertInto(MEETINGS, MEETINGS.SECTION_ID, MEETINGS.BEGIN_DATE,
                      MEETINGS.DURATION, MEETINGS.END_DATE)
          .values(sectionId, new Timestamp(m.getBeginDate().getMillis()),
                  m.getMinutesDuration(),
                  new Timestamp(m.getEndDate().getMillis()))
          .execute();
    }
  }
}
