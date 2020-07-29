package actions;

import database.GetConnection;
import database.courses.InsertCourses;
import database.courses.UpdateSections;
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

public class ScrapeTerm {

  public static void scrapeTerm(Term term, int batchSize,
                                int batchSizeSections) {
    scrapeTerm(term, batchSize, batchSizeSections, i -> i);
  }

  public static void
  scrapeTerm(Term term, int batchSize, int batchSizeSections,
             Function<List<SubjectCode>, Iterable<SubjectCode>> f) {
    GetConnection.withConnection(conn -> {
      int epoch = GetNewEpoch.getNewEpoch(conn, term);
      List<SubjectCode> allSubjects = SubjectCode.allSubjects();

      Iterator<SectionID> s =
          ScrapeCatalog.scrapeFromCatalog(term, f.apply(allSubjects), batchSize)
              .flatMap(courseList -> {
                try {
                  return InsertCourses
                      .insertCourses(conn, term, epoch, courseList)
                      .stream();
                } catch (SQLException e) {
                  throw new RuntimeException(e);
                }
              })
              .iterator();

      UpdateSections.updateSections(conn, term, s, batchSizeSections);

      CompleteEpoch.completeEpoch(conn, term, epoch);
    });
  }
}
