package database.epochs;

import static database.generated.Tables.EPOCHS;
import static org.jooq.impl.DSL.max;

import nyu.Term;
import org.jooq.DSLContext;
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
