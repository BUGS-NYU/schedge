package actions;

import static utils.TryCatch.*;

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

    logger.info("Updating current term... ({})", currentTerm);
    tcLogVoid(logger, ScrapeTerm::scrapeTerm, currentTerm, batchSize,
              batchSizeSections);
    logger.info("Updating next term... ({})", nextTerm);
    tcLogVoid(logger, ScrapeTerm::scrapeTerm, nextTerm, batchSize,
              batchSizeSections);
    logger.info("Updating the term after next term... ({})", nextNextTerm);
    tcLogVoid(logger, ScrapeTerm::scrapeTerm, nextNextTerm, batchSize,
              batchSizeSections);

    GetClient.close();

    logger.info("Done updating.");
  }
}
