package actions;

import static utils.TryCatch.*;

import types.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Client;
import utils.TryCatch;

public final class UpdateData {

  private static Logger logger = LoggerFactory.getLogger("actions.UpdateData");

  public static void updateData(Integer batchSize, Integer batchSizeSections) {
    logger.info("Updating data...");
    Term current = Term.getCurrentTerm();
    Term next = current.nextTerm();
    Term next2 = next.nextTerm();

    TryCatch tc = tcNew(e -> logger.error("Failed to update term", e));

    logger.info("Updating current term... ({})", current);
    tc.log(() -> ScrapeTerm.scrapeTerm(current, batchSize, batchSizeSections));
    logger.info("Updating next term... ({})", next);
    tc.log(() -> ScrapeTerm.scrapeTerm(next, batchSize, batchSizeSections));
    logger.info("Updating the term after next term... ({})", next2);
    tc.log(() -> ScrapeTerm.scrapeTerm(next2, batchSize, batchSizeSections));

    Client.close();

    logger.info("Done updating.");
  }
}
