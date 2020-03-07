package database;

import static database.generated.Tables.*;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.groupConcat;

import api.models.Meeting;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import database.models.CourseSectionRow;
import nyu.SubjectCode;
import org.jooq.*;

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

}
