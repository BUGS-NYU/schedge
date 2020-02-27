package services;

import database.generated.Tables;
import database.generated.tables.Courses;
import database.generated.tables.Meetings;
import database.generated.tables.Sections;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import models.*;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
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
      LoggerFactory.getLogger("services.insert_courses");
  public static void insertCourses(Term term, List<Course> courses)
      throws SQLException {
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
          insertSections(ctx, id, c.getSections());
        });
      }
    }
  }

  public static void insertSections(DSLContext context, int courseId,
                                    List<Section> sections)
      throws SQLException {
    Sections SECTIONS = Tables.SECTIONS;

    for (Section s : sections) {
      int id =
          context
              .insertInto(SECTIONS, SECTIONS.REGISTRATION_NUMBER,
                          SECTIONS.COURSE_ID, SECTIONS.SECTION_CODE,
                          SECTIONS.INSTRUCTOR, SECTIONS.SECTION_TYPE,
                          SECTIONS.SECTION_STATUS, SECTIONS.SECTION_NAME,
                          SECTIONS.WAITLIST_TOTAL, SECTIONS.MIN_UNITS,
                          SECTIONS.MAX_UNITS, SECTIONS.CAMPUS,
                          SECTIONS.DESCRIPTION, SECTIONS.INSTRUCTION_MODE,
                          SECTIONS.GRADING, SECTIONS.ROOM_NUMBER,
                          SECTIONS.PREREQUISITES)
              .values(s.getRegistrationNumber(), courseId, s.getSectionCode(),
                      s.getInstructor(), s.getType().ordinal(),
                      s.getStatus().ordinal(), s.getSectionName(),
                      s.getWaitlistTotal(), s.getMinUnits(), s.getMaxUnits(),
                      s.getCampus(), s.getDescription(), s.getInstructionMode(),
                      s.getGrading(), s.getRoomNumber(), s.getPrerequisites())
              .onDuplicateKeyUpdate()
              .set(SECTIONS.REGISTRATION_NUMBER, s.getRegistrationNumber())
              .set(SECTIONS.INSTRUCTOR, s.getInstructor())
              .set(SECTIONS.SECTION_TYPE, s.getType().ordinal())
              .set(SECTIONS.SECTION_STATUS, s.getStatus().ordinal())
              .set(SECTIONS.SECTION_NAME, s.getSectionName())
              .set(SECTIONS.WAITLIST_TOTAL, s.getWaitlistTotal())
              .set(SECTIONS.MIN_UNITS, s.getMinUnits())
              .set(SECTIONS.MAX_UNITS, s.getMaxUnits())
              .set(SECTIONS.CAMPUS, s.getCampus())
              .set(SECTIONS.DESCRIPTION, s.getDescription())
              .set(SECTIONS.INSTRUCTION_MODE, s.getInstructionMode())
              .set(SECTIONS.GRADING, s.getGrading())
              .set(SECTIONS.ROOM_NUMBER, s.getRoomNumber())
              .set(SECTIONS.PREREQUISITES, s.getPrerequisites())
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
                          SECTIONS.SECTION_STATUS, SECTIONS.ASSOCIATED_WITH,
                          SECTIONS.SECTION_NAME, SECTIONS.WAITLIST_TOTAL,
                          SECTIONS.MIN_UNITS, SECTIONS.MAX_UNITS,
                          SECTIONS.CAMPUS, SECTIONS.DESCRIPTION,
                          SECTIONS.INSTRUCTION_MODE, SECTIONS.GRADING,
                          SECTIONS.ROOM_NUMBER, SECTIONS.PREREQUISITES)
              .values(s.getRegistrationNumber(), courseId, s.getSectionCode(),
                      s.getInstructor(), s.getType().ordinal(),
                      s.getStatus().ordinal(), associatedWith,
                      s.getSectionName(), s.getWaitlistTotal(), s.getMinUnits(),
                      s.getMaxUnits(), s.getCampus(), s.getDescription(),
                      s.getInstructionMode(), s.getGrading(), s.getRoomNumber(),
                      s.getPrerequisites())
              .returning(SECTIONS.ID)
              .fetchOne()
              .getValue(SECTIONS.ID);
      insertMeetings(context, id, s.getMeetings());
    }
  }

  public static void insertMeetings(DSLContext context, int sectionId,
                                    List<Meeting> meetings)
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
