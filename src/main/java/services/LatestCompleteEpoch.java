package services;

import static org.jooq.impl.DSL.max;

import database.generated.Tables;
import database.generated.tables.Epochs;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.Record;
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
    Record r = context.select(max(EPOCHS.ID))
                   .from(EPOCHS)
                   .where(EPOCHS.COMPLETED_AT.isNotNull())
                   .limit(1)
                   .fetchOne();

    if (r == null)
      return -1;
    else
      return r.getValue(max(EPOCHS.ID));
  }
}
