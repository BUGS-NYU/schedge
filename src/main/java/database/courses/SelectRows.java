package database.courses;

import static database.generated.Tables.*;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.groupConcat;

import database.models.FullRow;
import database.models.Row;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import nyu.Meeting;
import nyu.SubjectCode;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectRows {

  private static Logger logger =
      LoggerFactory.getLogger("database.courses.SelectCourseSectionRows");

  public static Stream<Row> selectRows(DSLContext context, int epoch,
                                       SubjectCode code) {
    return selectRows(context, COURSES.EPOCH.eq(epoch),
                      COURSES.SCHOOL.eq(code.school),
                      COURSES.SUBJECT.eq(code.code));
  }

  public static Stream<Row> selectRows(DSLContext context,
                                       Condition... conditions) {
    Map<Integer, List<Meeting>> meetingsList =
        selectMeetings(context, conditions);
    Result<Record> records =
        context
            .select(COURSES.asterisk(), SECTIONS.ID,
                    SECTIONS.REGISTRATION_NUMBER, SECTIONS.SECTION_CODE,
                    SECTIONS.SECTION_TYPE, SECTIONS.SECTION_STATUS,
                    SECTIONS.ASSOCIATED_WITH, SECTIONS.WAITLIST_TOTAL,
                    SECTIONS.NAME, SECTIONS.MIN_UNITS, SECTIONS.MAX_UNITS,
                    SECTIONS.LOCATION,
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
        .map(r -> new Row(r, meetingsList.get(r.get(SECTIONS.ID))));
  }

  public static Stream<FullRow> selectFullRows(DSLContext context, int epoch,
                                               SubjectCode code) {
    return selectFullRows(context, COURSES.EPOCH.eq(epoch),
                          COURSES.SCHOOL.eq(code.school),
                          COURSES.SUBJECT.eq(code.code));
  }

  public static Stream<FullRow> selectFullRows(DSLContext context,
                                               Condition... conditions) {
    Map<Integer, List<Meeting>> meetingsList =
        selectMeetings(context, conditions);
    Result<Record> records =
        context
            .select(COURSES.asterisk(), SECTIONS.asterisk(),
                    groupConcat(
                        coalesce(IS_TEACHING_SECTION.INSTRUCTOR_NAME, ""), ";")
                        .as("section_instructors"),
                    groupConcat(MEETINGS.BEGIN_DATE, ";").as("begin_dates"),
                    groupConcat(MEETINGS.DURATION, ";").as("durations"),
                    groupConcat(MEETINGS.END_DATE, ";").as("end_dates"))
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
        .map(r -> new FullRow(r, meetingsList.get(r.get(SECTIONS.ID))));
  }

  private static Map<Integer, List<Meeting>>
  selectMeetings(DSLContext context, Condition... conditions) {
    Result<Record4<Integer, String, String, String>> records =
        context
            .select(SECTIONS.ID,
                    groupConcat(MEETINGS.BEGIN_DATE, ";").as("begin_dates"),
                    groupConcat(MEETINGS.DURATION, ";").as("durations"),
                    groupConcat(MEETINGS.END_DATE, ";").as("end_dates"))
            .from(COURSES)
            .leftJoin(SECTIONS)
            .on(SECTIONS.COURSE_ID.eq(COURSES.ID))
            .leftJoin(MEETINGS)
            .on(SECTIONS.ID.eq(MEETINGS.SECTION_ID))
            .where(conditions)
            .groupBy(SECTIONS.ID)
            .fetch();

    return records.stream().collect(Collectors.toMap(
        row
        -> row.component1(),
        row
        -> meetingList(row.component2(), row.component3(), row.component4())));
  }

  private static List<Meeting>
  meetingList(String beginString, String durationString, String endString) {
    if (beginString == null)
      return Collections.emptyList();
    String[] beginDates = beginString.split(";");
    String[] durations = durationString.split(";");
    String[] endDates = endString.split(";");
    ArrayList<Meeting> meetings = new ArrayList<>(beginDates.length);
    for (int i = 0; i < beginDates.length; i++) {
      meetings.add(new Meeting(beginDates[i], durations[i], endDates[i]));
    }
    return meetings;
  }
}
