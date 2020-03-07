package database;

import static database.generated.Tables.*;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.groupConcat;

import api.models.Course;
import api.models.Meeting;
import api.models.Section;
import database.generated.Tables;
import database.generated.tables.*;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import nyu.SectionStatus;
import nyu.SectionType;
import nyu.SubjectCode;
import nyu.Term;
import org.jooq.*;
import org.jooq.impl.DSL;

public class SelectCourseSectionRows {

  public static Stream<CourseSectionRow>
  selectCourseSectionRows(DSLContext context, int epoch, SubjectCode code) {
    return selectCourseSectionRows(context, COURSES.EPOCH.eq(epoch),
                                   COURSES.SCHOOL.eq(code.school),
                                   COURSES.SUBJECT.eq(code.code));
  }
  public static Stream<CourseSectionRow>
  selectCourseSectionRows(DSLContext context, Condition... conditions) {
    HashMap<Integer, ArrayList<Meeting>> meetingRows =
        getMeetingsMap(context, conditions);
    Result<Record> records =
        context
            .select(COURSES.asterisk(), SECTIONS.asterisk(),
                    groupConcat(
                        coalesce(IS_TEACHING_SECTION.INSTRUCTOR_NAME, ""), ";")
                        .as("section_instructors"))
            .from(COURSES)
            .leftJoin(SECTIONS)
            .on(SECTIONS.COURSE_ID.eq(COURSES.ID))
            .leftJoin(IS_TEACHING_SECTION)
            .on(SECTIONS.ID.eq(IS_TEACHING_SECTION.SECTION_ID))
            .where(conditions)
            .groupBy(SECTIONS.ID)
            .fetch();

    return StreamSupport
        .stream(records.spliterator(),
                false) // @Performance Should this be true?
        .map(r -> new CourseSectionRow(r, meetingRows));
  }

  private static HashMap<Integer, ArrayList<Meeting>>
  getMeetingsMap(DSLContext context, Condition... conditions) {
    Stream<Record4<Integer, Timestamp, Long, Timestamp>> meetingRecordStream =
        StreamSupport.stream(context
                                 .select(MEETINGS.SECTION_ID,
                                         MEETINGS.BEGIN_DATE, MEETINGS.DURATION,
                                         MEETINGS.END_DATE)
                                 .from(MEETINGS)
                                 .innerJoin(SECTIONS)
                                 .on(SECTIONS.ID.eq(MEETINGS.SECTION_ID))
                                 .innerJoin(COURSES)
                                 .on(COURSES.ID.eq(SECTIONS.COURSE_ID))
                                 .where(conditions)
                                 .groupBy(MEETINGS.SECTION_ID)
                                 .fetch()
                                 .spliterator(),
                             false); // @Performance Should this be true?

    return meetingRecordStream.reduce(
        new HashMap<>(),
        (map, r)
            -> {
          Meeting m =
              new Meeting(r.component2().toLocalDateTime(), r.component3(),
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
          for (Map.Entry<Integer, ArrayList<Meeting>> entry : map1.entrySet()) {
            if (map2.containsKey(entry.getKey())) {
              map2.get(entry.getKey()).addAll(entry.getValue());
            } else {
              map2.put(entry.getKey(), entry.getValue());
            }
          }
          return map2;
        });
  }

  public static class CourseSectionRow {
    public final int courseId;
    public final String name;
    public final SubjectCode subject;
    public final String deptCourseId;

    public final int sectionId;
    public final int registrationNumber;
    public final String sectionCode;
    public final String[] instructors;
    public final SectionType sectionType;
    public final SectionStatus sectionStatus;
    public final Integer associatedWith;
    public final Integer waitlistTotal;
    public final List<Meeting> meetings;

    public final String sectionName;
    public final String campus;
    public final String description;
    public final String instructionMode;
    public final Float minUnits;
    public final Float maxUnits;
    public final String grading;
    public final String location;
    public final String notes;
    public final String prerequisites;

    CourseSectionRow(Record row,
                     HashMap<Integer, ArrayList<Meeting>> meetingRows) {
      Courses COURSES = Tables.COURSES;
      Sections SECTIONS = Tables.SECTIONS;
      Instructors INSTRUCTORS = Tables.INSTRUCTORS;
      courseId = row.get(COURSES.ID);
      name = row.get(COURSES.NAME);
      subject =
          new SubjectCode(row.get(COURSES.SUBJECT), row.get(COURSES.SCHOOL));
      deptCourseId = row.get(COURSES.DEPT_COURSE_ID);

      sectionId = row.get(SECTIONS.ID);
      registrationNumber = row.get(SECTIONS.REGISTRATION_NUMBER);
      sectionCode = row.get(SECTIONS.SECTION_CODE);
      String instructorString = (String)row.get("section_instructors");
      instructors = instructorString.equals("") ? new String[] {"Staff"}
                                                : instructorString.split(";");
      sectionType = SectionType.values()[row.get(SECTIONS.SECTION_TYPE)];
      sectionStatus = SectionStatus.values()[row.get(SECTIONS.SECTION_STATUS)];
      associatedWith = row.get(SECTIONS.ASSOCIATED_WITH);
      meetings = meetingRows.get(row.get(SECTIONS.ID));
      waitlistTotal = row.get(SECTIONS.WAITLIST_TOTAL);
      sectionName = row.get(SECTIONS.NAME);
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
      return new Section(registrationNumber, sectionCode, instructors,
                         sectionType, sectionStatus, meetings, null,
                         sectionName, waitlistTotal, campus, description,
                         minUnits, maxUnits, instructionMode, grading, location,
                         notes, prerequisites);
    }
  }
}
