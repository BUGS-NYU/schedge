package actions;

import static database.InsertCourses.*;
import static database.UpdateSchools.*;
import static utils.Nyu.*;

import database.GetConnection;
import java.sql.*;
import me.tongfei.progressbar.ProgressBar;
import scraping.PeopleSoftClassSearch;

public class ScrapeTerm {

  public static void scrapeTerm(Term term, boolean display) {
    GetConnection.withConnection(conn -> {
      if (!display) {
        scrapeTerm(conn, term, null);
        return;
      }

      try (ProgressBar bar = new ProgressBar("Scrape " + term.json(), -1)) {
        scrapeTerm(conn, term, bar);
      }
    });
  }

  // @Note: bar is nullable
  static void scrapeTerm(Connection conn, Term term, ProgressBar bar)
      throws SQLException {
    clearPrevious(conn, term);

    var termData = PeopleSoftClassSearch.scrapeTerm(term, bar);
    updateSchoolsForTerm(conn, term, termData.getSchools());

    while (termData.hasNext()) {
      var coursesBatch = termData.next();
      insertCourses(conn, term, coursesBatch);
    }
  }
}
