package database.epochs;

import database.GetConnection;
import database.generated.Tables;
import database.generated.tables.Epochs;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import static database.generated.Tables.*;

public final class CleanEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("database.epochs.CleanEpoch");

  public static void cleanIncompleteEpochs(DSLContext context) {
          context.deleteFrom(EPOCHS)
                  .where(EPOCHS.COMPLETED_AT.isNull())
                  .execute();
  }

  public static void cleanEpochsUpTo(DSLContext context, int epoch) {
      context.deleteFrom(EPOCHS).where(EPOCHS.ID.lessThan(epoch)).execute();
  }

  public static void cleanEpochs(DSLContext context, Term term) {
      context.deleteFrom(EPOCHS)
          .where(EPOCHS.TERM_ID.eq(term.getId()))
          .execute();
  }

  public static void cleanEpoch(DSLContext context, int epoch) {
      context.deleteFrom(EPOCHS).where(EPOCHS.ID.eq(epoch)).execute();
  }
}
