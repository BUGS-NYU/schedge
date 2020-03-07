package database;

import static database.generated.Tables.*;
import database.generated.tables.Instructors;
import database.generated.tables.IsTeachingSection;
import nyu.SubjectCode;
import org.jooq.DSLContext;
import org.jooq.Record1;

public final class UpsertInstructor {

  public static void upsertInstructor(DSLContext context, SubjectCode subject,
                                      int sectionId, String instructor) {

    Record1<Integer> instructorRecord =
        context.select(INSTRUCTORS.ID)
            .from(INSTRUCTORS)
                .leftJoin(IS_TEACHING_SUBJECT).on(IS_TEACHING_SUBJECT.INSTRUCTOR_ID.eq(INSTRUCTORS.ID))
            .where(IS_TEACHING_SUBJECT.SUBJECT.eq(subject.code),
                    IS_TEACHING_SUBJECT.SCHOOL.eq(subject.school),
                   INSTRUCTORS.NAME.eq(instructor))
            .fetchOne();

    int instructorId;
    if (instructorRecord == null) {
      instructorId = context
                         .insertInto(INSTRUCTORS, INSTRUCTORS.NAME)
                         .values(instructor)
                         .returning(INSTRUCTORS.ID)
                         .fetchOne()
                         .component1();
        context.insertInto(IS_TEACHING_SUBJECT)
                .columns(IS_TEACHING_SUBJECT.INSTRUCTOR_ID,
                        IS_TEACHING_SUBJECT.SUBJECT,
                        IS_TEACHING_SUBJECT.SCHOOL)
                .values(instructorId, subject.code, subject.school)
                .execute();
    } else {
      instructorId = instructorRecord.component1();
    }



    context.insertInto(IS_TEACHING_SECTION)
        .columns(IS_TEACHING_SECTION.INSTRUCTOR_ID,
                 IS_TEACHING_SECTION.SECTION_ID,
                 IS_TEACHING_SECTION.INSTRUCTOR_NAME)
        .values(instructorId, sectionId, instructor)
        .execute();
  }
}
