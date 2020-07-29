package actions;

import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.query.GetClient;

public final class UpdateData {

  private static Logger logger = LoggerFactory.getLogger("actions.UpdateData");

  public static void updateData(Integer batchSize, Integer batchSizeSections) {
    logger.info("Updating data...");
    Term currentTerm = Term.getCurrentTerm();
    Term nextTerm = currentTerm.nextTerm();
    Term nextNextTerm = nextTerm.nextTerm();

    logger.info("Updating current term...");
    ScrapeTerm.scrapeTerm(currentTerm, batchSize, batchSizeSections);
    logger.info("Updating next term...");
    ScrapeTerm.scrapeTerm(nextTerm, batchSize, batchSizeSections);
    logger.info("Updating the term after next term...");
    ScrapeTerm.scrapeTerm(nextNextTerm, batchSize, batchSizeSections);
    GetClient.close();

    logger.info("Done updating.");
  }
}
