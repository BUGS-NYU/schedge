package cli;

import static picocli.CommandLine.*;
import static utils.Nyu.*;

import database.GetConnection;
import database.UpdateSchools;
import database.courses.InsertFullCourses;
import java.io.*;
import java.util.concurrent.ExecutionException;
import org.asynchttpclient.*;
import org.slf4j.*;
import picocli.CommandLine;
import scraping.PeopleSoftClassSearch;
import scraping.ScrapeSchedge;

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

  @Command(name = "scrape-schools", description = "Scrape schools for a term")
  public void scrapeSchools(@Mixin Mixins.Term termMixin)
      throws IOException, ExecutionException, InterruptedException {
    long start = System.nanoTime();

    var term = termMixin.getTerm();

    try (AsyncHttpClient client = new DefaultAsyncHttpClient()) {
      var schools = PeopleSoftClassSearch.scrapeSchools(client, term);

      GetConnection.withConnection(
          conn -> { UpdateSchools.updateSchoolsForTerm(conn, term, schools); });
      GetConnection.close();
    }

    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  @Command(
      name = "populate",
      description = "Populate the database by scraping the existing production "
                    + "Schedge instance.\n")
  public void
  populate(@Mixin Mixins.Term termMixin) {
    long start = System.nanoTime();

    Term term = termMixin.getTerm();
    try (var client = new DefaultAsyncHttpClient()) {
      GetConnection.withConnection(conn -> {
        var courses = ScrapeSchedge.scrapeFromSchedge(client, term);

        long end = System.nanoTime();
        double duration = (end - start) / 1000000000.0;
        logger.info("Fetching took {} seconds", duration);

        InsertFullCourses.clearPrevious(conn, term);
        InsertFullCourses.insertCourses(conn, term, courses);
      });

      GetConnection.close();
    }

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info("{} seconds", duration);
  }
}
