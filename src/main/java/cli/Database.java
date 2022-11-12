package cli;

import static picocli.CommandLine.*;
import static utils.Nyu.*;

import actions.ScrapeTerm;
import database.*;
import org.slf4j.*;
import picocli.CommandLine;
import scraping.*;

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
  public void scrapeSchools(@Mixin Mixins.Term termMixin) {
    long start = System.nanoTime();

    var term = termMixin.term;

    var schools = PeopleSoftClassSearch.scrapeSchools(term);

    GetConnection.withConnection(
        conn -> { UpdateSchools.updateSchoolsForTerm(conn, term, schools); });
    GetConnection.close();

    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  @Command(name = "scrape-term", description = "Scrape all data for a term")
  public void scrapeTerm(@Parameters(
      paramLabel = "TERMS",
      description = "Terms to scrape, e.g. fa2020, ja2020, sp2020, su2020",
      converter = Mixins.TermConverter.class) Term[] terms) {
    long start = System.nanoTime();

    for (var term : terms) {
      ScrapeTerm.scrapeTerm(term, true);
    }

    GetConnection.close();

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

    Term term = termMixin.term;
    GetConnection.withConnection(conn -> {
      var courses = ScrapeSchedge2.scrapeFromSchedge(term);

      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info("Fetching took {} seconds", duration);
      if (courses == null)
        return;

      InsertCourses.clearPrevious(conn, term);
      InsertCourses.insertCourses(conn, term, courses);
    });

    GetConnection.close();

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info("{} seconds", duration);
  }
}
