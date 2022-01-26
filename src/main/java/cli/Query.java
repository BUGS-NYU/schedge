package cli;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Mixin;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Spec;

import database.GetConnection;
import database.instructors.UpdateInstructors;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import scraping.GetRatings;
import scraping.models.Instructor;
import scraping.query.QueryCatalog;
import scraping.query.QuerySchool;
import scraping.query.QuerySection;

/*
   @Todo: Add annotation for parameter. Fix the method to parse.
          Adding multiple options for querying
   @Help: Add annotations, comments to code
*/
@Command(name = "query", description = "Query data from NYU Albert")
public class Query implements Runnable {
  @Spec private CommandLine.Model.CommandSpec spec;
  @Option(names = {"-h", "--help"}, usageHelp = true,
          description = "display a help message")
  boolean displayHelp;

  private static Logger logger = LoggerFactory.getLogger("cli.Query");

  @Override
  public void run() {
    throw new CommandLine.ParameterException(
        spec.commandLine(), "Need to use a subcommand here.\n");
  }

  @Command(name = "catalog",
           description =
               "Query catalog based on term, subject codes, or school for one "
               + "or multiple subjects/schools.\n")
  public void
  catalog(@Mixin Mixins.Term termMixin, @Mixin Mixins.SubjectCode subjectCodes,
          @Option(names = "--batch-size",
                  description = "batch size if query more than one catalog")
          Integer batchSize,
          @Mixin Mixins.OutputFile outputFile) {

    long start = System.nanoTime();
    outputFile.writeOutput(QueryCatalog.queryCatalog(
        termMixin.getTerm(), subjectCodes.getSubjectCodes(), batchSize));
    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  // @TODO: Adding query section for multiple sections
  @Command(name = "section",
           description = "Query section based on registration number.\n")
  public void
  section(@Mixin Mixins.Term termMixin,
          @Option(names = "--registration-number",
                  description = "registration number for specific catalog",
                  required = true) Integer registrationNumber,
          @Mixin Mixins.OutputFile outputFile) {
    long start = System.nanoTime();
    outputFile.writeOutput(
        QuerySection.querySection(termMixin.getTerm(), registrationNumber));
    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  @Command(name = "school", description = "Query school based on term.\n")
  public void school(@Mixin Mixins.Term termMixin,
                     @Mixin Mixins.OutputFile outputFile) {
    long start = System.nanoTime();
    outputFile.writeOutput(QuerySchool.querySchool(termMixin.getTerm()));
    long end = System.nanoTime();
    logger.info((end - start) / 1000000000 + " seconds");
  }

  @Command(
      name = "rmp",
      description =
          "Query rating for professors based on term from Rate My Professor.\n")
  public void
  rmp(@Mixin Mixins.OutputFile outputFile, Integer batchSize) {
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
