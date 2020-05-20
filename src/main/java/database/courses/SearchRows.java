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
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SearchRows {

  private static Logger logger =
      LoggerFactory.getLogger("database.courses.SearchCourses");

  public static Stream<Row> searchRows(DSLContext context, int epoch,
                                       String subject, String school,
                                       int resultSize, String query,
                                       int titleWeight, int descriptionWeight,
                                       int notesWeight, int prereqsWeight) {
    if (resultSize <= 0) {
      throw new IllegalArgumentException("result size must be positive");
    } else if (resultSize > 50)
      resultSize = 50;

    if (titleWeight == 0 && descriptionWeight == 0 && notesWeight == 0 &&
        prereqsWeight == 0) {
      throw new IllegalArgumentException("all of the weights were zero");
    }

    CommonTableExpression<Record1<Object>> with =
        DSL.name("q").fields("query").as(
            DSL.select(DSL.field("plainto_tsquery(?)", query)));

    ArrayList<String> fields = new ArrayList<>();
    ArrayList<String> rankings = new ArrayList<>();
    if (titleWeight != 0) {
      fields.add("courses.name_vec @@ q.query");
      fields.add("sections.name_vec @@ q.query");
      rankings.add(titleWeight + " * ts_rank_cd(courses.name_vec, q.query)");
      rankings.add(titleWeight + " * ts_rank_cd(sections.name_vec, q.query)");
    }
    if (descriptionWeight != 0) {
      fields.add("courses.description_vec @@ q.query");
      rankings.add(descriptionWeight +
                   " * ts_rank_cd(courses.description_vec, q.query)");
    }
    if (notesWeight != 0) {
      fields.add("sections.notes_vec @@ q.query");
      rankings.add(notesWeight + " * ts_rank_cd(sections.notes_vec, q.query)");
    }
    if (prereqsWeight != 0) {
      fields.add("sections.prereqs_vec @@ q.query");
      rankings.add(prereqsWeight +
                   " * ts_rank_cd(sections.prereqs_vec, q.query)");
    }

    if (subject != null)
      subject = subject.toUpperCase();
    if (school != null)
      school = school.toUpperCase();
    String conditionString;
    Object[] objArray;
    if (subject != null && school != null) {
      conditionString =
          ") AND courses.subject = ? AND courses.school = ? AND courses.epoch = ?";
      objArray = new Object[] {school, epoch};
    } else if (subject != null) {
      conditionString = ") AND courses.subject = ? AND courses.epoch = ?";
      objArray = new Object[] {subject, epoch};
    } else if (school != null) {
      conditionString = ") AND courses.school = ? AND courses.epoch = ?";
      objArray = new Object[] {school, epoch};
    } else {
      conditionString = ") AND courses.epoch = ?";
      objArray = new Object[] {epoch};
    }

    List<Integer> result =
        context.with(with)
            .selectDistinct(COURSES.ID)
            .from(DSL.table("q"), SECTIONS)
            .join(COURSES)
            .on(COURSES.ID.eq(SECTIONS.COURSE_ID))
            .where('(' + String.join(" OR ", fields) + conditionString,
                   objArray)
            .limit(50)
            .fetch()
            .getValues(SECTIONS.ID);

    Condition condition = COURSES.ID.in(result);
    Map<Integer, List<Meeting>> meetingsList =
        SelectRows.selectMeetings(context, condition);
    Result<org.jooq.Record> records =
        context.with(with)
            .select(COURSES.asterisk(), SECTIONS.ID,
                    SECTIONS.REGISTRATION_NUMBER, SECTIONS.SECTION_CODE,
                    SECTIONS.SECTION_TYPE, SECTIONS.SECTION_STATUS,
                    SECTIONS.ASSOCIATED_WITH, SECTIONS.WAITLIST_TOTAL,
                    SECTIONS.NAME, SECTIONS.MIN_UNITS, SECTIONS.MAX_UNITS,
                    SECTIONS.LOCATION,
                    groupConcat(
                        coalesce(IS_TEACHING_SECTION.INSTRUCTOR_NAME, ""), ";")
                        .as("section_instructors"))
            .from(DSL.table("q"), COURSES)
            .leftJoin(SECTIONS)
            .on(SECTIONS.COURSE_ID.eq(COURSES.ID))
            .leftJoin(IS_TEACHING_SECTION)
            .on(SECTIONS.ID.eq(IS_TEACHING_SECTION.SECTION_ID))
            .where(condition)
            .groupBy(DSL.field("q.query"), COURSES.ID, SECTIONS.ID)
            .orderBy(DSL.field(String.join(" + ", rankings) + " DESC"))
            .fetch();

    return StreamSupport
        .stream(records.spliterator(),
                false) // @Performance Should this be true?
        .map(r -> new Row(r, meetingsList.get(r.get(SECTIONS.ID))));
  }

  public static Stream<FullRow>
  searchFullRows(DSLContext context, int epoch, String subject, String school,
                 int resultSize, String query, int titleWeight,
                 int descriptionWeight, int notesWeight, int prereqsWeight) {
    if (resultSize <= 0) {
      throw new IllegalArgumentException("result size must be positive");
    } else if (resultSize > 50)
      resultSize = 50;

    if (titleWeight == 0 && descriptionWeight == 0 && notesWeight == 0 &&
        prereqsWeight == 0) {
      throw new IllegalArgumentException("all of the weights were zero");
    }
    CommonTableExpression<Record1<Object>> with =
        DSL.name("q").fields("query").as(
            DSL.select(DSL.field("plainto_tsquery(?)", query)));

    ArrayList<String> fields = new ArrayList<>();
    ArrayList<String> rankings = new ArrayList<>();
    if (titleWeight != 0) {
      fields.add("courses.name_vec @@ q.query");
      fields.add("sections.name_vec @@ q.query");
      rankings.add(titleWeight + " * ts_rank_cd(courses.name_vec, q.query)");
      rankings.add(titleWeight + " * ts_rank_cd(sections.name_vec, q.query)");
    }
    if (descriptionWeight != 0) {
      fields.add("courses.description_vec @@ q.query");
      rankings.add(descriptionWeight +
                   " * ts_rank_cd(courses.description_vec, q.query)");
    }
    if (notesWeight != 0) {
      fields.add("sections.notes_vec @@ q.query");
      rankings.add(notesWeight + " * ts_rank_cd(sections.notes_vec, q.query)");
    }
    if (prereqsWeight != 0) {
      fields.add("sections.prereqs_vec @@ q.query");
      rankings.add(prereqsWeight +
                   " * ts_rank_cd(sections.prereqs_vec, q.query)");
    }

    if (subject != null)
      subject = subject.toUpperCase();
    if (school != null)
      school = school.toUpperCase();
    String conditionString;
    Object[] objArray;
    if (subject != null && school != null) {
      conditionString =
          ") AND courses.subject = ? AND courses.school = ? AND courses.epoch = ?";
      objArray = new Object[] {school, epoch};
    } else if (subject != null) {
      conditionString = ") AND courses.subject = ? AND courses.epoch = ?";
      objArray = new Object[] {subject, epoch};
    } else if (school != null) {
      conditionString = ") AND courses.school = ? AND courses.epoch = ?";
      objArray = new Object[] {school, epoch};
    } else {
      conditionString = ") AND courses.epoch = ?";
      objArray = new Object[] {epoch};
    }

    List<Integer> result =
        context.with(with)
            .selectDistinct(COURSES.ID)
            .from(DSL.table("q"), SECTIONS)
            .join(COURSES)
            .on(COURSES.ID.eq(SECTIONS.COURSE_ID))
            .where('(' + String.join(" OR ", fields) + conditionString,
                   objArray)
            .limit(resultSize)
            .fetch()
            .getValues(SECTIONS.ID);

    // Condition[] conditions =
    //     new Condition[] {COURSES.EPOCH.eq(epoch), COURSES.ID.in(result)};

    Condition condition = COURSES.ID.in(result);
    Map<Integer, List<Meeting>> meetingsList =
        SelectRows.selectMeetings(context, condition);
    Result<org.jooq.Record> records =
        context.with(with)
            .select(COURSES.asterisk(), SECTIONS.asterisk(),
                    groupConcat(
                        coalesce(IS_TEACHING_SECTION.INSTRUCTOR_NAME, ""), ";")
                        .as("section_instructors"))
            .from(DSL.table("q"), COURSES)
            .leftJoin(SECTIONS)
            .on(SECTIONS.COURSE_ID.eq(COURSES.ID))
            .leftJoin(IS_TEACHING_SECTION)
            .on(SECTIONS.ID.eq(IS_TEACHING_SECTION.SECTION_ID))
            .where(condition)
            .groupBy(DSL.field("q.query"), COURSES.ID, SECTIONS.ID)
            .orderBy(DSL.field(String.join(" + ", rankings) + " DESC"))
            .fetch();

    return StreamSupport
        .stream(records.spliterator(),
                false) // @Performance Should this be true?
        .map(r -> new FullRow(r, meetingsList.get(r.get(SECTIONS.ID))));
  }
}
