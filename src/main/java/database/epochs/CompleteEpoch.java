package database.epochs;

import database.generated.Tables;
import database.generated.tables.Courses;
import database.generated.tables.Epochs;
import database.generated.tables.Meetings;
import database.generated.tables.Sections;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import nyu.SectionType;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.models.*;

public final class CompleteEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("database.epochs.CompleteEpoch");

  public static void completeEpoch(Connection conn, Term term, int id) {
    DSLContext context = DSL.using(conn, SQLDialect.SQLITE);
    Epochs EPOCHS = Tables.EPOCHS;
    context.update(EPOCHS)
        .set(EPOCHS.COMPLETED_AT, Timestamp.from(Instant.now()))
        .where(EPOCHS.ID.eq(id))
        .and(EPOCHS.TERM_ID.eq(term.getId()))
        .execute();
  }
}
