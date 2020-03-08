package actions;

import static database.epochs.LatestCompleteEpoch.getLatestEpoch;

import database.GetConnection;
import database.epochs.CleanEpoch;
import java.util.function.BiFunction;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import search.GetResources;
import utils.Utils;

public class CleanData {

  private static Logger logger = LoggerFactory.getLogger("actions.CleanData");
  public static void cleanData() {

    logger.info("Cleaning data...");

    Term current = Term.getCurrentTerm();
    Term prev = current.prevTerm();
    Term next = current.nextTerm();
    Term nextNext = current.nextTerm();

    Integer maxDeletableEpoch = GetConnection.withContextReturning(context -> {
      BiFunction<Integer, Term, Integer> updateMin = (curMin, curTerm) -> {
        Integer cur = null;
        if (curMin == null ||
            ((cur = getLatestEpoch(context, curTerm)) != null &&
             cur < curMin)) {
          return cur;
        } else
          return curMin;
      };

      Integer min = null;
      min = updateMin.apply(min, current);
      min = updateMin.apply(min, prev);
      min = updateMin.apply(min, next);
      min = updateMin.apply(min, nextNext);

      return min == null ? null : min - 1;
    });

    if (maxDeletableEpoch == null) {
      logger.info("No dead epochs found.");
      return;
    }

    logger.info("Youngest dead epoch is epoch=" + maxDeletableEpoch);

    GetConnection.withContext(
        context -> CleanEpoch.cleanEpochsUpTo(context, maxDeletableEpoch));

    int currentEpoch = maxDeletableEpoch;
    while (Utils.deleteFile(GetResources.getIndexFileForEpoch(currentEpoch--)))
      ;
  }
}
