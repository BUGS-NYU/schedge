package cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import models.Semester;
import models.SubjectCode;
import models.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import services.*;
import utils.UtilsKt;

/*
   @Todo: Add annotation for parameter. Fix the method to parse
   @Help: Add annotations, comments to code
*/
@CommandLine.Command(
        name = "scrape", synopsisSubcommandLabel = "(catalog | section | sections | school)",
        subcommands = {scrape.Catalog.class, scrape.Section.class, scrape.Sections.class, scrape.School.class})
public class scrape implements Runnable {
  @CommandLine.Spec
  private CommandLine.Model.CommandSpec spec;

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(),
            "Missing required subcommand");
  }


  @CommandLine.Command(
          name = "section",
          sortOptions = false,
          headerHeading = "Usage:%n%n",
          synopsisHeading = "%n",
          descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n",
          header = "Scrape section",
          description = "Scrape section based on term and registration number")
  public static class Section implements Runnable {
    private Logger logger = LoggerFactory.getLogger("scrape.section");

    @CommandLine.Option(names = "--term", description = "term to query from") private Integer term;
    @CommandLine.Option(names = "--semester", description = "semester: ja, sp, su, or fa") private String semester;
    @CommandLine.Option(names = "--year", description = "year to scrape from") private Integer year;
    @CommandLine.Option(names = "--batch-size", description = "batch size if query more than one catalog") private Integer batchSize;
    @CommandLine.Option(names = "--output-file", description = "output file to write to") private String outputFile;
    @CommandLine.Option(names = "--registration-number", required = true, description = "registration number for specific catalog")
    private String registrationNumber;

    public void run() {
      long start = System.nanoTime();
      Term term;
      if (this.term == null && this.semester == null && this.year == null) {
        throw new IllegalArgumentException(
            "Must provide at least one. Either --term OR --semester AND --year");
      } else if (this.term == null) {
        if(this.semester == null || this.year == null) {
          throw new IllegalArgumentException(
                  "Must provide both --semester AND --year"
          );
        }
        term = new Term(Semester.fromCode(this.semester), year);
      } else {
        term = Term.fromId(this.term);
      }
      try {
        UtilsKt.writeToFileOrStdout(
            outputFile, JsonMapper.toJson(Scrape_sectionKt.scrapeFromSection(
                            term, Integer.parseInt(registrationNumber))));
      } catch (IOException e) {
        e.printStackTrace();
      }
      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info(duration + "seconds");
    }
  }


  @CommandLine.Command(
          name = "sections",
          sortOptions = false,
          headerHeading = "Usage:%n%n",
          synopsisHeading = "%n",
          descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n",
          header = "Scrape multiple sections",
          description = "Scrape multiple sections based on term, subject codes, or school for one or multiple subjects/schools")
  public static class Sections implements Runnable {
    private Logger logger = LoggerFactory.getLogger("scrape.sections");

    @CommandLine.Option(names = "--term", description = "term to query from") private Integer term;
    @CommandLine.Option(names = "--semester", description = "semester: ja, sp, su, or fa") private String semester;
    @CommandLine.Option(names = "--year", description = "year to scrape from") private Integer year;
    @CommandLine.Option(names = "--school", description = "school code: UA, UT, UY, etc") private String school;
    @CommandLine.Option(names = "--subject", description = "subject code: CSCI(Computer Science), MA(Math), etc")
    private String subject;
    @CommandLine.Option(names = "--batch-size", description = "batch size if query more than one catalog") private Integer batchSize;
    @CommandLine.Option(names = "--output-file", description = "output file to write to") private String outputFile;
    @CommandLine.Option(names = "--pretty") private String pretty;

    public void run() {
      long start = System.nanoTime();
      Term term;
      if (this.term == null && this.semester == null && this.year == null) {
        throw new IllegalArgumentException(
            "Must provide at least one. Either --term OR --semester AND --year");
      } else if (this.term == null) {
        if(this.semester == null || this.year == null) {
          throw new IllegalArgumentException(
                  "Must provide both --semester AND --year"
          );
        }
        term = new Term(Semester.fromCode(this.semester), year);
      } else {
        term = Term.fromId(this.term);
      }

      if (school != null && subject != null) {
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile,
              JsonMapper.toJson(Scrape_sectionKt.scrapeFromCatalogSection(
                                    term,
                                    new SubjectCode(this.subject, this.school),
                                    batchSize),
                                Boolean.parseBoolean(pretty)));
        } catch (IOException e) {
          logger.warn(e.getMessage());
        }
      } else if (subject == null) {
        try {
          UtilsKt.writeToFileOrStdout(
              JsonMapper.toJson(Scrape_sectionKt.scrapeFromCatalogSection(
                                    term, school, batchSize),
                                Boolean.parseBoolean(pretty)),
              outputFile);
        } catch (IOException e) {
          logger.warn(e.getMessage());
        }
      } else {
        try {
          UtilsKt.writeToFileOrStdout(
              JsonMapper.toJson(Scrape_catalogKt.scrapeFromCatalog(
                                    term, SubjectCode.allSubjects(),
                                    batchSize),
                                Boolean.parseBoolean(pretty)),
              outputFile);
        } catch (IOException e) {
          logger.warn(e.getMessage());
        }
      }
      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info(duration + " seconds");
    }
  }


  @CommandLine.Command(
          name = "catalog",
          sortOptions = false,
          headerHeading = "Usage:%n%n",
          synopsisHeading = "%n",
          descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n",
          header = "Scrape catalog",
          description = "Scrape catalog based on term, subject codes, or school for one or multiple subjects/schools")
  public static class Catalog implements Runnable {
    private Logger logger = LoggerFactory.getLogger("scrape.catalog");

    @CommandLine.Option(names = "--term", description = "term to query from") private Integer term;
    @CommandLine.Option(names = "--semester", description = "semester: ja, sp, su, or fa") private String semester;
    @CommandLine.Option(names = "--year", description = "year to scrape from") private Integer year;
    @CommandLine.Option(names = "--school", description = "school code: UA, UT, UY, etc") private String school;
    @CommandLine.Option(names = "--subject", description = "subject code: CSCI(Computer Science), MA(Math), etc")
    private String subject;
    @CommandLine.Option(names = "--batch-size", description = "batch size if query more than one catalog") private Integer batchSize;
    @CommandLine.Option(names = "--output-file", description = "output file to write to") private String outputFile;
    @CommandLine.Option(names = "--pretty", defaultValue = "false" ) private String pretty;

    public void run() {
      long start = System.nanoTime();
      Term term;
      if (this.term == null && this.semester == null && this.year == null) {
        throw new IllegalArgumentException(
            "Must provide at least one. Either --term OR --semester AND --year");
      } else if (this.term == null) {
        if(this.semester == null || this.year == null) {
          throw new IllegalArgumentException(
                  "Must provide both --semester AND --year"
          );
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
        Scrape_catalogKt
            .scrapeFromCatalog(term, SubjectCode.allSubjects(), batchSize)
            .iterator()
            .forEachRemaining(value -> {
              try {
                System.out.println(
                    JsonMapper.toJson(value, Boolean.parseBoolean(pretty)));
              } catch (JsonProcessingException e) {
                logger.warn(e.getMessage());
              }
            });
      } else if (subject == null) {
        Scrape_catalogKt.scrapeAllFromCatalog(term, school, batchSize)
            .iterator()
            .forEachRemaining(value -> {
              try {
                System.out.println(
                    JsonMapper.toJson(value, Boolean.parseBoolean(pretty)));
              } catch (JsonProcessingException e) {
                e.printStackTrace();
              }
            });
      } else {
        if (batchSize != null) {
          throw new IllegalArgumentException(
              "Batch size doesn't make sense when only doing on query");
        }
        try {
          System.out.println(
              JsonMapper.toJson(Scrape_catalogKt.scrapeFromCatalog(
                                    term, new SubjectCode(subject, school)),
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


  @CommandLine.Command(
          name = "school",
          sortOptions = false,
          headerHeading = "Usage:%n%n",
          synopsisHeading = "%n",
          descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n",
          header = "Scrape catalog",
          description = "Scrape catalog based on term, subject codes, or school for one or multiple subjects/schools")
  public static class School implements Runnable {
    private Logger logger = LoggerFactory.getLogger("scrape.school");

    @CommandLine.Option(names = "--term", description = "term to query from") private Integer term;
    @CommandLine.Option(names = "--semester", description = "semester: ja, sp, su, or fa") private String semester;
    @CommandLine.Option(names = "--year", description = "year to scrape from") private Integer year;
    @CommandLine.Option(names = "--school", description = "school code: UA, UT, UY, etc") private String school;
    @CommandLine.Option(names = "--subject", description = "subject code: CSCI(Computer Science), MA(Math), etc")
    private String subject;
    @CommandLine.Option(names = "--batch-size", description = "batch size if query more than one catalog") private Integer batchSize;
    @CommandLine.Option(names = "--output-file", description = "output file to write to") private String outputFile;
    @CommandLine.Option(names = "--pretty") private String pretty;

    public void run() {
      long start = System.nanoTime();
      Term term;
      if (this.term == null && this.semester == null && this.year == null) {
        throw new IllegalArgumentException(
            "Must provide at least one. Either --term OR --semester AND --year");
      } else if (this.term == null) {
        if(this.semester == null || this.year == null) {
          throw new IllegalArgumentException(
                  "Must provide both --semester AND --year"
          );
        }
        term = new Term(Semester.fromCode(this.semester), year);
      } else {
        term = Term.fromId(this.term);
      }
      try {
        UtilsKt.writeToFileOrStdout(
            JsonMapper.toJson(ParseSchoolSubjects.parseSchool(
                                  Query_schoolKt.querySchool(term)),
                              Boolean.parseBoolean(pretty)),
            outputFile);
        UtilsKt.writeToFileOrStdout(
            JsonMapper.toJson(ParseSchoolSubjects.parseSubject(
                                  Query_schoolKt.querySchool(term)),
                              Boolean.parseBoolean(pretty)),
            outputFile);
      } catch (IOException e) {
        logger.warn(e.getMessage());
      }
      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info(duration + " seconds");
    }
  }
}
