package actions;

import static database.InsertCourses.*;
import static database.UpdateSchools.*;
import static utils.Nyu.*;

import java.sql.*;
import java.util.function.*;
import scraping.*;

public class WriteTerm {

  public static void writeTerm(Connection conn, TermScrapeResult data)
      throws SQLException {
    var term = data.term;
    clearPrevious(conn, term);

    updateSchoolsForTerm(conn, term, data.getSchools());

    while (data.hasNext()) {
      var coursesBatch = data.next();
      insertCourses(conn, term, coursesBatch);
    }
  }

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

  // @Note: bar is nullable
  public static void scrapeSchedgeV1Term(Connection conn, Term term,
                                         Consumer<ScrapeEvent> consumer)
      throws SQLException {
    clearPrevious(conn, term);
    var termData = ScrapeSchedgeV1.scrapeFromSchedge(term, consumer);
    updateSchoolsForTerm(conn, term, termData.getSchools());

    while (termData.hasNext()) {
      var coursesBatch = termData.next();
      insertCourses(conn, term, coursesBatch);
    }
  }
}
