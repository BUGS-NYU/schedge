package actions;
import static database.courses.InsertCourses.*;
import static database.courses.UpdateSections.*;
import static utils.Utils.*;

import cli.ConsoleProgressBarConsumer;
import database.GetConnection;
import database.models.SectionID;
import java.util.*;
import java.util.function.Function;
import me.tongfei.progressbar.*;
import types.*;

public class ScrapeTerm {

  // public static void scrapeTerm(Term term, int batchSize,
  //                               int batchSizeSections) {
  //   scrapeTerm(term, batchSize, batchSizeSections, false);
  // }

  // public static void scrapeTerm(Term term, int batchSize, int
  // batchSizeSections,
  //                               boolean display) {
  //   ProgressBarBuilder bar =
  //       new ProgressBarBuilder()
  //           .setStyle(ProgressBarStyle.ASCII)
  //           .setConsumer(new ConsoleProgressBarConsumer(System.out));

  //   List<Subject> rawSubjectData = Subject.allSubjects();
  //   var subjectData = new ArrayList<String>();
  //   for (var s : rawSubjectData) {
  //     subjectData.add(s.code);
  //   }

  //   Iterable<String> subjects =
  //       display ? ProgressBar.wrap(subjectData, bar) : subjectData;

  //   List<Course> courses = scrapeCatalog(term, subjects, batchSize);

  //   GetConnection.withConnection(conn -> {
  //     clearPrevious(conn, term);

  //     List<SectionID> idData = insertCourses(conn, term, courses);

  //     Iterable<SectionID> ids =
  //         display ? ProgressBar.wrap(idData, bar) : idData;

  //     updateSections(conn, term, ids.iterator(), batchSizeSections);
  //   });
  // }
}
