package database.epochs;

import static database.generated.Tables.EPOCHS;

import nyu.Term;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CleanEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("database.epochs.CleanEpoch");

  public static void cleanIncompleteEpochs(DSLContext context) {
    context.deleteFrom(EPOCHS).where(EPOCHS.COMPLETED_AT.isNull()).execute();
  }

  public static void cleanEpochsUpTo(DSLContext context, int epoch) {
    context.deleteFrom(EPOCHS).where(EPOCHS.ID.lessThan(epoch)).execute();
  }

  public static void cleanEpochs(DSLContext context, Term term) {
    context.deleteFrom(EPOCHS).where(EPOCHS.TERM_ID.eq(term.getId())).execute();
  }

  public static void cleanEpoch(DSLContext context, int epoch) {
    context.deleteFrom(EPOCHS).where(EPOCHS.ID.eq(epoch)).execute();
  }
}
