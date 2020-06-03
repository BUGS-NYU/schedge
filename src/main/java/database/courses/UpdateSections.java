package database.courses;

import static database.generated.Tables.*;
import static scraping.query.QuerySection.querySectionAsync;

import database.generated.tables.Sections;
import database.instructors.UpsertInstructor;
import database.models.SectionID;
import java.util.*;
import nyu.SubjectCode;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.models.SectionAttribute;
import scraping.parse.ParseSection;
import utils.SimpleBatchedFutureEngine;

/**
 * This class insert courses into the Postgresql database based on
 * the data scraped from Albert Mobile
 */
public class UpdateSections {
  private static Logger logger =
      LoggerFactory.getLogger("database.courses.UpdateSections");

  private static class SaveState {
    SubjectCode code;
    int id;
    int registrationNumber;
    String data;
    SaveState(SubjectCode c, int i, int r, String d) {
      code = c;
      id = i;
      registrationNumber = r;
      data = d;
    }
  }

  public static void updateSections(DSLContext context, Term term,
                                    Iterator<SectionID> sectionIds,
                                    Integer batchSizeNullable) {
    logger.info("updating sections");
    Iterator<SaveState> sectionAttributes = new SimpleBatchedFutureEngine<>(
        sectionIds, batchSizeNullable == null ? 40 : batchSizeNullable,
        (sectionID, __)
            -> querySectionAsync(
                term, sectionID.registrationNumber,
                str
                -> new SaveState(sectionID.subjectCode, sectionID.id,
                                 sectionID.registrationNumber, str)

                    ));

    HashMap<Integer, String> courseDescriptions = new HashMap<>();
    while (sectionAttributes.hasNext()) {
      SaveState save = sectionAttributes.next();

      if (save == null)
        continue;

      SectionAttribute s;
      try {
        s = ParseSection.parse(save.data);
      } catch (NullPointerException e) {
        logger.warn("Parse error on registrationNumber=" +
                    save.registrationNumber);
        throw e;
      }
      if (s == null) {
        logger.warn("Parse error on registrationNumber=" +
                    save.registrationNumber);
        continue;
      }

      logger.debug("Adding section information...");

      for (String i : s.instructors) {
        if (i.equals("Staff"))
          continue;
        UpsertInstructor.upsertInstructor(context, save.code, save.id, i);
      }

      Integer courseId =
          context.update(SECTIONS)
              .set(SECTIONS.NAME, s.sectionName)
              .set(
                  SECTIONS.NAME_VEC,
                  (Object)DSL.field("to_tsvector({0})",
                                    save.code.toString() + ' ' + s.sectionName))
              .set(SECTIONS.CAMPUS, s.campus)
              .set(SECTIONS.INSTRUCTION_MODE, s.instructionMode)
              .set(SECTIONS.MIN_UNITS, s.minUnits)
              .set(SECTIONS.MAX_UNITS, s.maxUnits)
              .set(SECTIONS.LOCATION, s.location)
              .set(SECTIONS.GRADING, s.grading)
              .set(SECTIONS.NOTES, s.notes)
              .set(SECTIONS.NOTES_VEC,
                   (Object)DSL.field("to_tsvector({0})", s.notes))
              .set(SECTIONS.PREREQUISITES, s.prerequisites)
              .set(SECTIONS.PREREQS_VEC,
                   (Object)DSL.field("to_tsvector({0})", s.prerequisites))
              .where(SECTIONS.ID.eq(save.id))
              .returning(SECTIONS.COURSE_ID)
              .fetchOne()
              .get(SECTIONS.COURSE_ID);
      if (!courseDescriptions.containsKey(courseId)) {
        courseDescriptions.put(courseId, s.description);
      }
    }

    for (Map.Entry<Integer, String> entry : courseDescriptions.entrySet()) {
      context.update(COURSES)
          .set(COURSES.DESCRIPTION, entry.getValue())
          .where(COURSES.ID.eq(entry.getKey()))
          .execute();
    }
  }
}
