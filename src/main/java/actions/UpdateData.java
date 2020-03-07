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
import java.util.Timer;
import java.util.stream.Stream;

import nyu.SubjectCode;
import nyu.Term;
import scraping.ScrapeCatalog;
import search.UpdateIndex;

public final class UpdateData {
  public static void updateData() {
      Term currentTerm = Term.getCurrentTerm();
      Term nextTerm = currentTerm.nextTerm();
      Term nextNextTerm = nextTerm.nextTerm();

      ScrapeTerm.scrapeTerm(currentTerm, 20, 100);
      ScrapeTerm.scrapeTerm(nextTerm, 20, 100);
      ScrapeTerm.scrapeTerm(nextNextTerm, 20, 100);
  }
}
