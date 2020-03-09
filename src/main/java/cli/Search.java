package cli;

import api.v1.SelectCoursesBySectionId;
import api.v1.models.Course;
import cli.templates.OutputFileMixin;
import cli.templates.TermMixin;
import database.GetConnection;
import database.epochs.LatestCompleteEpoch;
import java.util.Collections;
import java.util.List;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import search.SearchCourses;

@CommandLine.Command(name = "search", synopsisSubcommandLabel = "label",
                     description = "Search in term.")
public final class Search implements Runnable {

  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  private @CommandLine.Mixin TermMixin termMixin;
  @CommandLine.
  Option(names = "--result-size", description = "Maximum number of results")
  private Integer resultSize;
  private @CommandLine.Mixin OutputFileMixin outputFileMixin;

  @CommandLine.Option(names = "--query", description = "Query to execute.")
  private String query;
  private static Logger logger = LoggerFactory.getLogger("cli.Search");

  @Override
  public void run() {
    long start = System.nanoTime();
    Term term = termMixin.getTerm();
    GetConnection.withContext(context -> {
      Integer epoch = LatestCompleteEpoch.getLatestEpoch(context, term);
      if (epoch == null) {
        logger.warn("No completed epoch for term=" + term);
        outputFileMixin.writeOutput(Collections.emptyList());
        return;
      }

      List<Integer> result =
          SearchCourses.searchCourses(epoch, query, resultSize);
      List<Course> courses = SelectCoursesBySectionId.selectCoursesBySectionId(
          context, epoch, result);
      outputFileMixin.writeOutput(courses);
    });
    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }
}
