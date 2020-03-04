package database;

import database.generated.Tables;
import database.generated.tables.Courses;
import database.generated.tables.Meetings;
import database.generated.tables.Sections;
import database.models.SectionID;
import nyu.SectionType;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.models.Course;
import scraping.models.Meeting;
import scraping.models.Section;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * This class insert courses into the Postgresql database based on
 * the data scraped from Albert Mobile
 */
public class InsertCourses {

  private static Logger logger =
      LoggerFactory.getLogger("database.InsertCourses");

  public static ArrayList<SectionID> insertCourses(Term term, int epoch,
                                                   List<Course> courses) {
    try (Connection conn = GetConnection.getConnection()) {
      DSLContext context = DSL.using(conn, GetConnection.DIALECT);
      Courses COURSES = Tables.COURSES;

      ArrayList<SectionID> states = new ArrayList<>();
      for (Course c : courses) {
        context.transaction(config -> {
          DSLContext ctx = DSL.using(config);

          int id =
              ctx.insertInto(COURSES, COURSES.EPOCH, COURSES.NAME,
                             COURSES.SCHOOL, COURSES.SUBJECT,
                             COURSES.DEPT_COURSE_ID, COURSES.TERM_ID)
                  .values(epoch, c.getName(), c.getSchool(), c.getSubject(),
                          c.getDeptCourseId(), term.getId())
                  .returning(COURSES.ID)
                  .fetchOne()
                  .getValue(COURSES.ID);
          insertSections(ctx, id, c.getSections(), states);
        });
      }
      return states;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void insertSections(DSLContext context, int courseId,
                                    List<Section> sections,
                                    ArrayList<SectionID> states) {
    Sections SECTIONS = Tables.SECTIONS;

    for (Section s : sections) {
      Record r = context
                     .insertInto(SECTIONS, SECTIONS.REGISTRATION_NUMBER,
                                 SECTIONS.COURSE_ID, SECTIONS.SECTION_CODE,
                                 SECTIONS.SECTION_TYPE, SECTIONS.SECTION_STATUS,
                                 SECTIONS.WAITLIST_TOTAL)
                     .values(s.getRegistrationNumber(), courseId,
                             s.getSectionCode(), s.getType().ordinal(),
                             s.getStatus().ordinal(), s.getWaitlistTotal())
                     .returning(SECTIONS.ID, SECTIONS.REGISTRATION_NUMBER)
                     .fetchOne();

      SectionID state = new SectionID(s.getSubjectCode(), r.get(SECTIONS.ID), s.getRegistrationNumber());
      states.add(state);
      insertMeetings(context, state.id, s.getMeetings());
      if (s.getRecitations() != null)
        insertRecitations(context, courseId, s.getRecitations(), state.id,
                          states);
    }
  }

  public static void insertRecitations(DSLContext context, int courseId,
                                       List<Section> sections,
                                       int associatedWith,
                                       ArrayList<SectionID> states) {
    for (Section s : sections) {
      if (s.getType() == SectionType.LEC)
        throw new IllegalArgumentException(
            "Associated section was a lecture for some reason");
      if (s.getRecitations() != null)
        throw new IllegalArgumentException(
            "Associated section had associated sections for some reason.");

      Sections SECTIONS = Tables.SECTIONS;
      Record r =
          context
              .insertInto(SECTIONS, SECTIONS.REGISTRATION_NUMBER,
                          SECTIONS.COURSE_ID, SECTIONS.SECTION_CODE,
                          SECTIONS.SECTION_TYPE, SECTIONS.SECTION_STATUS,
                          SECTIONS.WAITLIST_TOTAL, SECTIONS.ASSOCIATED_WITH)
              .values(s.getRegistrationNumber(), courseId, s.getSectionCode(),
                      s.getType().ordinal(), s.getStatus().ordinal(),
                      s.getWaitlistTotal(), associatedWith)
              .returning(SECTIONS.ID, SECTIONS.REGISTRATION_NUMBER)
              .fetchOne();
        SectionID state = new SectionID(s.getSubjectCode(), r.get(SECTIONS.ID), s.getRegistrationNumber());
      states.add(state);
      insertMeetings(context, state.id, s.getMeetings());
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
