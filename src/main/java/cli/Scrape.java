package cli;

import cli.validation.*;
import java.util.stream.Collectors;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import scraping.ScrapeCatalog;
import scraping.ScrapeSection;
import scraping.query.QuerySchool;
import scraping.query.QuerySection;
import services.*;
import utils.Utils;

/*
   @Todo: Add annotation for parameter. Fix the method to parse
   @Help: Add annotations, comments to code
*/
@CommandLine.
Command(name = "scrape",
        synopsisSubcommandLabel = "(master | catalog | sections | school)",
        subcommands = {Scrape.Catalog.class, Scrape.Sections.class,
                       Scrape.School.class})
public class Scrape implements Runnable {
  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(),
                                             "Missing required subcommand");
  }

  @CommandLine.Command(
      name = "sections", sortOptions = false, headerHeading = "Usage:%n%n",
      synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n", header = "Scrape section",
      description =
          "Scrape section based on term and registration number, OR school and subject")
  public static class Sections implements Runnable {
    private Logger logger = LoggerFactory.getLogger("scrape.section");

    @CommandLine.Option(names = "--term", description = "term to query from")
    private Integer term;
    @CommandLine.
    Option(names = "--semester", description = "semester: ja, sp, su, or fa")
    private String semester;
    @CommandLine.Option(names = "--year", description = "year to scrape from")
    private Integer year;
    @CommandLine.
    Option(names = "--registration-number",
           description = "registration number for specific catalog")
    private Integer registrationNumber;
    @CommandLine.
    Option(names = "--school", description = "school code: UA, UT, UY, etc")
    private String school;
    @CommandLine.
    Option(names = "--subject", description = "subject code: CSCI, MA, etc")
    private String subject;
    @CommandLine.
    Option(names = "--batch-size",
           description = "batch size if query more than one catalog")
    private Integer batchSize;
    @CommandLine.
    Option(names = "--output-file", description = "output file to write to")
    private String outputFile;
    @CommandLine.Option(names = "--pretty", defaultValue = "true")
    private boolean pretty;

    public void run() {
      long start = System.nanoTime();
      ValidateSectionArgs
          .validate(term, semester, year, registrationNumber, school, subject,
                    batchSize, outputFile, pretty)
          .andRun((term, list, batchSize)
                      -> ScrapeSection.scrapeFromSection(term, list, batchSize)
                             .collect(Collectors.toList()),
                  ScrapeSection::scrapeFromSection);
      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info(duration + "seconds");
    }
  }

  @CommandLine.Command(
      name = "catalog", sortOptions = false, headerHeading = "Usage:%n%n",
      synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n", header = "Scrape catalog",
      description =
          "Scrape catalog based on term, subject codes, or school for one or multiple subjects/schools")
  public static class Catalog implements Runnable {
    private Logger logger = LoggerFactory.getLogger("scrape.catalog");

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
    @CommandLine.Option(names = "--pretty", defaultValue = "true")
    private boolean pretty;

    public void run() {
      long start = System.nanoTime();
      ValidateCatalogArgs
          .validate(term, semester, year, school, subject, batchSize,
                    outputFile, pretty)
          .andRun((term, list, batchSize)
                      -> ScrapeCatalog.scrapeFromCatalog(term, list, batchSize)
                             .collect(Collectors.toList()),
                  ScrapeCatalog::scrapeFromCatalog);
      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info(duration + " seconds");
    }
  }

  @CommandLine.
  Command(name = "school", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Scrape school/subject",
          description = "Scrape school/subject based on term")
  public static class School implements Runnable {
    private Logger logger = LoggerFactory.getLogger("scrape.school");

    @CommandLine.Option(names = "--term", description = "term to query from")
    private Integer termId;
    @CommandLine.
    Option(names = "--semester", description = "semester: ja, sp, su, or fa")
    private String semester;
    @CommandLine.Option(names = "--year", description = "year to scrape from")
    private Integer year;
    @CommandLine.Option(
        names = "--school",
        description =
            "Enter no if not want. If none provided, will read the school values")
    private String school;
    @CommandLine.Option(
        names = "--subject",
        description =
            "Enter no if not want. If none provided, will read the subject values")
    private String subject;
    @CommandLine.
    Option(names = "--batch-size",
           description = "batch size if query more than one catalog")
    private Integer batchSize;
    @CommandLine.
    Option(names = "--output-file", description = "output file to write to")
    private String outputFile;
    @CommandLine.Option(names = "--pretty") private String pretty;

    public void run() {
      long start = System.nanoTime();
      Term term;
      if (termId == null && semester == null && year == null) {
        throw new IllegalArgumentException(
            "Must provide at least one. Either --term OR --semester AND --year");
      } else if (termId == null) {
        if (semester == null || year == null) {
          throw new IllegalArgumentException(
              "Must provide both --semester AND --year");
        }
        term = new Term(semester, year);
      } else {
        term = Term.fromId(termId);
      }
      if (school == null && subject == null) {
        Utils.writeToFileOrStdout(
            outputFile, JsonMapper.toJson(ParseSchoolSubjects.parseSchool(
                                              QuerySchool.querySchool(term)),
                                          Boolean.parseBoolean(pretty)));
        Utils.writeToFileOrStdout(
            outputFile, JsonMapper.toJson(ParseSchoolSubjects.parseSubject(
                                              QuerySchool.querySchool(term)),
                                          Boolean.parseBoolean(pretty)));
      } else if (school != null) {
        Utils.writeToFileOrStdout(
            outputFile, JsonMapper.toJson(ParseSchoolSubjects.parseSchool(
                                              QuerySchool.querySchool(term)),
                                          Boolean.parseBoolean(pretty)));
      } else {
        Utils.writeToFileOrStdout(
            outputFile, JsonMapper.toJson(ParseSchoolSubjects.parseSchool(
                                              QuerySchool.querySchool(term)),
                                          Boolean.parseBoolean(pretty)));
      }
      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info(duration + " seconds");
    }
  }
}
