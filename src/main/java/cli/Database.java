package cli;

import static actions.CopyTermFromProduction.*;
import static picocli.CommandLine.*;
import static utils.Nyu.*;

import actions.WriteTerm;
import database.*;
import java.util.*;
import me.tongfei.progressbar.ProgressBar;
import org.slf4j.*;
import picocli.CommandLine;
import scraping.*;

@Command(name = "db", description = "Operate on data in the database.\n")
public class Database implements Runnable {
  @Spec private CommandLine.Model.CommandSpec spec;

  @Option(
      names = {"-h", "--help"},
      usageHelp = true,
      description = "display a help message")
  boolean displayHelp;

  private static Logger logger = LoggerFactory.getLogger("cli.Database");

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand.\n");
  }

  @Command(name = "scrape-schools", description = "Scrape schools for a term")
  public void scrapeSchools(@Mixin Mixins.TermOption termMixin) {
    long start = System.nanoTime();

    var term = termMixin.term;

    var schools = PSClassSearch.scrapeSchools(term);

    GetConnection.withConnection(
        conn -> {
          UpdateSchools.updateSchoolsForTerm(conn, term, schools);
        });
    GetConnection.close();

    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  @Command(name = "scrape-term", description = "Scrape all data for a term")
  public void scrapeTerm(@Mixin Mixins.TermArgument termMixin) {
    long start = System.nanoTime();

    GetConnection.withConnection(
        conn -> {
          for (var term : termMixin.terms) {
            try (ProgressBar bar = new ProgressBar("Scrape " + term.json(), -1)) {
              var data = PSClassSearch.scrapeTerm(term, ScrapeEvent.cli(logger, bar));
              WriteTerm.writeTerm(conn, data);
            }
          }
        });

    GetConnection.close();

    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  @Command(
      name = "populate",
      description =
          "Populate the database by scraping the existing production " + "Schedge instance.\n")
  public void populate(
      @Mixin Mixins.TermArgument termMixin,
      @Option(
              names = {"--v1"},
              description = "scrape v1")
          boolean useV1,
      @Option(
              names = {"--v2"},
              description = "scrape v2",
              defaultValue = "true")
          boolean useV2) {
    long start = System.nanoTime();

    var terms = termMixin.terms;
    if (useV1 && useV2) {
      throw new IllegalArgumentException(
          "--v1 and --v2 are incompatible because they mean opposite things");
    }

    // Default to v2 but use v1 if the user explicitly requests it
    var version = useV1 ? SchedgeVersion.V1 : SchedgeVersion.V2;
    for (var term : terms) {
      copyTermFromProduction(version, term, ScrapeEvent.log(logger));
    }

    GetConnection.close();

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info("{} seconds", duration);
  }

  private static final HashMap<String, Optional<List<String>>> subjects;

  static {
    var s = new HashMap<String, Optional<List<String>>>();

    s.put("sp2021", Optional.of(Arrays.asList("csci-ua", "sca-ua_1", "math-ua", "ds-ua")));

    s.put("fa2022", Optional.of(Arrays.asList("csci-ua")));

    s.put(
        "sp2023",
        Optional.of(
            Arrays.asList(
                "ITPG-GT",
                "MASY1-GC",
                "CSCI-UA",
                "DM-UY",
                "CS-UY",
                "INTG1-GC",
                "PUBB1-GC",
                "URPL-GP",
                "DHSS-GA",
                "OART-UT",
                "OART-GT",
                "COART-UT",
                "IMNY-UT",
                "PWRT1-GC")));

    subjects = s;
  }

  @Command(
      name = "test-populate",
      description =
          "Populate the database for testing by scraping the existing production "
              + "Schedge instance.\n")
  public void testPopulate() {
    var start = System.nanoTime();

    GetConnection.forceInit();

    for (var pair : subjects.entrySet()) {
      var term = Term.fromString(pair.getKey());
      var subjects = pair.getValue();

      logger.info("Fetching term={}", term.json());

      GetConnection.withConnection(
          conn -> {
            var termStart = System.nanoTime();

            var result = ScrapeSchedgeV2.scrapeFromSchedge(term, subjects, ScrapeEvent.log(logger));

            var fetchEnd = System.nanoTime();
            var duration = (fetchEnd - termStart) / 1000000000.0;
            logger.info("Fetching took {} seconds", duration);

            WriteTerm.writeTerm(conn, result);
            var dbEnd = System.nanoTime();
            duration = (dbEnd - fetchEnd) / 1000000000.0;

            logger.info("Insertion took {} seconds", duration);
          });
    }

    GetConnection.close();

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info("{} seconds", duration);
  }
}
