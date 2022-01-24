package cli;

import cli.templates.OutputFileMixin;
import cli.templates.RegistrationNumberMixin;
import cli.templates.SubjectCodeMixin;
import cli.templates.TermMixin;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import nyu.SubjectCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import scraping.ScrapeCatalog;
import scraping.ScrapeSection;
import scraping.parse.ParseSchoolSubjects;
import scraping.query.QuerySchool;

/*
   @Todo: Add annotation for parameter.
*/
@CommandLine.
Command(name = "scrape",
        description =
            "Query then parse NYU Albert data based on different catagories",
        synopsisSubcommandLabel = "(catalog | sections | school | schedge)")
public class Scrape implements Runnable {
  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  private static Logger logger = LoggerFactory.getLogger("cli.Scrape");
  @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
                      description = "display a help message")
  boolean displayHelp;

  @Override
  public void run() {
    throw new CommandLine.ParameterException(
        spec.commandLine(),
        "\nMissing required subcommand. Try ./schedge scrape [subcommand] --help to"
            + " display help message for possible subcommands");
  }

  @CommandLine.
  Command(name = "schedge", sortOptions = false, headerHeading = "Command: ",
          descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Scrape section",
          description = "Scrape existing Schedge instance")
  public void
  schedge(
      @CommandLine.
      Option(names = "--domain", defaultValue = "schedge.a1liu.com",
             description = "domain to scrape as if it's an instance of schedge")
      String domain,
      @CommandLine.Mixin OutputFileMixin outputFileMixin) {
    // TODO this will eventually scrape directly from the new API  instead of
    // the old one
    long start = System.nanoTime();

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + "seconds");
  }

  @CommandLine.Command(
      name = "sections", sortOptions = false,
      headerHeading = "Command: ", descriptionHeading = "%nDescription:%n%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n", header = "Scrape section",
      description =
          "Scrape section based on term and registration number, OR school and subject")
  public void
  sections(@CommandLine.Mixin TermMixin termMixin,
           @CommandLine.Mixin RegistrationNumberMixin registrationNumberMixin,
           @CommandLine.
           Option(names = "--batch-size-catalog",
                  description = "batch size if query more than one catalog")
           Integer batchSize,
           @CommandLine.
           Option(names = "--batch-size-sections",
                  description = "batch size if query more than one catalog")
           Integer batchSizeSections,
           @CommandLine.Mixin OutputFileMixin outputFileMixin) {
    long start = System.nanoTime();
    List<SubjectCode> subjectCodes = registrationNumberMixin.getSubjectCodes();
    if (subjectCodes == null) {
      outputFileMixin.writeOutput(ScrapeSection.scrapeFromSection(
          termMixin.getTerm(),
          registrationNumberMixin.getRegistrationNumber()));
    } else {
      outputFileMixin.writeOutput(
          ScrapeSection
              .scrapeFromSection(termMixin.getTerm(), subjectCodes, batchSize,
                                 batchSizeSections)
              .collect(Collectors.toList()));
    }
    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + "seconds");
  }

  @CommandLine.Command(
      name = "catalog", sortOptions = false,
      headerHeading = "Command: ", descriptionHeading = "%nDescription:%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n", header = "Scrape catalog",
      description =
          "Scrape catalog based on term, subject codes, or school for one or multiple subjects/schools")
  public void
  catalog(@CommandLine.Mixin TermMixin termMixin,
          @CommandLine.Mixin SubjectCodeMixin subjectCodeMixin,
          @CommandLine.
          Option(names = "--batch-size",
                 description = "batch size if query more than one catalog")
          Integer batchSize,
          @CommandLine.Mixin OutputFileMixin outputFileMixin) {
    long start = System.nanoTime();
    outputFileMixin.writeOutput(
        ScrapeCatalog
            .scrapeFromCatalog(termMixin.getTerm(),
                               subjectCodeMixin.getSubjectCodes(), batchSize)
            .collect(Collectors.toList()));
    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }

  @CommandLine.
  Command(name = "school", sortOptions = false,
          headerHeading = "Command: ", descriptionHeading = "%nDescription:%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Scrape school/subject",
          description = "Scrape school/subject based on term")
  public void
  school(@CommandLine.Mixin TermMixin termMixin,
         @CommandLine.Mixin OutputFileMixin outputFileMixin)
      throws ExecutionException, InterruptedException {
    long start = System.nanoTime();
    outputFileMixin.writeOutput(ParseSchoolSubjects.parseSchool(
        QuerySchool.querySchool(termMixin.getTerm())));
    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }
}
