package cli;

import static utils.TryCatch.*;

import actions.CleanData;
import actions.ScrapeTerm;
import actions.UpdateData;
import api.App;
import api.v1.SelectCourses;
import cli.templates.*;
import database.GetConnection;
import database.courses.InsertFullCourses;
import database.epochs.*;
import database.instructors.UpdateInstructors;
import database.models.FullRow;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;
import me.tongfei.progressbar.*;
import models.Course;
import nyu.SubjectCode;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import scraping.ScrapeSchedge;
import scraping.query.GetClient;

@CommandLine.
Command(name = "db",
        description = "query/scrape/update/serve data through the database",
        synopsisSubcommandLabel =
            "(scrape | query | update | serve | clean | populate)",
        subcommands = {Database.Clean.class})
public class Database implements Runnable {
  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;
  @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
                      description = "display a help message")
  boolean displayHelp;

  private static Logger logger = LoggerFactory.getLogger("cli.Database");
  private static ProgressBarBuilder barBuilder =
      new ProgressBarBuilder()
          .setStyle(ProgressBarStyle.ASCII)
          .setConsumer(new ConsoleProgressBarConsumer(System.out));

  @Override
  public void run() {
    throw new CommandLine.ParameterException(
        spec.commandLine(),
        "\nMissing required subcommand. Try ./schedge db [subcommand] --help to"
            + " display help message for possible subcommands");
  }

  @CommandLine.Command(
      name = "scrape", sortOptions = false,
      headerHeading = "Command: ", descriptionHeading = "%nDescription:%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n", header = "Scrape section from db",
      description =
          "Scrape section based on term and registration number, OR school and subject from db")
  public void
  scrape(
      @CommandLine.Mixin TermMixin termMixin,
      @CommandLine.Mixin BatchSizeMixin batchSize,
      @CommandLine.Option(
          names = "--service",
          description =
              "turns scraping into a service; if set, --year, --semester, and --term are ignored.")
      boolean service) {
    while (service) {
      CleanData.cleanData();
      UpdateData.updateData(batchSize.getCatalog(20),
                            batchSize.getSections(50));

      tcFatal(() -> TimeUnit.DAYS.sleep(1), "Failed to sleep");
    }

    long start = System.nanoTime();
    ScrapeTerm.scrapeTerm(
        termMixin.getTerm(), batchSize.getCatalog(20),
        batchSize.getSections(50),
        subjectCodes -> ProgressBar.wrap(subjectCodes, barBuilder));
    GetConnection.close();
    GetClient.close();
    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  @CommandLine.Command(
      name = "rmp", sortOptions = false,
      headerHeading = "Command: ", descriptionHeading = "%nDescription:%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n",
      header = "Update instructors' ratings using Rate My Professor",
      description =
          "Scrape Rate My Professor for ratings, parsed and updated in the database")
  public void
  rmp(@CommandLine.
      Option(names = "--batch-size",
             description = "batch size for querying Rate My Professor")
      Integer batchSize) {
    long start = System.nanoTime();
    GetConnection.withConnection(conn -> {
      UpdateInstructors.updateInstructors(
          conn,
          ProgressBar.wrap(UpdateInstructors.instructorUpdateList(conn),
                           barBuilder),
          batchSize);
    });
    GetConnection.close();
    GetClient.close();

    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  @CommandLine.Command(
      name = "query", sortOptions = false,
      headerHeading = "Command: ", descriptionHeading = "%nDescription:%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n", header = "Query section",
      description =
          "Query section based on term and registration number, OR school and subject from db")
  public void
  query(@CommandLine.Mixin TermMixin termMixin,
        @CommandLine.Mixin SubjectCodeMixin subjectCodeMixin,
        @CommandLine.Mixin OutputFileMixin outputFile) {
    long start = System.nanoTime();

    Term term = termMixin.getTerm();
    List<SubjectCode> codes = subjectCodeMixin.getSubjectCodes();
    GetConnection.withConnection(conn -> {
      Integer epoch = LatestCompleteEpoch.getLatestEpoch(conn, term);
      if (epoch == null) {
        logger.warn("No completed epoch for term=" + term);
        return;
      }

      Object o = SelectCourses.selectCourses(conn, epoch, codes);
      outputFile.writeOutput(o);
    });

    GetConnection.close();

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }

  @CommandLine.
  Command(name = "clean", sortOptions = false,
          headerHeading = "Command: ", descriptionHeading = "%nDescription:%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Clean epochs",
          description = "Clean epochs")
  public static class Clean implements Runnable {

    @CommandLine.Mixin TermMixin termMixin;
    @CommandLine.Option(names = "--epoch", description = "The epoch to clean")
    Integer epoch;

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    public void run() {
      Term term = termMixin.getTermAllowNull();
      GetConnection.withConnection(conn -> {
        if (epoch == null && term == null) {
          logger.info("Cleaning all old epochs...");
          CleanData.cleanData();
        } else if (epoch != null && term == null) {
          logger.info("Cleaning epoch={}...", epoch);
          CleanEpoch.cleanEpoch(conn, epoch);
        } else if (term != null && epoch == null) {
          logger.info("Cleaning epochs for term={}...", term);
          CleanEpoch.cleanEpochs(conn, term);
        } else {
          throw new CommandLine.ParameterException(
              spec.commandLine(), "Term and --epoch are mutually exclusive!");
        }
      });
      GetConnection.close();
    }
  }

  @CommandLine.
  Command(name = "serve", sortOptions = false,
          headerHeading = "Command: ", descriptionHeading = "%nDescription:%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Serve data",
          description = "Serve data through an API")
  public void
  serve(@CommandLine.Mixin BatchSizeMixin batchSizeMixin,
        @CommandLine.
        Option(names = "--scrape",
               description = "whether or not to scrape while serving")
        boolean scrape) {
    GetConnection.initIfNecessary();
    App.run();

    while (scrape) {
      CleanData.cleanData();
      UpdateData.updateData(batchSizeMixin.getCatalog(20),
                            batchSizeMixin.getSections(20));

      tcFatal(() -> TimeUnit.DAYS.sleep(1), "Failed to sleep");
    }
  }

  @CommandLine.
  Command(name = "populate", sortOptions = false, headerHeading = "Command: ",
          descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n",
          header = "populate database using production API",
          description = "Scrape existing Schedge instance")
  public void
  populate(
      @CommandLine.Mixin TermMixin termMixin,
      @CommandLine.
      Option(names = "--domain", defaultValue = "schedge.a1liu.com",
             description = "domain to scrape as if it's an instance of schedge")
      String domain) {
    // TODO this will eventually scrape directly from the new API instead of
    // the old one
    //                        - Albert Liu, Jan 25, 2022 Tue 18:32 EST
    long start = System.nanoTime();

    Term term = termMixin.getTerm();
    List<Course> courses =
        ScrapeSchedge.scrapeFromSchedge(term).collect(Collectors.toList());

    GetConnection.withConnection(conn -> {
      int epoch = GetNewEpoch.getNewEpoch(conn, term);
      InsertFullCourses.insertCourses(conn, term, epoch, courses);
      CompleteEpoch.completeEpoch(conn, term, epoch);
    });

    GetConnection.close();

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + "seconds");
  }
}
