package actions;

import static database.InsertCourses.*;
import static database.UpdateSchools.*;
import static utils.Nyu.*;

import database.GetConnection;
import java.sql.*;
import me.tongfei.progressbar.ProgressBar;
import scraping.PeopleSoftClassSearch;

public class ScrapeTerm {
  // @Note: bar is nullable
  public static void scrapeTerm(Connection conn, Term term, ProgressBar bar)
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
