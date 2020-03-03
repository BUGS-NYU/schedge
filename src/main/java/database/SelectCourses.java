package database;

import api.models.Course;
import api.models.Meeting;
import api.models.Section;
import database.epochs.LatestCompleteEpoch;
import database.generated.Tables;
import database.generated.tables.Courses;
import database.generated.tables.Meetings;
import database.generated.tables.Sections;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import nyu.SectionStatus;
import nyu.SectionType;
import nyu.SubjectCode;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record4;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectCourses {

  private static Logger logger =
      LoggerFactory.getLogger("database.SelectCourses");

  public static List<Course> selectCourses(Term term, List<SubjectCode> codes) {
    try (Connection conn = GetConnection.getConnection()) {
      int epoch = LatestCompleteEpoch.getLatestEpoch(conn, term);
      if (epoch == -1)
        Collections.emptyList();
      return codes.stream()
          .flatMap(code -> selectCourses(conn, term, epoch, code))
          .collect(Collectors.toList());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static Stream<Course> selectCourses(Connection conn, Term term, int epoch,
                                              SubjectCode code) {
    Courses COURSES = Tables.COURSES;
    Sections SECTIONS = Tables.SECTIONS;
    Meetings MEETINGS = Tables.MEETINGS;
      DSLContext context = DSL.using(conn, SQLDialect.SQLITE);

      Stream<Record4<Integer, Timestamp, Long, Timestamp>> meetingRecordStream =
          StreamSupport.stream(context
                                   .select(MEETINGS.SECTION_ID,
                                           MEETINGS.BEGIN_DATE,
                                           MEETINGS.DURATION, MEETINGS.END_DATE)
                                   .from(MEETINGS)
                                   .innerJoin(SECTIONS)
                                   .on(SECTIONS.ID.eq(MEETINGS.SECTION_ID))
                                   .innerJoin(COURSES)
                                   .on(COURSES.ID.eq(SECTIONS.COURSE_ID))
                                   .where(COURSES.TERM_ID.eq(term.getId()),
                                          COURSES.EPOCH.eq(epoch),
                                          COURSES.SCHOOL.eq(code.school),
                                          COURSES.SUBJECT.eq(code.subject))
                                   .groupBy(MEETINGS.SECTION_ID)
                                   .fetch()
                                   .spliterator(),
                               false);

      HashMap<Integer, ArrayList<Meeting>> meetingRows =
          meetingRecordStream.reduce(
              new HashMap<>(),
              (map, r)
                  -> {
                Meeting m = new Meeting(r.component2().toLocalDateTime(),
                                        r.component3(),
                                        r.component4().toLocalDateTime());
                if (map.containsKey(r.component1())) {
                  map.get(r.component1()).add(m);
                } else {
                  ArrayList<Meeting> meetings = new ArrayList<>();
                  meetings.add(m);
                  map.put(r.component1(), meetings);
                }
                return map;
              },
              (map1, map2) -> {
                for (Map.Entry<Integer, ArrayList<Meeting>> entry :
                     map1.entrySet()) {
                  if (map2.containsKey(entry.getKey())) {
                    map2.get(entry.getKey()).addAll(entry.getValue());
                  } else {
                    map2.put(entry.getKey(), entry.getValue());
                  }
                }
                return map2;
              });

      HashMap<Integer, Section> sections = new HashMap<>();
      HashMap<Integer, Course> courses = new HashMap<>();
      List<CourseSectionRow> recitationRecords =
          StreamSupport
              .stream(context.select()
                          .from(COURSES)
                          .innerJoin(SECTIONS)
                          .on(SECTIONS.COURSE_ID.eq(COURSES.ID))
                          .where(COURSES.TERM_ID.eq(term.getId()),
                                 COURSES.EPOCH.eq(epoch),
                                 COURSES.SCHOOL.eq(code.school),
                                 COURSES.SUBJECT.eq(code.subject))
                          .fetch()
                          .spliterator(),
                      false)
              .map(r -> {
                CourseSectionRow row = new CourseSectionRow(r, meetingRows);

                if (!courses.containsKey(row.courseId))
                  courses.put(row.courseId,
                              new Course(row.name, row.deptCourseId,
                                         row.subject, new ArrayList<>()));
                if (row.associatedWith == null) {
                  Section s = row.getSection();
                  sections.put(row.sectionId, s);
                  courses.get(row.courseId).getSections().add(s);
                }
                return row;
              })
              .filter(i -> i.associatedWith != null)
              .collect(Collectors.toList());

      recitationRecords.stream().forEach(
          row
          -> sections.get(row.associatedWith).addRecitation(row.getSection()));

      return courses.entrySet().stream().map(entry -> entry.getValue());
  }

  private static class CourseSectionRow {
    final int courseId;
    final String name;
    final SubjectCode subject;
    final String deptCourseId;

    final int sectionId;
    final int registrationNumber;
    final String sectionCode;
    final String instructor;
    final SectionType sectionType;
    final SectionStatus sectionStatus;
    final Integer associatedWith;
    final Integer waitlistTotal;
    final List<Meeting> meetings;

    final String sectionName;
    final String campus;
    final String description;
    final String instructionMode;
    final Float minUnits;
    final Float maxUnits;
    final String grading;
    final String location;
    final String notes;
    final String prerequisites;

    CourseSectionRow(Record row,
                     HashMap<Integer, ArrayList<Meeting>> meetingRows) {
      Courses COURSES = Tables.COURSES;
      Sections SECTIONS = Tables.SECTIONS;
      courseId = row.get(COURSES.ID);
      name = row.get(COURSES.NAME);
      subject =
          new SubjectCode(row.get(COURSES.SUBJECT), row.get(COURSES.SCHOOL));
      deptCourseId = row.get(COURSES.DEPT_COURSE_ID);

      sectionId = row.get(SECTIONS.ID);
      registrationNumber = row.get(SECTIONS.REGISTRATION_NUMBER);
      sectionCode = row.get(SECTIONS.SECTION_CODE);
      instructor = row.get(SECTIONS.INSTRUCTOR);
      sectionType = SectionType.values()[row.get(SECTIONS.SECTION_TYPE)];
      sectionStatus = SectionStatus.values()[row.get(SECTIONS.SECTION_STATUS)];
      associatedWith = row.get(SECTIONS.ASSOCIATED_WITH);
      meetings = meetingRows.get(row.get(SECTIONS.ID));
      waitlistTotal = row.get(SECTIONS.WAITLIST_TOTAL);
      sectionName = row.get(SECTIONS.SECTION_NAME);
      campus = row.get(SECTIONS.CAMPUS);
      description = row.get(SECTIONS.DESCRIPTION);
      minUnits = row.get(SECTIONS.MIN_UNITS);
      maxUnits = row.get(SECTIONS.MAX_UNITS);
      instructionMode = row.get(SECTIONS.INSTRUCTION_MODE);
      grading = row.get(SECTIONS.GRADING);
      location = row.get(SECTIONS.LOCATION);
      notes = row.get(SECTIONS.NOTES);
      prerequisites = row.get(SECTIONS.PREREQUISITES);
    }

    public Section getSection() {
      return new Section(registrationNumber, sectionCode, instructor,
                         sectionType, sectionStatus, meetings, null,
                         sectionName, waitlistTotal, campus, description,
                         minUnits, maxUnits, instructionMode, grading, location,
                         notes, prerequisites);
    }
  }
}
