package cli;

import static actions.CopyTermFromProduction.*;
import static picocli.CommandLine.*;
import static utils.Nyu.*;

import actions.ScrapeTerm;
import database.*;
import me.tongfei.progressbar.ProgressBar;
import org.slf4j.*;
import picocli.CommandLine;
import scraping.*;
import java.util.*;

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
  public void scrapeSchools(@Mixin Mixins.TermOption termMixin) {
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
  public void scrapeTerm(@Mixin Mixins.TermArgument termMixin) {
    long start = System.nanoTime();

    GetConnection.withConnection(conn -> {
      for (var term : termMixin.terms) {
        try (ProgressBar bar = new ProgressBar("Scrape " + term.json(), -1)) {
          ScrapeTerm.scrapeTerm(conn, term, e -> {
            switch (e.kind) {
            case MESSAGE:
            case SUBJECT_START:
              bar.setExtraMessage(e.message);
              break;
            case WARNING:
              logger.warn(e.message);
              break;
            case PROGRESS:
              bar.stepBy(e.value);
              break;
            case HINT_CHANGE:
              bar.maxHint(e.value);
              break;
            }
          });
        }
      }
    });

    GetConnection.close();

    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  @Command(
      name = "populate",
      description = "Populate the database by scraping the existing production "
                    + "Schedge instance.\n")
  public void
  populate(@Mixin Mixins.TermArgument termMixin,
           @Option(names = {"--v2"},
                   description = "scrape v2 instead of v1") boolean useV2) {
    long start = System.nanoTime();

    var terms = termMixin.terms;
    var version = useV2 ? SchedgeVersion.V2 : SchedgeVersion.V1;
    for (var term : terms) {
      copyTermFromProduction(version, term);
    }

    GetConnection.close();

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info("{} seconds", duration);
  }

  @Command(
          name = "ci-populate",
          description = "Populate the database for CI by scraping the existing production "
                  + "Schedge instance.\n")
  public void
  ciPopulate(@Parameters(paramLabel = "SUBJECT_STRINGS") String[] subjectStrings) {
    var start = System.nanoTime();

    var map = new HashMap<String, ArrayList<String>>();

    for (var subjectAndTermString : subjectStrings) {
      var parts = subjectAndTermString.split("/", 2);
      var termString = parts[0];

      if (parts.length == 0) {
        map.put(termString, null);
        continue;
      }

      var subjectString = parts[1];

      var subjects = map.getOrDefault(termString, new ArrayList<>());
      subjects.add(subjectString);
    }

    for (var pair : map.entrySet()) {
      var term = Term.fromString(pair.getKey());
      var subjects = pair.getValue();

      GetConnection.withConnection(conn -> {
        var termStart = System.nanoTime();
        var result = ScrapeSchedgeV2.scrapeFromSchedge(term, subjects);

        var fetchEnd = System.nanoTime();
        var duration = (fetchEnd - termStart) / 1000000000.0;
        logger.info("Fetching took {} seconds", duration);
        if (result == null)
          return;

        UpdateSchools.updateSchoolsForTerm(conn, term, result.schools);
        InsertCourses.insertCourses(conn, term, result.courses);

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
