package services;

import database.generated.Tables;
import database.generated.tables.Courses;
import database.generated.tables.Meetings;
import database.generated.tables.Sections;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import models.*;
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
  public static void updateSessions(Term term, Integer batchSizeNullable)
      throws SQLException {
    try (Connection conn = GetConnection.getConnection()) {
      DSLContext context = DSL.using(conn, SQLDialect.POSTGRES);
      Sections SECTIONS = Tables.SECTIONS;

      // List<Integer> registrationNumbers =
      //     (List<Integer>)context.select(SECTIONS.REGISTRATION_NUMBER)
      //         .from(SECTIONS)
      //         .fetch()
      //         .getValues(0);

      // Iterator<SectionAttribute> sectionAttributes =
      //     new SimpleBatchedFutureEngine<>(
      //         registrationNumbers,
      //         batchSizeNullable == null ? 20 : batchSizeNullable,
      //         (n, a) -> new CompletableFuture<>());

      // while (sectionAttributes.hasNext()) {
      //   SectionAttribute s = sectionAttributes.next();
      //   context.update(SECTIONS)
      //       .set(SECTIONS.SECTION_NAME, s.getCourseName())
      //       .set(SECTIONS.CAMPUS, s.getCampus())
      //       .set(SECTIONS.DESCRIPTION, s.getDescription())
      //       .set(SECTIONS.INSTRUCTION_MODE, s.getInstructionMode())
      //       .set(SECTIONS.MIN_UNITS, s.getMinUnits())
      //       .set(SECTIONS.MAX_UNITS, s.getMaxUnits())
      //       .set(SECTIONS.ROOM_NUMBER, s.getRoom())
      //       .set(SECTIONS.GRADING, s.getGrading())
      //       .set(SECTIONS.PREREQUISITES, s.getPrerequisites())
      //       .where(SECTIONS.REGISTRATION_NUMBER.eq(s.getRegistrationNumber()))
      //       .execute();
      // }
    }
  }
}
