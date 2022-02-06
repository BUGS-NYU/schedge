package actions;

import database.Epoch;
import database.GetConnection;

import java.sql.SQLException;
import org.slf4j.*;
import types.Term;

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

      Epoch.cleanEpochsUpTo(conn, current);
      Epoch.cleanEpochsUpTo(conn, current.prevTerm());
      Epoch.cleanEpochsUpTo(conn, next);
      Epoch.cleanEpochsUpTo(conn, next.nextTerm());
    });
  }
}
