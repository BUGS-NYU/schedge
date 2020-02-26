package cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.util.Json;
import java.io.IOException;
import models.Semester;
import models.SubjectCode;
import models.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import scraping.models.Subject;
import services.*;
import utils.UtilsKt;

/*
   @Todo: Add annotation for parameter. Fix the method to parse
   @Help: Add annotations, comments to code
*/
@CommandLine.Command(name = "scrape",
                     synopsisSubcommandLabel =
                         "(master | catalog | section | sections | school)",
                     subcommands = {scrape.Master.class, scrape.Catalog.class,
                                    scrape.Sections.class, scrape.School.class})
public class scrape implements Runnable {
  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(),
                                             "Missing required subcommand");
  }

  @CommandLine.
  Command(name = "master", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Scrape section",
          description = "Scrape sections and update accordingly")
  public static class Master implements Runnable {
    private Logger logger = LoggerFactory.getLogger("scrape.master");

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
    Option(names = "--subject", description = "subject code: CSCI, MA, etc")
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
      if (school == null) {
        if (subject != null) {
          throw new IllegalArgumentException(
              "Subject does not make sense here");
        }
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile,
              JsonMapper.toJson(
                  Master_scraperKt
                      .masterScrapeSection(term, SubjectCode.allSubjects(),
                                           batchSize)
                      .iterator(),
                  Boolean.parseBoolean(pretty)));
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
      } else if (subject == null) {
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile,
              JsonMapper.toJson(
                  Master_scraperKt.masterScrapeSection(term, school, batchSize)
                      .iterator(),
                  Boolean.parseBoolean(pretty)));
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
      } else {
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile,
              JsonMapper.toJson(Master_scraperKt.masterScrapeSection(
                                    term, new SubjectCode(subject, school)),
                                Boolean.parseBoolean(pretty)));
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
      }
      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info(duration + "seconds");
    }
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
    @CommandLine.Option(names = "--pretty") private String pretty;

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
      if ((subject != null && school != null && registrationNumber != null)) {
        throw new IllegalArgumentException(
            "Must provide either --registration-number OR "
            + "--subject AND --term");
      }

      if (subject == null && school == null) {
        if (batchSize != null) {
          throw new IllegalArgumentException(
              "--batch-size doesn't make sense if scrape one catalog");
        }
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile, JsonMapper.toJson(Scrape_sectionKt.scrapeFromSection(
                              term, registrationNumber)));
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else if (school != null && subject != null) {
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile,
              JsonMapper.toJson(
                  Scrape_sectionKt
                      .scrapeFromCatalogSection(
                          term, new SubjectCode(this.subject, this.school),
                          batchSize)
                      .iterator(),
                  Boolean.parseBoolean(pretty)));
        } catch (IOException e) {
          logger.warn(e.getMessage());
        }
      } else if (subject == null) {
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile, JsonMapper.toJson(Scrape_sectionKt
                                                .scrapeFromCatalogSection(
                                                    term, school, batchSize)
                                                .iterator(),
                                            Boolean.parseBoolean(pretty)));
        } catch (IOException e) {
          logger.warn(e.getMessage());
        }
      } else {
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile,
              JsonMapper.toJson(
                  Scrape_sectionKt
                      .scrapeFromAllCatalogSection(
                          term, SubjectCode.allSubjects(), batchSize)
                      .iterator(),
                  Boolean.parseBoolean(pretty)));
        } catch (IOException e) {
          logger.warn(e.getMessage());
        }
      }
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
    @CommandLine.Option(names = "--pretty", defaultValue = "false")
    private String pretty;

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
      if (school == null) {
        if (subject != null) {
          throw new IllegalArgumentException(
              "--subject doesn't make sense if school is null");
        }
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile,
              JsonMapper.toJson(
                  Scrape_catalogKt
                      .scrapeFromCatalog(term, SubjectCode.allSubjects(),
                                         batchSize)
                      .iterator(),
                  Boolean.parseBoolean(pretty)));
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
      } else if (subject == null) {
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile,
              JsonMapper.toJson(
                  Scrape_catalogKt.scrapeAllFromCatalog(term, school, batchSize)
                      .iterator(),
                  Boolean.parseBoolean(pretty)));
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
      } else {
        if (batchSize != null) {
          throw new IllegalArgumentException(
              "Batch size doesn't make sense when only doing on query");
        }
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile,
              JsonMapper.toJson(
                  Scrape_catalogKt
                      .scrapeFromCatalog(term, new SubjectCode(subject, school))
                      .iterator(),
                  Boolean.parseBoolean(pretty)));
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
      }
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
    private Integer term;
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
      if (school == null && subject == null) {
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile,
              JsonMapper.toJson(ParseSchoolSubjects.parseSchool(
                                    Query_schoolKt.querySchool(term)),
                                Boolean.parseBoolean(pretty)));
          UtilsKt.writeToFileOrStdout(
              outputFile,
              JsonMapper.toJson(ParseSchoolSubjects.parseSubject(
                                    Query_schoolKt.querySchool(term)),
                                Boolean.parseBoolean(pretty)));
        } catch (IOException e) {
          logger.warn(e.getMessage());
        }
      } else if (school != null) {
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile,
              JsonMapper.toJson(ParseSchoolSubjects.parseSchool(
                                    Query_schoolKt.querySchool(term)),
                                Boolean.parseBoolean(pretty)));
        } catch (IOException e) {
          logger.warn(e.getMessage());
        }
      } else {
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile,
              JsonMapper.toJson(ParseSchoolSubjects.parseSchool(
                                    Query_schoolKt.querySchool(term)),
                                Boolean.parseBoolean(pretty)));
        } catch (IOException e) {
          logger.warn(e.getMessage());
        }
      }
      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info(duration + " seconds");
    }
  }
}
