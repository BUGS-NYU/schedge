package cli;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Mixin;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Spec;
import static utils.TryCatch.*;

import actions.CleanData;
import actions.ScrapeTerm;
import actions.UpdateData;
import api.App;
import api.v1.SelectCourses;
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
import org.slf4j.*;
import picocli.CommandLine;
import scraping.ScrapeSchedge;
import scraping.query.GetClient;

@Command(name = "db", description = "Operate on data in the database.\n",
         subcommands = {Database.Clean.class})
public class Database implements Runnable {
  @Spec private CommandLine.Model.CommandSpec spec;

  @Option(names = {"-h", "--help"}, usageHelp = true,
          description = "display a help message")
  boolean displayHelp;

  private static Logger logger = LoggerFactory.getLogger("cli.Database");

  private static ProgressBarBuilder barBuilder =
      new ProgressBarBuilder()
          .setStyle(ProgressBarStyle.ASCII)
          .setConsumer(new ConsoleProgressBarConsumer(System.out));

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(),
                                             "Missing required subcommand.\n");
  }

  @Command(
      name = "scrape",
      description = "Scrape section based on term and registration number, "
                    + "OR school and subject from db.\n")
  public void
  scrape(@Mixin Mixins.Term termMixin, @Mixin Mixins.BatchSize batchSize,
         @Option(names = "--service",
                 description = "turns scraping into a service; if set, "
                               + "--year, --semester, and --term are ignored.")
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

  @Command(name = "rmp", description = "Scrape Rate My Professor for ratings, "
                                       +
                                       "parsed and updated in the database.\n")
  public void
  rmp(@Option(names = "--batch-size",
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

  @Command(name = "query",
           description = "Query section based on term and registration number, "
                         + "OR school and subject from db.\n")
  public void
  query(@Mixin Mixins.Term termMixin,
        @Mixin Mixins.SubjectCode subjectCodeMixin,
        @Mixin Mixins.OutputFile outputFile) {
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

  @Command(name = "clean", description = "Clean unused epoch data.\n")
  public static class Clean implements Runnable {
    @Mixin Mixins.Term termMixin;

    @Option(names = "--epoch", description = "The epoch to clean")
    Integer epoch;

    @Spec CommandLine.Model.CommandSpec spec;

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

  @Command(name = "serve", description = "Serve data through the API.\n")
  public void
  serve(@Mixin Mixins.BatchSize batchSizeMixin,
        @Option(names = "--scrape",
                description = "whether or not to scrape while serving")
        boolean scrape) {
    App.run();

    // This SHOULD eagerly create the connection pool, so that there's not a
    // situation where the first person to request from the database is
    // guarranteed to get screwed
    //                        - Albert Liu, Jan 26, 2022 Wed 00:06 EST
    GetConnection.withConnection(conn -> {
      Term term = new Term("sp", 2022);
      Integer epoch = LatestCompleteEpoch.getLatestEpoch(conn, term);
    });

    while (scrape) {
      CleanData.cleanData();
      UpdateData.updateData(batchSizeMixin.getCatalog(20),
                            batchSizeMixin.getSections(20));

      tcFatal(() -> TimeUnit.DAYS.sleep(1), "Failed to sleep");
    }
  }

  @Command(
      name = "populate",
      description = "Populate the database by scraping the existing production "
                    + "Schedge instance.\n")
  public void
  populate(@Mixin Mixins.Term termMixin,
           @Option(names = "--domain", defaultValue = "schedge.a1liu.com",
                   description =
                       "domain to scrape as if it's an instance of schedge")
           String domain) {
    long start = System.nanoTime();

    Term term = termMixin.getTerm();

    GetConnection.withConnection(conn -> {
      int epoch = GetNewEpoch.getNewEpoch(conn, term);

      // TODO this will eventually scrape directly from the new API instead of
      // the old one
      //                        - Albert Liu, Jan 25, 2022 Tue 18:32 EST
      ScrapeSchedge.scrapeFromSchedge(term).forEach(course -> {
        ArrayList<Course> courses = new ArrayList<>();
        courses.add(course);

        tcFatal(
            () -> InsertFullCourses.insertCourses(conn, term, epoch, courses));
      });

      CompleteEpoch.completeEpoch(conn, term, epoch);
    });

    GetConnection.close();
    GetClient.close();

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + "seconds");
  }
}
