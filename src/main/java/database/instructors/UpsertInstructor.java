package database.instructors;

import static database.generated.Tables.*;

import nyu.SubjectCode;
import org.jooq.DSLContext;
import org.jooq.Record1;

public final class UpsertInstructor {

  public static void upsertInstructor(DSLContext context, SubjectCode subject,
                                      int sectionId, String instructor) {

    Record1<Integer> instructorRecord =
        context.select(INSTRUCTORS.ID)
            .from(INSTRUCTORS)
            .where(INSTRUCTORS.SUBJECT.eq(subject.code),
                   INSTRUCTORS.SCHOOL.eq(subject.school),
                   INSTRUCTORS.NAME.eq(instructor))
            .fetchOne();

    int instructorId;
    if (instructorRecord == null) {
      instructorId = context
                         .insertInto(INSTRUCTORS, INSTRUCTORS.NAME,
                                     INSTRUCTORS.SUBJECT, INSTRUCTORS.SCHOOL)
                         .values(instructor, subject.code, subject.school)
                         .returning(INSTRUCTORS.ID)
                         .fetchOne()
                         .component1();
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
