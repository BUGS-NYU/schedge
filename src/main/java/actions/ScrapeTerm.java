package actions;

import static database.courses.InsertCourses.*;
import static database.courses.UpdateSections.*;
import static scraping.ScrapeCatalog.*;
import static utils.Utils.*;

import cli.ConsoleProgressBarConsumer;
import database.GetConnection;
import database.models.SectionID;
import java.util.List;
import java.util.function.Function;
import me.tongfei.progressbar.*;
import scraping.models.Course;
import types.*;

public class ScrapeTerm {

  public static void scrapeTerm(Term term, int batchSize,
                                int batchSizeSections) {
    scrapeTerm(term, batchSize, batchSizeSections, false);
  }

  public static void scrapeTerm(Term term, int batchSize, int batchSizeSections,
                                boolean display) {
    ProgressBarBuilder bar =
        new ProgressBarBuilder()
            .setStyle(ProgressBarStyle.ASCII)
            .setConsumer(new ConsoleProgressBarConsumer(System.out));

    List<Subject> subjectData = Subject.allSubjects();
    Iterable<Subject> subjects =
        display ? ProgressBar.wrap(subjectData, bar) : subjectData;

    List<Course> courses = scrapeCatalog(term, subjects, batchSize);

    GetConnection.withConnection(conn -> {
      clearPrevious(conn, term);

      List<SectionID> idData = insertCourses(conn, term, courses);

      Iterable<SectionID> ids =
          display ? ProgressBar.wrap(idData, bar) : idData;

      updateSections(conn, term, ids.iterator(), batchSizeSections);
    });
  }
}
