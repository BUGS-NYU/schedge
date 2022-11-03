package actions;

import static database.courses.InsertFullCourses.*;
import static utils.Nyu.*;

import database.GetConnection;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import org.asynchttpclient.*;
import scraping.PeopleSoftClassSearch;

public class ScrapeTerm {

  public static void scrapeTerm(Term term)
      throws IOException, ExecutionException, InterruptedException {
    scrapeTerm(term, false);
  }

  public static void scrapeTerm(Term term, boolean display)
      throws IOException, ExecutionException, InterruptedException {
    /* ProgressBarBuilder bar =
    new ProgressBarBuilder()
    .setStyle(ProgressBarStyle.ASCII)
    .setConsumer(new ConsoleProgressBarConsumer(System.out));

    Iterable<String> subjects =
        display ? ProgressBar.wrap(subjectData, bar) : subjectData;
        */

    PeopleSoftClassSearch.CoursesForTerm courses;
    try (AsyncHttpClient client = new DefaultAsyncHttpClient()) {
      var search = new PeopleSoftClassSearch(client);
      courses = search.scrapeTerm(term);
    }

    GetConnection.withConnection(conn -> {
      //
      insertCourses(conn, term, courses.courses);
    });
  }
}
