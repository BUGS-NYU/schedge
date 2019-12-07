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

/**
 * This class insert courses into the Postgresql database based on
 * the data scraped from Albert Mobile
 */
public class InsertCourses {
  public static void insertCourses(Logger logger, Term term,
                                   List<Course> courses) throws SQLException {
    try (Connection conn = GetConnection.getConnection()) {
      DSLContext context = DSL.using(conn, SQLDialect.POSTGRES);
      Courses COURSES = Tables.COURSES;
      for (Course c : courses) {
        context.transaction(config -> {
          DSLContext ctx = DSL.using(config);
          int id = ctx.insertInto(COURSES, COURSES.NAME, COURSES.SCHOOL,
                                  COURSES.SUBJECT, COURSES.DEPT_COURSE_ID,
                                  COURSES.TERM_ID)
                       .values(c.getName(), c.getSchool(), c.getSubject(),
                               c.getDeptCourseId(), term.getId())
                       .onDuplicateKeyUpdate()
                       .set(COURSES.NAME, c.getName())
                       .returning(COURSES.ID)
                       .fetchOne()
                       .getValue(COURSES.ID);

          insertSections(logger, ctx, id, c.getSections());
        });
      }
    }
  }

  public static void insertSections(Logger logger, DSLContext context,
                                    int courseId, List<Section> sections)
      throws SQLException {
    Sections SECTIONS = Tables.SECTIONS;
    context.delete(SECTIONS).where(SECTIONS.COURSE_ID.eq(courseId)).execute();

    for (Section s : sections) {
      int id = context
                   .insertInto(SECTIONS, SECTIONS.REGISTRATION_NUMBER,
                               SECTIONS.COURSE_ID, SECTIONS.SECTION_CODE,
                               SECTIONS.INSTRUCTOR, SECTIONS.SECTION_TYPE,
                               SECTIONS.SECTION_STATUS)
                   .values(s.getRegistrationNumber(), courseId,
                           s.getSectionCode(), s.getInstructor(),
                           s.getType().ordinal(), s.getStatus().ordinal())
                   .returning(SECTIONS.ID)
                   .fetchOne()
                   .getValue(SECTIONS.ID);
      insertMeetings(logger, context, id, s.getMeetings());
      if (s.getRecitations() != null)
        insertRecitations(logger, context, courseId, s.getRecitations(), id);
    }
  }

  public static void insertRecitations(Logger logger, DSLContext context,
                                       int courseId, List<Section> sections,
                                       int associatedWith) throws SQLException {
    for (Section s : sections) {
      if (s.getType() == SectionType.LEC)
        throw new IllegalArgumentException(
            "Associated section was a lecture for some reason");
      if (s.getRecitations() != null)
        throw new IllegalArgumentException(
            "Associated section had associated sections for some reason.");

      Sections SECTIONS = Tables.SECTIONS;

      int id =
          context
              .insertInto(SECTIONS, SECTIONS.REGISTRATION_NUMBER,
                          SECTIONS.COURSE_ID, SECTIONS.SECTION_CODE,
                          SECTIONS.INSTRUCTOR, SECTIONS.SECTION_TYPE,
                          SECTIONS.SECTION_STATUS, SECTIONS.ASSOCIATED_WITH)
              .values(s.getRegistrationNumber(), courseId, s.getSectionCode(),
                      s.getInstructor(), s.getType().ordinal(),
                      s.getStatus().ordinal(), associatedWith)
              .returning(SECTIONS.ID)
              .fetchOne()
              .getValue(SECTIONS.ID);
      insertMeetings(logger, context, id, s.getMeetings());
    }
  }

  public static void insertMeetings(Logger logger, DSLContext context,
                                    int sectionId, List<Meeting> meetings)
      throws SQLException {
    Meetings MEETINGS = Tables.MEETINGS;
    context.delete(MEETINGS).where(MEETINGS.SECTION_ID.eq(sectionId)).execute();

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
