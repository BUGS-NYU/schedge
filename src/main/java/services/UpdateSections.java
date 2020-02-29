package services;

import static scraping.query.QuerySection.*;

import database.generated.Tables;
import database.generated.tables.Sections;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.SimpleBatchedFutureEngine;
import scraping.models.*;

/**
 * This class insert courses into the Postgresql database based on
 * the data scraped from Albert Mobile
 */
public class UpdateSections {
  private static Logger logger =
      LoggerFactory.getLogger("services.UpdateSections");

  private static class SaveState {
    int id;
    int registrationNumber;
    String data;
    SaveState(int i, int r, String d) {
      id = i;
      registrationNumber = r;
      data = d;
    }
  }

  public static void updateSections(Term term, Integer batchSizeNullable)
      throws SQLException {
    try (Connection conn = GetConnection.getConnection()) {
      DSLContext context = DSL.using(conn, SQLDialect.POSTGRES);
      Sections SECTIONS = Tables.SECTIONS;

      Iterator<SaveState> registrationNumbers =
          StreamSupport
              .stream(context.select(SECTIONS.ID, SECTIONS.REGISTRATION_NUMBER)
                          .from(SECTIONS)
                          .fetch()
                          .spliterator(),
                      false)
              .map(record
                   -> new SaveState(record.get(SECTIONS.ID),
                                    record.get(SECTIONS.REGISTRATION_NUMBER),
                                    null))
              .iterator();

      Iterator<SaveState> sectionAttributes = new SimpleBatchedFutureEngine<>(
          registrationNumbers,
          batchSizeNullable == null ? 40 : batchSizeNullable,
          (saveState,
           __) -> querySectionAsync(term, saveState.registrationNumber, str -> {
            saveState.data = str;
            return saveState;
          }));

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

        logger.info("Adding section information...");

        context.update(SECTIONS)
            .set(SECTIONS.SECTION_NAME, s.getCourseName())
            .set(SECTIONS.CAMPUS, s.getCampus())
            .set(SECTIONS.DESCRIPTION, s.getDescription())
            .set(SECTIONS.INSTRUCTION_MODE, s.getInstructionMode())
            .set(SECTIONS.MIN_UNITS, s.getMinUnits())
            .set(SECTIONS.MAX_UNITS, s.getMaxUnits())
            .set(SECTIONS.ROOM_NUMBER, s.getRoom())
            .set(SECTIONS.GRADING, s.getGrading())
            .set(SECTIONS.PREREQUISITES, s.getPrerequisites())
            .where(SECTIONS.ID.eq(save.id))
            .execute();
      }
    }
  }
}
