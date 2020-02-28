package cli;

import cli.validation.*;

import models.Semester;
import models.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import services.Query_catalogKt;
import services.Query_schoolKt;
import services.Query_sectionKt;
import utils.UtilsKt;


/*
   @Todo: Add annotation for parameter. Fix the method to parse.
          Adding multiple options for querying
   @Help: Add annotations, comments to code
*/
@CommandLine.Command(name = "query",
                     synopsisSubcommandLabel = "(catalog | section | school)",
                     subcommands = {Query.Catalog.class, Query.Section.class,
                                    Query.School.class})
public class Query implements Runnable {
  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

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
  static class Catalog implements Runnable {
    private Logger logger = LoggerFactory.getLogger("query.catalog");

    @CommandLine.Option(names = "--term", description = "term to query from")
    private Integer term;
    @CommandLine.
    Option(names = "--semester", description = "semester: ja, sp, su, or fa")
    private String semester;
    @CommandLine.Option(names = "--year", description = "year to scrape from")
    private Integer year;
    @CommandLine.
    Option(names = "--school", description = "school code: UA, UT, UY, etc")
    private String school;
    @CommandLine.
    Option(names = "--subject",
           description = "subject code: CSCI(Computer Science), MA(Math), etc")
    private String subject;
    @CommandLine.
    Option(names = "--batch-size",
           description = "batch size if query more than one catalog")
    private Integer batchSize;
    @CommandLine.
    Option(names = "--output-file", description = "output file to write to")
    private String outputFile;

    public void run() {
      long start = System.nanoTime();
      ValidateCatalogArgs
          .validate(term, semester, year, school, subject, batchSize,
                    outputFile)
          .andRun((term, list, batchSize)
                      -> UtilsKt.seqToList(Query_catalogKt.queryCatalog(term, list, batchSize)),
                  (term, subjectCode)
                      -> Query_catalogKt.queryCatalog(term, subjectCode));
      long end = System.nanoTime();
      logger.info((end - start) / 1000000000 + " seconds");
    }
  }

  // @ToDo: Adding query section for multiple sections
  @CommandLine.
  Command(name = "section", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Query section",
          description = "Query section based on registration number")
  static class Section implements Runnable {
    private Logger logger = LoggerFactory.getLogger("query.section");

    @CommandLine.Option(names = "--term", description = "term to query from")
    private Integer term;
    @CommandLine.
    Option(names = "--semester", description = "semester: ja, sp, su, or fa")
    private String semester;
    @CommandLine.Option(names = "--year", description = "year to scrape from")
    private Integer year;
    @CommandLine.
    Option(names = "--registration-number",
           description = "registration number for specific catalog",
           required = true)
    private Integer registrationNumber;
    @CommandLine.
    Option(names = "--output-file", description = "output file to write to")
    private String outputFile;

    public void run() {
      long start = System.nanoTime();
      Term term;
      if (this.term == null && this.semester == null && this.year == null) {
        throw new IllegalArgumentException(
            "Must provide at least one. Either --term OR --semester AND --year");
      } else if (this.term == null) {
        if (this.semester == null || this.year == null) {
          throw new IllegalArgumentException(
              "Must provide both --semester AND --year");
        }
        term = new Term(Semester.fromCode(this.semester), year);
      } else {
        term = Term.fromId(this.term);
      }
      UtilsKt.writeToFileOrStdout(
          outputFile, Query_sectionKt.querySection(term, registrationNumber));
      long end = System.nanoTime();
      logger.info((end - start) / 1000000000 + " seconds");
    }
  }

  @CommandLine.
  Command(name = "school", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Query school",
          description = "Query school based on term")
  static class School implements Runnable {
    private Logger logger = LoggerFactory.getLogger("query.school");

    @CommandLine.Option(names = "--term", description = "term to query from")
    private Integer term;
    @CommandLine.
    Option(names = "--semester", description = "semester: ja, sp, su, or fa")
    private String semester;
    @CommandLine.Option(names = "--year", description = "year to scrape from")
    private Integer year;
    @CommandLine.
    Option(names = "--output-file", description = "output file to write to")
    private String outputFile;

    public void run() {
      long start = System.nanoTime();
      Term term;
      if (this.term == null && this.semester == null && this.year == null) {
        throw new IllegalArgumentException(
            "Must provide at least one. Either --term OR --semester AND --year");
      } else if (this.term == null) {
        if (this.semester == null || this.year == null) {
          throw new IllegalArgumentException(
              "Must provide both --semester AND --year");
        }
        term = new Term(Semester.fromCode(this.semester), year);
      } else {
        term = Term.fromId(this.term);
      }
      UtilsKt.writeToFileOrStdout(outputFile, Query_schoolKt.querySchool(term));
      long end = System.nanoTime();
      logger.info((end - start) / 1000000000 + " seconds");
    }
  }
}
