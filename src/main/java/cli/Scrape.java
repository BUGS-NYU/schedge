package cli;

import static picocli.CommandLine.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.asynchttpclient.*;
import org.slf4j.*;
import picocli.CommandLine;
import scraping.PeopleSoftClassSearch;
import utils.Nyu;

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

  // @Command(name = "catalog",
  //          description = "Scrape catalog based on term, subject codes, "
  //                        + "or school for one or multiple subjects/schools")
  // public void
  // catalog(@Mixin Mixins.Term termMixin, @Mixin Mixins.Subject subjectMixin,
  //         @Option(names = "--batch-size", defaultValue = "20",
  //                 description = "batch size if query more than one catalog")
  //         int batchSize,
  //         @Mixin Mixins.OutputFile outputFileMixin) {
  //   long start = System.nanoTime();

  //   List<scraping.models.Course> courses = ScrapeCatalog.scrapeCatalog(
  //       termMixin.getTerm(), subjectMixin.getSubjects(), batchSize);

  //   outputFileMixin.writeOutput(courses);

  //   Client.close();

  //   long end = System.nanoTime();
  //   double duration = (end - start) / 1000000000.0;
  //   logger.info("{} seconds for {} courses", duration, courses.size());
  // }

  @Command(name = "ps", sortOptions = false,
           headerHeading = "Command: ", descriptionHeading = "%nDescription:%n",
           parameterListHeading = "%nParameters:%n",
           optionListHeading = "%nOptions:%n",
           header = "Scrape the PeopleSoft Class Search",
           description = "Scrape the PeopleSoft Class Search for a term")
  public void
  ps(@Mixin Mixins.Term termMixin, @Mixin Mixins.OutputFile outputFileMixin)
      throws IOException, ExecutionException, InterruptedException {
    long start = System.nanoTime();

    Nyu.Term term = termMixin.getTerm();
    try (AsyncHttpClient client = new DefaultAsyncHttpClient()) {

      var schools = PeopleSoftClassSearch.scrapeSchools(client, term);
      outputFileMixin.writeOutput(schools);
    }

    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }
}
