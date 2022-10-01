package cli;

import static picocli.CommandLine.*;
import static utils.TryCatch.*;
import static utils.Utils.*;

import actions.*;
import api.App;
import api.SelectCourses;
import database.GetConnection;
import database.courses.InsertFullCourses;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.slf4j.*;
import picocli.CommandLine;
import scraping.ScrapeSchedge;
import types.*;
import utils.Client;

@Command(name = "db", description = "Operate on data in the database.\n")
public class Database implements Runnable {
  @Spec private CommandLine.Model.CommandSpec spec;

  @Option(names = {"-h", "--help"}, usageHelp = true,
          description = "display a help message")
  boolean displayHelp;

  private static Logger logger = LoggerFactory.getLogger("cli.Database");

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
      UpdateData.updateData(batchSize.catalog, batchSize.sections);

      tcFatal(() -> TimeUnit.DAYS.sleep(1), "Failed to sleep");
    }

    long start = System.nanoTime();

    int catalogBatch = batchSize.catalog;
    int sectionsBatch = batchSize.sections;
    Term term = termMixin.getTerm();

    ScrapeTerm.scrapeTerm(term, catalogBatch, sectionsBatch, true);

    GetConnection.close();
    Client.close();
    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  @Command(name = "query",
           description = "Query section based on term and registration number, "
                         + "OR school and subject from db.\n")
  public void
  query(@Mixin Mixins.Term termMixin, @Mixin Mixins.Subject subjectCodeMixin,
        @Mixin Mixins.OutputFile outputFile) {
    long start = System.nanoTime();

    Term term = termMixin.getTerm();
    List<String> codes = subjectCodeMixin.getSubjects();
    GetConnection.withConnection(conn -> {
      Object o = SelectCourses.selectCourses(conn, term, codes);
      outputFile.writeOutput(o);
    });

    GetConnection.close();

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
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
      List<List<Course>> data = ScrapeSchedge.scrapeFromSchedge(term);

      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info("Fetching took {} seconds", duration);

      InsertFullCourses.clearPrevious(conn, term);

      for (List<Course> courses : data) {
        tcFatal(() -> InsertFullCourses.insertCourses(conn, term, courses));
      }
    });

    GetConnection.close();
    Client.close();

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info("{} seconds", duration);
  }
}
