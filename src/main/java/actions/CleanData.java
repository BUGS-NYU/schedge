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

    Integer minLiveEpoch = GetConnection.withContextReturning(context -> {
      BiFunction<Integer, Term, Integer> updateMin = (curMin, curTerm) -> {
        Integer cur = getLatestEpoch(context, curTerm);
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

    GetConnection.withContext(
        context -> CleanEpoch.cleanEpochsUpTo(context, minLiveEpoch));

    int currentEpoch = minLiveEpoch - 1;
    while (Utils.deleteFile(GetResources.getIndexFileForEpoch(currentEpoch--)))
      ;
  }
}
