package cli;

import cli.templates.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import nyu.Term;
import nyu.User;
import org.jooq.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import register.AddCourses;
import register.Context;
import register.Login;
import scraping.query.QueryCatalog;
import scraping.query.QuerySchool;
import scraping.query.QuerySection;

/*
   @Todo: Add annotation for parameter. Fix the method to parse.
          Adding multiple options for querying
   @Help: Add annotations, comments to code
*/
@CommandLine.Command(name = "query", synopsisSubcommandLabel =
                                         "(catalog | section | school | shop)")
public class Query implements Runnable {
  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  private static Logger logger = LoggerFactory.getLogger("cli.Query");

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(),
                                             "Missing required subcommand");
  }

  @CommandLine.Command(
      name = "catalog", sortOptions = false, headerHeading = "Usage:%n%n",
      synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n", header = "Query catalog",
      description =
          "Query catalog based on term, subject codes, or school for one or multiple subjects/schools")
  public void
  catalog(@CommandLine.Mixin TermMixin termMixin,
          @CommandLine.Mixin SubjectCodeMixin subjectCodes,
          @CommandLine.
          Option(names = "--batch-size",
                 description = "batch size if query more than one catalog")
          Integer batchSize,
          @CommandLine.Mixin OutputFileMixin outputFile) {

    long start = System.nanoTime();
    outputFile.writeOutput(QueryCatalog.queryCatalog(
        termMixin.getTerm(), subjectCodes.getSubjectCodes(), batchSize));
    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  // @ToDo: Adding query section for multiple sections
  @CommandLine.
  Command(name = "section", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Query section",
          description = "Query section based on registration number")
  public void
  section(@CommandLine.Mixin TermMixin termMixin,
          @CommandLine.
          Option(names = "--registration-number",
                 description = "registration number for specific catalog",
                 required = true) Integer registrationNumber,
          @CommandLine.Mixin OutputFileMixin outputFile) {
    long start = System.nanoTime();
    outputFile.writeOutput(
        QuerySection.querySection(termMixin.getTerm(), registrationNumber));
    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  @CommandLine.
  Command(name = "school", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Query school",
          description = "Query school based on term")
  public void
  school(@CommandLine.Mixin TermMixin termMixin,
         @CommandLine.Mixin OutputFileMixin outputFile) {
    long start = System.nanoTime();
    outputFile.writeOutput(QuerySchool.querySchool(termMixin.getTerm()));
    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  @CommandLine.
  Command(name = "shop", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Scrape school/subject",
          description = "Scrape school/subject based on term")
  public void register(@CommandLine.Mixin TermMixin termMixin,
                       @CommandLine.Mixin LoginMixin loginMixin,
                       @CommandLine.
                       Mixin RegistrationNumberMixin registrationNumberMixin,
                       @CommandLine.Mixin OutputFileMixin outputFileMixin)
      throws ExecutionException, InterruptedException {

    long start = System.nanoTime();
    Term term = termMixin.getTerm();
    User user = loginMixin.getUser();
    Login.addToCart(user.getUsername(), user.getPassword(),
            term, registrationNumberMixin.getRegistrationNumber(),
            Context.getContextAsync(term).get());
    long end = System.nanoTime();
    double duration = (end - start) / 1000000000.0;
    logger.info(duration + " seconds");
  }
}
