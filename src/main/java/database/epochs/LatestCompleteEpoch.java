package database.epochs;

import database.GetConnection;
import database.generated.Tables;
import database.generated.tables.Epochs;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

import static org.jooq.impl.DSL.max;

public final class LatestCompleteEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("database.epochs.LatestCompleteEpoch");

  public static int getLatestEpoch(Connection conn, Term term) {
    DSLContext context = DSL.using(conn, GetConnection.DIALECT);
    Epochs EPOCHS = Tables.EPOCHS;
    Integer e = context.select(max(EPOCHS.ID))
                    .from(EPOCHS)
                    .where(EPOCHS.COMPLETED_AT.isNotNull(),
                           EPOCHS.TERM_ID.eq(term.getId()))
                    .limit(1)
                    .fetchOne()
                    .getValue(max(EPOCHS.ID));

    if (e == null) {
      logger.info("Couldn't find epoch for term=" + term);
      return -1;
    } else {
      logger.info("found epoch=" + e + " for term=" + term);
      return e;
    }
  }
}
