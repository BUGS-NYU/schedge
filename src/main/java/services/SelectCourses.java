package services;

import api.models.*;
import database.generated.Tables;
import database.generated.tables.Courses;
import database.generated.tables.Meetings;
import database.generated.tables.Sections;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import nyu.SectionStatus;
import nyu.SectionType;
import nyu.SubjectCode;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectCourses {

  private static Logger logger =
      LoggerFactory.getLogger("services.SelectCourses");

  public static List<Course> selectCourses(Term term, List<SubjectCode> codes) {
    int epoch;
    try (Connection conn = GetConnection.getConnection()) {
      epoch = LatestCompleteEpoch.getLatestEpoch(conn, term);
      if (epoch == -1)
        return Collections.emptyList();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return codes.stream()
        .map(code -> {
          try {
            return selectCourses(term, epoch, code);
          } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
          }
        })
        .flatMap(item -> item.stream())
        .collect(Collectors.toList());
  }

  private static List<Course> selectCourses(Term term, int epoch,
                                            SubjectCode code)
      throws SQLException {
    try (Connection conn = GetConnection.getConnection()) {
      Courses COURSES = Tables.COURSES;
      DSLContext context = DSL.using(conn, SQLDialect.SQLITE);
      Result<Record> records =
          context.select()
              .from(Tables.COURSES)
              .where(COURSES.TERM_ID.eq(term.getId()),
                     COURSES.SUBJECT.eq(code.subject),
                     COURSES.SCHOOL.eq(code.school), COURSES.EPOCH.eq(epoch))
              .fetch();
      ArrayList<Course> courses = new ArrayList<>(records.size());

      for (Record r : records) {
        List<Section> sections = selectSections(context, r.get(COURSES.ID));
        courses.add(new Course(
            r.get(COURSES.NAME), r.get(COURSES.DEPT_COURSE_ID),
            new SubjectCode(r.get(COURSES.SUBJECT), r.get(COURSES.SCHOOL)),
            sections));
      }
      return courses;
    }
  }

  public static List<Section> selectSections(DSLContext context, int courseId) {
    Sections SECTIONS = Tables.SECTIONS;
    Result<Record> records = context.select()
                                 .from(SECTIONS)
                                 .where(SECTIONS.COURSE_ID.eq(courseId))
                                 .fetch();
    Map<Integer, Record> sections = new HashMap<>();
    Map<Integer, ArrayList<Section>> associatedSections = new HashMap<>();
    for (Record r : records) {
      Integer associatedWith = r.get(SECTIONS.ASSOCIATED_WITH);
      if (associatedWith == null) {
        sections.put(r.get(SECTIONS.ID), r);
      } else {
        Section s = new Section(
            r.get(SECTIONS.REGISTRATION_NUMBER), r.get(SECTIONS.SECTION_CODE),
            r.get(SECTIONS.INSTRUCTOR),
            SectionType.values()[r.get(SECTIONS.SECTION_TYPE)],
            SectionStatus.values()[r.get(SECTIONS.SECTION_STATUS)],
            selectMeetings(context, r.get(SECTIONS.ID)), null,
            r.get(SECTIONS.SECTION_NAME), r.get(SECTIONS.WAITLIST_TOTAL),
            r.get(SECTIONS.CAMPUS), r.get(SECTIONS.DESCRIPTION),
            r.get(SECTIONS.MIN_UNITS), r.get(SECTIONS.MAX_UNITS),
            r.get(SECTIONS.INSTRUCTION_MODE), r.get(SECTIONS.GRADING),
            r.get(SECTIONS.ROOM_NUMBER), r.get(SECTIONS.PREREQUISITES));
        if (!associatedSections.containsKey(associatedWith)) {
          associatedSections.put(associatedWith, new ArrayList<>());
        }
        associatedSections.get(associatedWith).add(s);
      }
    }
    return sections.entrySet()
        .stream()
        .map(kv -> {
          int id = kv.getKey();
          Record r = kv.getValue();
          return new Section(
              r.get(SECTIONS.REGISTRATION_NUMBER), r.get(SECTIONS.SECTION_CODE),
              r.get(SECTIONS.INSTRUCTOR),
              SectionType.values()[r.get(SECTIONS.SECTION_TYPE)],
              SectionStatus.values()[r.get(SECTIONS.SECTION_STATUS)],
              selectMeetings(context, id),
              associatedSections.getOrDefault(id, null),
              r.get(SECTIONS.SECTION_NAME), r.get(SECTIONS.WAITLIST_TOTAL),
              r.get(SECTIONS.CAMPUS), r.get(SECTIONS.DESCRIPTION),
              r.get(SECTIONS.MIN_UNITS), r.get(SECTIONS.MAX_UNITS),
              r.get(SECTIONS.INSTRUCTION_MODE), r.get(SECTIONS.GRADING),
              r.get(SECTIONS.ROOM_NUMBER), r.get(SECTIONS.PREREQUISITES));
        })
        .collect(Collectors.toList());
  }

  public static List<Meeting> selectMeetings(DSLContext context,
                                             int sectionId) {
    Meetings MEETINGS = Tables.MEETINGS;
    Result<Record> records = context.select()
                                 .from(MEETINGS)
                                 .where(MEETINGS.SECTION_ID.eq(sectionId))
                                 .fetch();

    return records.stream()
        .map(r
             -> new Meeting(r.get(MEETINGS.BEGIN_DATE).toLocalDateTime(),
                            r.get(MEETINGS.DURATION),
                            r.get(MEETINGS.END_DATE).toLocalDateTime()))
        .collect(Collectors.toList());
  }
}
