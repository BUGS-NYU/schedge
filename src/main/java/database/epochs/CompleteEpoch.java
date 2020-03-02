package database.epochs;

import database.generated.Tables;
import database.generated.tables.Epochs;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;

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
