package actions;

import static database.InsertCourses.*;
import static database.UpdateSchools.*;
import static utils.Nyu.*;

import database.GetConnection;
import me.tongfei.progressbar.ProgressBar;
import scraping.PeopleSoftClassSearch;

public class ScrapeTerm {

  public static void scrapeTerm(Term term, boolean display) {
    if (!display) {
      scrapeTerm(term, null);
      return;
    }

    try (ProgressBar bar = new ProgressBar("Scraper", -1)) {
      scrapeTerm(term, bar);
    }
  }

  // @Note: bar is nullable
  static void scrapeTerm(Term term, ProgressBar bar) {
    var search = new PeopleSoftClassSearch();
    var termData = search.scrapeTerm(term, bar);

    GetConnection.withConnection(conn -> {
      updateSchoolsForTerm(conn, term, termData.schools);
      insertCourses(conn, term, termData.courses);
    });
  }
}
