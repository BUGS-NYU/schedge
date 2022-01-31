package actions;

import static database.courses.InsertCourses.*;
import static database.courses.UpdateSections.*;

import database.GetConnection;
import database.epochs.CompleteEpoch;
import database.epochs.GetNewEpoch;
import database.models.SectionID;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import nyu.SubjectCode;
import nyu.Term;
import scraping.ScrapeCatalog;
import scraping.models.Course;

public class ScrapeTerm {

  public static void scrapeTerm(Term term, int batchSize,
                                int batchSizeSections) {
    scrapeTerm(term, batchSize, batchSizeSections, i -> i);
  }

  public static void
  scrapeTerm(Term term, int batchSize, int batchSizeSections,
             Function<List<SubjectCode>, Iterable<SubjectCode>> f) {
    List<SubjectCode> allSubjects = SubjectCode.allSubjects();
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
