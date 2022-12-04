package actions;

import static utils.ArrayJS.*;
import static utils.Nyu.*;

import database.*;
import org.slf4j.*;
import scraping.*;

public class CopyTermFromProduction {
  static final Logger logger =
      LoggerFactory.getLogger("actions.CopyTermFromProduction");
  public enum SchedgeVersion {
    V1,
    V2;
  }

  public static void copyTermFromProduction(SchedgeVersion version, Term term) {
    GetConnection.withConnection(conn -> {
      long start = System.nanoTime();
      var result = run(() -> {
        switch (version) {
        case V1:
          return ScrapeSchedgeV1.scrapeFromSchedge(term);
        case V2:
          return ScrapeSchedgeV2.scrapeFromSchedge(term);

        default:
          return null;
        }
      });

      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info("Fetching took {} seconds", duration);
      if (result == null)
        return;

      UpdateSchools.updateSchoolsForTerm(conn, term, result.schools);
      InsertCourses.clearPrevious(conn, term);
      InsertCourses.insertCourses(conn, term, result.courses);
    });
  }
}
