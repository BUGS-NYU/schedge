package actions;

import static database.epochs.LatestCompleteEpoch.getLatestEpoch;

import database.GetConnection;
import database.epochs.CleanEpoch;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

import java.sql.SQLException;

public class CleanData {


    interface BiFunction<T, E, R> {
        R apply(T t, E e) throws SQLException;
    }

  private static Logger logger = LoggerFactory.getLogger("actions.CleanData");
  public static void cleanData() {

    logger.info("Cleaning data...");

    Term current = Term.getCurrentTerm();
    Term prev = current.prevTerm();
    Term next = current.nextTerm();
    Term nextNext = current.nextTerm();

    Integer minLiveEpoch = GetConnection.withConnectionReturning(conn -> {
      BiFunction<Integer, Term, Integer> updateMin = (curMin, curTerm) -> {
        Integer cur = getLatestEpoch(conn, curTerm);
        if (curMin == null || (cur != null && cur < curMin)) {
          return cur;
        } else
          return curMin;
      };

      Integer min = null;
      min = updateMin.apply(min, current);
      min = updateMin.apply(min, prev);
      min = updateMin.apply(min, next);
      min = updateMin.apply(min, nextNext);

      return min;
    });

    if (minLiveEpoch == null) {
      logger.info("No dead epochs found.");
      return;
    }

    logger.info("Oldest live epoch is epoch=" + minLiveEpoch);

    GetConnection.withConnection(
        conn -> CleanEpoch.cleanEpochsUpTo(conn, minLiveEpoch));
  }
}
