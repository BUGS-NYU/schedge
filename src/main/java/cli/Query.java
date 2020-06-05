package cli;

import cli.templates.OutputFileMixin;
import cli.templates.SubjectCodeMixin;
import cli.templates.TermMixin;
import database.GetConnection;
import database.instructors.UpdateInstructors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import scraping.GetRatings;
import scraping.models.Instructor;
import scraping.query.QueryCatalog;
import scraping.query.QuerySchool;
import scraping.query.QuerySection;

import java.util.List;

/*
   @Todo: Add annotation for parameter. Fix the method to parse.
          Adding multiple options for querying
   @Help: Add annotations, comments to code
*/
@CommandLine.
Command(name = "query", description = "Querying data from NYU Albert",
        synopsisSubcommandLabel = "(catalog | section | school | rmp)")
public class Query implements Runnable {
  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;
  @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
                      description = "display a help message")
  boolean displayHelp;

  private static Logger logger = LoggerFactory.getLogger("cli.Query");

  @Override
  public void run() {
    throw new CommandLine.ParameterException(
        spec.commandLine(),
        "\nMissing required subcommand. Try ./schedge query [subcommand] --help to"
            + " display help message for possible subcommands");
  }

  @CommandLine.Command(
      name = "catalog", sortOptions = false,
      headerHeading = "Command: ", descriptionHeading = "%nDescription:%n",
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
  Command(name = "section", sortOptions = false, headerHeading = "Command: ",
          descriptionHeading = "%nDescription:%n%n",
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
  Command(name = "school", sortOptions = false, headerHeading = "Command: ",
          descriptionHeading = "%nDescription:%n%n",
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
  Command(name = "rmp", sortOptions = false, headerHeading = "Command: ",
          descriptionHeading = "%nDescription:%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n",
          header = "Query rating for professors from Rate My Professor",
          description = "Query rating for professors based on term from Rate My Professor")
  public void
  rmp(@CommandLine.Mixin OutputFileMixin outputFile, Integer batchSize) {
    long start = System.nanoTime();
    GetConnection.withConnection(conn -> {
      List<Instructor> instructors =
          UpdateInstructors.instructorUpdateList(conn);
      outputFile.writeOutput(
          GetRatings.getRatings(instructors.iterator(), batchSize));
    });
    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }
}
