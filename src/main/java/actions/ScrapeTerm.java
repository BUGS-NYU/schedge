package actions;

import static database.UpdateSchools.*;
import static database.InsertCourses.*;
import static utils.Nyu.*;

import database.GetConnection;
import java.io.IOException;
import java.util.concurrent.*;
import me.tongfei.progressbar.ProgressBar;
import org.asynchttpclient.*;
import scraping.PeopleSoftClassSearch;

public class ScrapeTerm {

  public static void scrapeTerm(Term term, boolean display)
      throws IOException, ExecutionException, InterruptedException {
    if (!display) {
      scrapeTerm(term, null);
      return;
    }

    try (ProgressBar bar = new ProgressBar("Scraper", -1)) {
      scrapeTerm(term, bar);
    }
  }

  // @Note: bar is nullable
  static void scrapeTerm(Term term, ProgressBar bar)
      throws IOException, ExecutionException, InterruptedException {
    try (AsyncHttpClient client = new DefaultAsyncHttpClient()) {
      var search = new PeopleSoftClassSearch(client);
      var termData = search.scrapeTerm(term, bar);

      GetConnection.withConnection(conn -> {
        updateSchoolsForTerm(conn, term, termData.schools);
        insertCourses(conn, term, termData.courses);
      });
    }
  }
}
