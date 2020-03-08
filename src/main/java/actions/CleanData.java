package actions;

import static database.epochs.LatestCompleteEpoch.getLatestEpoch;

import database.GetConnection;
import database.epochs.CleanEpoch;
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
      Integer min = null;
      Integer cur = null;

      if (min == null ||
          ((cur = getLatestEpoch(context, prev)) != null && cur < min)) {
        min = cur;
      }

      if (min == null ||
          ((cur = getLatestEpoch(context, current)) != null && cur < min)) {
        min = cur;
      }

      if (min == null ||
          ((cur = getLatestEpoch(context, next)) != null && cur < min)) {
        min = cur;
      }

      if (min == null ||
          ((cur = getLatestEpoch(context, nextNext)) != null && cur < min)) {
        min = cur;
      }

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
