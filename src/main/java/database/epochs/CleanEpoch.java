package database.epochs;

import database.GetConnection;
import database.generated.Tables;
import database.generated.tables.Epochs;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import nyu.Term;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CleanEpoch {

  private static Logger logger =
      LoggerFactory.getLogger("database.epochs.CleanEpoch");

  public static void cleanEpoch(Term term) {
    try (Connection conn = GetConnection.getConnection()) {
      DSLContext context = DSL.using(conn, SQLDialect.SQLITE);
      Epochs EPOCHS = Tables.EPOCHS;
      context.deleteFrom(EPOCHS)
          .where(EPOCHS.TERM_ID.eq(term.getId()))
          .execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void cleanEpoch(int epoch) {
    try (Connection conn = GetConnection.getConnection()) {
      DSLContext context = DSL.using(conn, SQLDialect.SQLITE);
      Epochs EPOCHS = Tables.EPOCHS;
      context.deleteFrom(EPOCHS).where(EPOCHS.ID.eq(epoch)).execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
