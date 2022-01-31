package actions;

import database.GetConnection;
import database.epochs.CleanEpoch;
import java.sql.SQLException;
import types.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanData {

  interface BiFunction<T, E, R> {
    R apply(T t, E e) throws SQLException;
  }

  private static Logger logger = LoggerFactory.getLogger("actions.CleanData");
  public static void cleanData() {
    logger.info("Cleaning data...");

    GetConnection.withConnection(conn -> {
      Term current = Term.getCurrentTerm();
      Term next = current.nextTerm();

      CleanEpoch.cleanEpochsUpTo(conn, current);
      CleanEpoch.cleanEpochsUpTo(conn, current.prevTerm());
      CleanEpoch.cleanEpochsUpTo(conn, next);
      CleanEpoch.cleanEpochsUpTo(conn, next.nextTerm());
    });
  }
}
