package database.epochs;

import nyu.Term;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;

import static database.generated.Tables.EPOCHS;

public final class GetNewEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("database.epochs.GetEpoch");

  public static int getNewEpoch(DSLContext context, Term term) {
      return context
              .insertInto(EPOCHS, EPOCHS.STARTED_AT, EPOCHS.COMPLETED_AT,
                      EPOCHS.TERM_ID)
              .values(Timestamp.from(Instant.now()), null, term.getId())
              .returning(EPOCHS.ID)
              .fetchOne()
              .getValue(EPOCHS.ID);
  }
}
