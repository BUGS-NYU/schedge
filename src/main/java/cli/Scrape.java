package cli;

import static picocli.CommandLine.*;

import me.tongfei.progressbar.ProgressBar;
import org.slf4j.*;
import picocli.CommandLine;
import scraping.*;
import utils.Nyu;

/*
   @Todo: Add annotation for parameter.
*/
@Command(name = "scrape", description = "Scrape course data")
public class Scrape implements Runnable {
  @Spec private CommandLine.Model.CommandSpec spec;

  private static Logger logger = LoggerFactory.getLogger("cli.Scrape");

  @Option(
      names = {"-h", "--help"},
      usageHelp = true,
      description = "display a help message")
  boolean displayHelp;

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(), "\nMissing required subcommand.");
  }

  @Command(
      name = "term",
      sortOptions = false,
      headerHeading = "Command: ",
      descriptionHeading = "%nDescription:%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n",
      header = "Scrape the PeopleSoft Class Search",
      description = "Scrape the PeopleSoft Class Search for a term")
  public void term(@Mixin Mixins.TermOption termMixin, @Mixin Mixins.OutputFile outputFileMixin) {
    long start = System.nanoTime();

    Nyu.Term term = termMixin.term;
    try (ProgressBar bar = new ProgressBar("Scrape", -1)) {
      var courses = PSClassSearch.scrapeTerm(term, ScrapeEvent.cli(logger, bar));
      outputFileMixin.writeOutput(courses);
    }

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }

  @Command(
      name = "subject",
      sortOptions = false,
      headerHeading = "Command: ",
      descriptionHeading = "%nDescription:%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n",
      header = "Scrape the PeopleSoft Class Search",
      description = "Scrape the PeopleSoft Class Search for a term")
  public void subject(
      @Mixin Mixins.TermOption termMixin,
      @Mixin Mixins.OutputFile outputFileMixin,
      @Parameters(index = "0", paramLabel = "SUBJECT", description = "A subject code like MATH-UA")
          String subject) {
    long start = System.nanoTime();

    Nyu.Term term = termMixin.term;
    var courses = PSClassSearch.scrapeSubject(term, subject);
    outputFileMixin.writeOutput(courses);

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }

  @Command(
      name = "schools",
      sortOptions = false,
      headerHeading = "Command: ",
      descriptionHeading = "%nDescription:%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n",
      header = "Scrape the PeopleSoft Class Search",
      description = "Scrape the PeopleSoft Class Search for a term")
  public void schools(
      @Mixin Mixins.TermOption termMixin, @Mixin Mixins.OutputFile outputFileMixin) {
    long start = System.nanoTime();

    Nyu.Term term = termMixin.term;
    var schools = PSClassSearch.scrapeSchools(term);
    outputFileMixin.writeOutput(schools);

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }
}
