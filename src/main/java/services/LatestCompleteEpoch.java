package services;

import static org.jooq.impl.DSL.max;

import database.generated.Tables;
import database.generated.tables.Epochs;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LatestCompleteEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("services.LatestCompleteEpoch");

  public static int getLatestEpoch(Connection conn, Term term) {
    DSLContext context = DSL.using(conn, SQLDialect.SQLITE);
    Epochs EPOCHS = Tables.EPOCHS;
    return context.select(max(EPOCHS.ID))
        .from(EPOCHS)
        .where(EPOCHS.COMPLETED_AT.isNotNull())
        .limit(1)
        .fetch()
        .get(0)
        .component1();
  }
}
