package services;

import database.generated.Tables;
import database.generated.tables.Courses;
import database.generated.tables.Meetings;
import database.generated.tables.Sections;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.List;
import nyu.SectionType;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.models.*;

/**
 * This class insert courses into the Postgresql database based on
 * the data scraped from Albert Mobile
 */
public class InsertCourses {

  private static Logger logger =
      LoggerFactory.getLogger("services.InsertCourses");
  public static void insertCourses(Term term, List<Course> courses) {
    try (Connection conn = GetConnection.getConnection()) {
      DSLContext context = DSL.using(conn, SQLDialect.SQLITE);
      Courses COURSES = Tables.COURSES;
      for (Course c : courses) {
        context.transaction(config -> {
          DSLContext ctx = DSL.using(config);

          // ctx.delete(COURSES) .where(COURSES.TERM_ID.eq(term.getId()))
          //     .and(COURSES.SUBJECT.eq(c.getSubject()))
          //     .and(COURSES.SCHOOL.eq(c.getSchool()))
          //     .and(COURSES.DEPT_COURSE_ID.eq(c.getDeptCourseId()));

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
          insertSections(ctx, id, c.getSections());
        });
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void insertSections(DSLContext context, int courseId,
                                    List<Section> sections) {
    Sections SECTIONS = Tables.SECTIONS;

    for (Section s : sections) {
      int id =
          context
              .insertInto(SECTIONS, SECTIONS.REGISTRATION_NUMBER,
                          SECTIONS.COURSE_ID, SECTIONS.SECTION_CODE,
                          SECTIONS.INSTRUCTOR, SECTIONS.SECTION_TYPE,
                          SECTIONS.SECTION_STATUS, SECTIONS.WAITLIST_TOTAL)
              .values(s.getRegistrationNumber(), courseId, s.getSectionCode(),
                      s.getInstructor(), s.getType().ordinal(),
                      s.getStatus().ordinal(), s.getWaitlistTotal())
              .onDuplicateKeyUpdate()
              .set(SECTIONS.REGISTRATION_NUMBER, s.getRegistrationNumber())
              .set(SECTIONS.INSTRUCTOR, s.getInstructor())
              .set(SECTIONS.SECTION_TYPE, s.getType().ordinal())
              .set(SECTIONS.SECTION_STATUS, s.getStatus().ordinal())
              .set(SECTIONS.WAITLIST_TOTAL, s.getWaitlistTotal())
              .returning(SECTIONS.ID)
              .fetchOne()
              .getValue(SECTIONS.ID);
      insertMeetings(context, id, s.getMeetings());
      if (s.getRecitations() != null)
        insertRecitations(context, courseId, s.getRecitations(), id);
    }
  }

  public static void insertRecitations(DSLContext context, int courseId,
                                       List<Section> sections,
                                       int associatedWith) {
    for (Section s : sections) {
      if (s.getType() == SectionType.LEC)
        throw new IllegalArgumentException(
            "Associated section was a lecture for some reason");
      if (s.getRecitations() != null)
        throw new IllegalArgumentException(
            "Associated section had associated sections for some reason.");

      Sections SECTIONS = Tables.SECTIONS;

      int id = context
                   .insertInto(SECTIONS, SECTIONS.REGISTRATION_NUMBER,
                               SECTIONS.COURSE_ID, SECTIONS.SECTION_CODE,
                               SECTIONS.INSTRUCTOR, SECTIONS.SECTION_TYPE,
                               SECTIONS.SECTION_STATUS, SECTIONS.WAITLIST_TOTAL,
                               SECTIONS.ASSOCIATED_WITH)
                   .values(s.getRegistrationNumber(), courseId,
                           s.getSectionCode(), s.getInstructor(),
                           s.getType().ordinal(), s.getStatus().ordinal(),
                           s.getWaitlistTotal(), associatedWith)
                   .onDuplicateKeyUpdate()
                   .set(SECTIONS.REGISTRATION_NUMBER, s.getRegistrationNumber())
                   .set(SECTIONS.INSTRUCTOR, s.getInstructor())
                   .set(SECTIONS.SECTION_TYPE, s.getType().ordinal())
                   .set(SECTIONS.SECTION_STATUS, s.getStatus().ordinal())
                   .set(SECTIONS.WAITLIST_TOTAL, s.getWaitlistTotal())
                   .set(SECTIONS.ASSOCIATED_WITH, associatedWith)
                   .returning(SECTIONS.ID)
                   .fetchOne()
                   .getValue(SECTIONS.ID);
      insertMeetings(context, id, s.getMeetings());
    }
  }

  public static void insertMeetings(DSLContext context, int sectionId,
                                    List<Meeting> meetings) {
    Meetings MEETINGS = Tables.MEETINGS;
    context.delete(MEETINGS).where(MEETINGS.SECTION_ID.eq(sectionId)).execute();

    for (Meeting m : meetings) {
      context
          .insertInto(MEETINGS, MEETINGS.SECTION_ID, MEETINGS.BEGIN_DATE,
                      MEETINGS.DURATION, MEETINGS.END_DATE)
          .values(sectionId,
                  Timestamp.from(m.getBeginDate().toInstant(ZoneOffset.UTC)),
                  m.getMinutesDuration(),
                  Timestamp.from(m.getEndDate().toInstant(ZoneOffset.UTC)))
          .execute();
    }
  }
}
