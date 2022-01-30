package cli;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Mixin;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Spec;

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
import scraping.query.GetClient;
import scraping.query.QuerySchool;

/*
   @Todo: Add annotation for parameter.
*/
@Command(name = "scrape",
         description =
             "Query then parse NYU Albert data based on different catagories")
public class Scrape implements Runnable {
  @Spec private CommandLine.Model.CommandSpec spec;

  private static Logger logger = LoggerFactory.getLogger("cli.Scrape");
  @Option(names = {"-h", "--help"}, usageHelp = true,
          description = "display a help message")
  boolean displayHelp;

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(),
                                             "\nMissing required subcommand.");
  }

  @CommandLine.Command(
      name = "sections",
      description = "Scrape section based on term and registration number, OR "
                    + "school and subject.\n")
  public void
  sections(@Mixin Mixins.Term termMixin,
           @Mixin Mixins.RegistrationNumber registrationNumberMixin,
           @Option(names = "--batch-size-catalog",
                   description = "batch size if query more than one catalog")
           Integer batchSize,
           @Option(names = "--batch-size-sections",
                   description = "batch size if query more than one catalog")
           Integer batchSizeSections,
           @Mixin Mixins.OutputFile outputFileMixin) {
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

    GetClient.close();

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + "seconds");
  }

  @Command(name = "catalog",
           description = "Scrape catalog based on term, subject codes, "
                         + "or school for one or multiple subjects/schools")
  public void
  catalog(@Mixin Mixins.Term termMixin,
          @Mixin Mixins.SubjectCode subjectCodeMixin,
          @Option(names = "--batch-size", defaultValue = "20",
                  description = "batch size if query more than one catalog")
          Integer batchSize,
          @Mixin Mixins.OutputFile outputFileMixin) {
    long start = System.nanoTime();

    outputFileMixin.writeOutput(
        ScrapeCatalog
            .scrapeFromCatalog(termMixin.getTerm(),
                               subjectCodeMixin.getSubjectCodes(), batchSize)
            .collect(Collectors.toList()));

    GetClient.close();

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }

  @Command(name = "school", sortOptions = false,
           headerHeading = "Command: ", descriptionHeading = "%nDescription:%n",
           parameterListHeading = "%nParameters:%n",
           optionListHeading = "%nOptions:%n", header = "Scrape school/subject",
           description = "Scrape school/subject based on term")
  public void
  school(@Mixin Mixins.Term termMixin, @Mixin Mixins.OutputFile outputFileMixin)
      throws ExecutionException, InterruptedException {
    long start = System.nanoTime();

    outputFileMixin.writeOutput(ParseSchoolSubjects.parseSchool(
        QuerySchool.querySchool(termMixin.getTerm())));

    GetClient.close();

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }
}
