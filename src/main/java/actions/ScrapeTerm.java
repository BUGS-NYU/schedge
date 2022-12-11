package actions;

import static database.InsertCourses.*;
import static database.UpdateSchools.*;
import static scraping.TermScrapeResult.*;
import static utils.Nyu.*;

import java.sql.*;
import java.util.function.*;
import scraping.PeopleSoftClassSearch;
import scraping.ScrapeSchedgeV1;
import scraping.TermScrapeResult;

public class ScrapeTerm {

  // @Note: bar is nullable
  public static void scrapeTerm(Connection conn, Term term,
                                Consumer<ScrapeEvent> consumer)
      throws SQLException {
    clearPrevious(conn, term);
    var termData = PeopleSoftClassSearch.scrapeTerm(term, consumer);
    updateSchoolsForTerm(conn, term, termData.getSchools());

    while (termData.hasNext()) {
      var coursesBatch = termData.next();
      insertCourses(conn, term, coursesBatch);
    }
  }
}
