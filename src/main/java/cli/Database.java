package cli;

import api.App;
import cli.templates.OutputFileMixin;
import cli.templates.SubjectCodeMixin;
import cli.templates.TermMixin;
import database.GetConnection;
import database.InsertCourses;
import database.SelectCourses;
import database.UpdateSections;
import database.epochs.CleanEpoch;
import database.epochs.CompleteEpoch;
import database.epochs.GetEpoch;
import database.models.SectionID;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import nyu.SubjectCode;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import scraping.ScrapeCatalog;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

@CommandLine.Command(name = "db", synopsisSubcommandLabel =
                                      "(scrape | query | update | serve)")
public class Database implements Runnable {
  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  private static Logger logger = LoggerFactory.getLogger("cli.Database");

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(),
                                             "Missing required subcommand");
  }

  @CommandLine.Command(
      name = "scrape", sortOptions = false, headerHeading = "Usage:%n%n",
      synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n", header = "Scrape section from db",
      description =
          "Scrape section based on term and registration number, OR school and subject from db")
  public void
  scrape(@CommandLine.Mixin TermMixin termMixin,
         @CommandLine.
         Option(names = "--batch-size-catalog",
                description = "batch size for querying the catalog")
         Integer batchSize,
         @CommandLine.Option(names = "--batch-size-sections",
                             description = "batch size for querying sections")
         Integer batchSizeSections) {
    Term term = termMixin.getTerm();
    long start = System.nanoTime();
    int epoch;
    try (Connection conn = GetConnection.getConnection()) {

      epoch = GetEpoch.getEpoch(conn, term);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    List<SubjectCode> allSubjects = SubjectCode.allSubjects();
    ProgressBarBuilder barBuilder =
        new ProgressBarBuilder()
            .setStyle(ProgressBarStyle.ASCII)
            .setConsumer(new ConsoleProgressBarConsumer(System.out));
    Iterator<SectionID> s =
        ScrapeCatalog
            .scrapeFromCatalog(term, ProgressBar.wrap(allSubjects, barBuilder),
                               batchSize)
            .flatMap(courseList
                     -> InsertCourses.insertCourses(term, epoch, courseList)
                            .stream())
            .iterator();
    UpdateSections.updateSections(term, s, batchSizeSections);

    try (Connection conn = GetConnection.getConnection()) {
      CompleteEpoch.completeEpoch(conn, term, epoch);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  @CommandLine.Command(
      name = "query", sortOptions = false, headerHeading = "Usage:%n%n",
      synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n", header = "Query section",
      description =
          "Query section based on term and registration number, OR school and subject from db")
  public void
  query(@CommandLine.Mixin TermMixin termMixin,
        @CommandLine.Mixin SubjectCodeMixin subjectCodeMixin,
        @CommandLine.
        Option(names = "--batch-size",
               description = "batch size if query more than one catalog")
        Integer batchSize,
        @CommandLine.Mixin OutputFileMixin outputFile) {
    long start = System.nanoTime();
    outputFile.writeOutput(SelectCourses.selectCourses(
        termMixin.getTerm(), subjectCodeMixin.getSubjectCodes()));

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }

  @CommandLine.
  Command(name = "clean", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Serve data",
          description = "Clean epochs")
  public void
  clean(@CommandLine.Mixin TermMixin termMixin,
        @CommandLine.Option(names = "--epoch",
                            description = "The epoch to clean") Integer epoch) {
    Term term = termMixin.getTermAllowNull();
    if (epoch == null && term == null) {
      logger.info("Cleaning incomplete epochs...");
      CleanEpoch.cleanIncompleteEpochs();
    } else if (epoch != null && term == null) {
      logger.info("Cleaning epoch={}...", epoch);
      CleanEpoch.cleanEpoch(epoch);
    } else if (term != null && epoch == null) {
      logger.info("Cleaning epochs for term={}...", term);
      CleanEpoch.cleanEpoch(term);
    } else {
      System.err.println("Term and --epoch are mutually exclusive!");
      System.exit(1);
    }
  }

  @CommandLine.
  Command(name = "serve", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Serve data",
          description = "Serve data through an API")
  public void
  serve() {
    App.run();
  }
}
