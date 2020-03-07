package cli;

import api.models.Course;
import cli.templates.OutputFileMixin;
import cli.templates.TermMixin;
import database.GetConnection;
import database.SelectCourseSectionRows;
import database.SelectCoursesBySectionId;
import database.epochs.LatestCompleteEpoch;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import database.models.CourseSectionRow;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import nyu.SubjectCode;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import search.SearchCourses;
import search.UpdateIndex;

@CommandLine.Command(name = "search", synopsisSubcommandLabel = "label")
public final class Search implements Runnable {

  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  private static Logger logger = LoggerFactory.getLogger("cli.Search");

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(),
                                             "Missing required subcommand");
  }

  @CommandLine.
  Command(name = "term", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Update search index",
          description = "Search in term.")
  public void
  term(@CommandLine.Mixin TermMixin termMixin,
       @CommandLine.Option(names = "--result-size",
                           description = "Maximum number of results")
       Integer resultSize,
       @CommandLine.Mixin OutputFileMixin outputFileMixin, String... args) {
    long start = System.nanoTime();
    Term term = termMixin.getTerm();
    GetConnection.withContext(context -> {
      int epoch = LatestCompleteEpoch.getLatestEpoch(context, term);
      List<Integer> result = SearchCourses.searchCourses(
          epoch, String.join(" ", args), resultSize);
      List<Course> courses = SelectCoursesBySectionId.selectCoursesBySectionId(
          context, epoch, result);
      outputFileMixin.writeOutput(courses);
    });
    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  @CommandLine.
  Command(name = "update", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Update search index",
          description = "Update search index based on term and latest epoch.")
  public void
  update(@CommandLine.Mixin TermMixin termMixin) {
    long start = System.nanoTime();
    Term term = termMixin.getTerm();
    ProgressBarBuilder barBuilder =
        new ProgressBarBuilder()
            .setStyle(ProgressBarStyle.ASCII)
            .setConsumer(new ConsoleProgressBarConsumer(System.out));

    GetConnection.withContext(context -> {
      int epoch = LatestCompleteEpoch.getLatestEpoch(context, term);
      Stream<CourseSectionRow> rows =
          StreamSupport
              .stream(ProgressBar.wrap(SubjectCode.allSubjects(), barBuilder)
                          .spliterator(),
                      false)
              .flatMap(code
                       -> SelectCourseSectionRows.selectCourseSectionRows(
                           context, epoch, code));
      UpdateIndex.updateIndex(epoch, rows);
    });
    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }
}
