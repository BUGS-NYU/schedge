package actions;

import static database.courses.InsertCourses.*;
import static database.courses.UpdateSections.*;

import database.GetConnection;
import database.epochs.*;
import database.models.SectionID;
import java.util.List;
import java.util.function.Function;
import scraping.ScrapeCatalog;
import scraping.models.Course;
import types.*;

public class ScrapeTerm {

  public static void scrapeTerm(Term term, int batchSize,
                                int batchSizeSections) {
    scrapeTerm(term, batchSize, batchSizeSections, i -> i);
  }

  public static void scrapeTerm(Term term, int batchSize, int batchSizeSections,
                                Function<List<Subject>, Iterable<Subject>> f) {
    List<Subject> allSubjects = Subject.allSubjects();
    List<Course> courses =
        ScrapeCatalog.scrapeCatalog(term, f.apply(allSubjects), batchSize);

    GetConnection.withConnection(conn -> {
      int epoch = GetNewEpoch.getNewEpoch(conn, term);

      List<SectionID> s = insertCourses(conn, term, epoch, courses);

      updateSections(conn, term, s.iterator(), batchSizeSections);

      CompleteEpoch.completeEpoch(conn, term, epoch);
    });
  }
}
