package actions;

import static utils.Nyu.*;

import database.*;
import java.util.function.*;
import org.slf4j.*;
import scraping.*;

public class CopyTermFromProduction {
  static final Logger logger =
      LoggerFactory.getLogger("actions.CopyTermFromProduction");

  public enum SchedgeVersion {
    V1,
    V2;
  }

  public static void copyTermFromProduction(SchedgeVersion version, Term term,
                                            Consumer<ScrapeEvent> consumer) {
    GetConnection.withConnection(conn -> {
      long start = System.nanoTime();
      var result = switch (version) {
        case V1 -> ScrapeSchedgeV1.scrapeFromSchedge(term, consumer);
        case V2 -> ScrapeSchedgeV2.scrapeFromSchedge(term, consumer);
      };

      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info("Fetching took {} seconds", duration);
      if (result == null)
        return;

      WriteTerm.writeTerm(conn, result);
    });
  }
}
