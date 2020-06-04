package actions;

import database.GetConnection;
import database.courses.InsertCourses;
import database.courses.UpdateSections;
import database.epochs.CompleteEpoch;
import database.epochs.GetNewEpoch;
import database.models.SectionID;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import nyu.SubjectCode;
import nyu.Term;
import scraping.ScrapeCatalog;

public class ScrapeTerm {

  public static void scrapeTerm(Term term, Integer batchSize,
                                Integer batchSizeSections) {
    scrapeTerm(term, batchSize, batchSizeSections, i -> i);
  }

  public static void
  scrapeTerm(Term term, Integer batchSize, Integer batchSizeSections,
             Function<List<SubjectCode>, Iterable<SubjectCode>> f) {
    GetConnection.withContext(context -> {
      int epoch = GetNewEpoch.getNewEpoch(context, term);
      List<SubjectCode> allSubjects = SubjectCode.allSubjects();

      Iterator<SectionID> s =
          ScrapeCatalog.scrapeFromCatalog(term, f.apply(allSubjects), batchSize)
              .flatMap(
                  courseList
                  -> context.connectionResult(
                      (conn)
                          -> InsertCourses
                                 .insertCourses(conn, term, epoch, courseList)
                                 .stream()))
              .iterator();

      UpdateSections.updateSections(context, term, s, batchSizeSections);

      CompleteEpoch.completeEpoch(context, term, epoch);
    });
  }
}
