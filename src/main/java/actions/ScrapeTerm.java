package actions;

import static database.courses.SelectCourseSectionRows.*;

import database.GetConnection;
import database.courses.InsertCourses;
import database.courses.UpdateSections;
import database.epochs.CompleteEpoch;
import database.epochs.GetNewEpoch;
import database.models.CourseSectionRow;
import database.models.SectionID;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import nyu.SubjectCode;
import nyu.Term;
import scraping.ScrapeCatalog;
import search.UpdateIndex;

public final class ScrapeTerm {
  public static void scrapeTerm(Term term, Integer batchSize,
                                Integer batchSizeSections) {
    GetConnection.withContext(context -> {
      int epoch = GetNewEpoch.getNewEpoch(context, term);
      List<SubjectCode> allSubjects = SubjectCode.allSubjects();

      Iterator<SectionID> s =
          ScrapeCatalog.scrapeFromCatalog(term, allSubjects, batchSize)
              .flatMap(courseList
                       -> InsertCourses
                              .insertCourses(context, term, epoch, courseList)
                              .stream())
              .iterator();

      UpdateSections.updateSections(context, term, s, batchSizeSections);

      Stream<CourseSectionRow> rows =
          SubjectCode.allSubjects().stream().flatMap(
              code -> selectCourseSectionRows(context, epoch, code));
      UpdateIndex.updateIndex(epoch, rows);

      CompleteEpoch.completeEpoch(context, term, epoch);
    });
  }
}
