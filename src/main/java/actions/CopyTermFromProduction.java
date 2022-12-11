package actions;

import static scraping.TermScrapeResult.*;
import static utils.ArrayJS.*;
import static utils.Nyu.*;

import database.*;
import java.util.function.Consumer;
import org.slf4j.*;
import scraping.*;

public class CopyTermFromProduction {
  static final Logger logger =
      LoggerFactory.getLogger("actions.CopyTermFromProduction");
  public enum SchedgeVersion {
    V1,
    V2;
  }

  public static void
  copyTermFromProduction(SchedgeVersion version, Term term,
                         Consumer<TermScrapeResult.ScrapeEvent> consumer) {
    GetConnection.withConnection(conn -> {
      long start = System.nanoTime();
      var result = run(() -> {
        switch (version) {
        case V1:
          return ScrapeSchedgeV1.scrapeFromSchedge(term, consumer);
        case V2:
          return ScrapeSchedgeV2.scrapeFromSchedge(term, consumer);

        default:
          return null;
        }
      });

      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info("Fetching took {} seconds", duration);
      if (result == null)
        return;

      UpdateSchools.updateSchoolsForTerm(conn, term, result.getSchools());
      InsertCourses.clearPrevious(conn, term);
      InsertCourses.insertCourses(conn, term, result.getCourses());
    });
  }
}
