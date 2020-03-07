package database.epochs;

import static database.generated.Tables.*;
import static org.jooq.impl.DSL.max;

import database.GetConnection;
import database.generated.Tables;
import database.generated.tables.Epochs;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public final class LatestCompleteEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("database.epochs.LatestCompleteEpoch");

  public static Integer getLatestEpoch(DSLContext context, Term term) {
    Integer e = context.select(max(EPOCHS.ID))
                    .from(EPOCHS)
                    .where(EPOCHS.COMPLETED_AT.isNotNull(),
                           EPOCHS.TERM_ID.eq(term.getId()))
                    .limit(1)
                    .fetchOne()
                    .getValue(max(EPOCHS.ID));

    if (e == null) {
      logger.info("Couldn't find epoch for term=" + term);
    } else {
      logger.info("found epoch=" + e + " for term=" + term);
    }
    return e;
  }
}
