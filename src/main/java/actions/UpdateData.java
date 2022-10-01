package actions;

import static types.Nyu.*;
import static utils.TryCatch.*;

import org.slf4j.*;
import utils.*;

public final class UpdateData {

  private static Logger logger = LoggerFactory.getLogger("actions.UpdateData");

  // public static void updateData(int batchSize, int batchSizeSections) {
  //   logger.info("Updating data...");
  //   Term current = Term.getCurrentTerm();
  //   Term next = current.nextTerm();
  //   Term next2 = next.nextTerm();

  //   TryCatch tc = tcNew(e -> logger.error("Failed to update term", e));

  //   logger.info("Updating current term... ({})", current);
  //   tc.log(() -> ScrapeTerm.scrapeTerm(current, batchSize,
  //   batchSizeSections)); logger.info("Updating next term... ({})", next);
  //   tc.log(() -> ScrapeTerm.scrapeTerm(next, batchSize, batchSizeSections));
  //   logger.info("Updating the term after next term... ({})", next2);
  //   tc.log(() -> ScrapeTerm.scrapeTerm(next2, batchSize, batchSizeSections));

  //   Client.close();

  //   logger.info("Done updating.");
  // }
}
