package database.courses;

import static database.generated.Tables.*;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.groupConcat;

import database.models.Row;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

  public static Stream<Row>
  selectRows(DSLContext context, int epoch, SubjectCode code) {
    return selectRows(context, COURSES.EPOCH.eq(epoch),
                                   COURSES.SCHOOL.eq(code.school),
                                   COURSES.SUBJECT.eq(code.code));
  }
  public static Stream<Row>
  selectRows(DSLContext context, Condition... conditions) {
    long start = System.nanoTime();
    HashMap<Integer, ArrayList<Meeting>> meetingRows =
        getMeetingsMap(context, conditions);
    long meetings = System.nanoTime();
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
    long end = System.nanoTime();
    logger.info((meetings - start) / 1000000 +
                " milliseconds for meetings database");
    logger.info((end - start) / 1000000 + " milliseconds for database");

    return StreamSupport
        .stream(records.spliterator(),
                false) // @Performance Should this be true?
        .map(r -> new Row(r, meetingRows));
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
}
