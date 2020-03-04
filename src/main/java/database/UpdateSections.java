package database;

import database.generated.Tables;
import database.generated.tables.Sections;
import database.models.SectionID;
import nyu.SubjectCode;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.SimpleBatchedFutureEngine;
import scraping.models.SectionAttribute;
import scraping.parse.ParseSection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import static scraping.query.QuerySection.querySectionAsync;

/**
 * This class insert courses into the Postgresql database based on
 * the data scraped from Albert Mobile
 */
public class UpdateSections {
  private static Logger logger =
      LoggerFactory.getLogger("database.UpdateSections");

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

  public static void updateSections(Term term, Iterator<SectionID> sectionIds,
                                    Integer batchSizeNullable) {
    try (Connection conn = GetConnection.getConnection()) {
      DSLContext context = DSL.using(conn, GetConnection.DIALECT);
      Sections SECTIONS = Tables.SECTIONS;

      Iterator<SaveState> sectionAttributes = new SimpleBatchedFutureEngine<>(
          sectionIds, batchSizeNullable == null ? 40 : batchSizeNullable,
          (sectionID, __)
              -> querySectionAsync(
                  term, sectionID.registrationNumber,
                  str
                  -> new SaveState(sectionID.subjectCode, sectionID.id,
                                   sectionID.registrationNumber, str)

                      ));

      while (sectionAttributes.hasNext()) {
        SaveState save = sectionAttributes.next();
        SectionAttribute s = null;
        try {
          s = ParseSection.parse(save.data);

        } catch (NullPointerException e) {
          logger.warn("Parse error on registrationNumber: " +
                      save.registrationNumber);
          throw e;
        }
        if (s == null) {
          logger.warn("Parse error on registrationNumber: " +
                      save.registrationNumber);
          continue;
        }

        logger.debug("Adding section information...");

        for (String i : s.getInstructors()) {
          if (i.equals("Staff"))
            continue;
          UpsertInstructor.upsertInstructor(context, save.code, save.id, i);
        }

        context.update(SECTIONS)
            .set(SECTIONS.SECTION_NAME, s.getSectionName())
            .set(SECTIONS.CAMPUS, s.getCampus())
            .set(SECTIONS.DESCRIPTION, s.getDescription())
            .set(SECTIONS.INSTRUCTION_MODE, s.getInstructionMode())
            .set(SECTIONS.MIN_UNITS, s.getMinUnits())
            .set(SECTIONS.MAX_UNITS, s.getMaxUnits())
            .set(SECTIONS.LOCATION, s.getLocation())
            .set(SECTIONS.GRADING, s.getGrading())
            .set(SECTIONS.NOTES, s.getNotes())
            .set(SECTIONS.PREREQUISITES, s.getPrerequisites())
            .where(SECTIONS.ID.eq(save.id))
            .execute();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
