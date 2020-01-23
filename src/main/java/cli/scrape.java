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
@CommandLine.Command(name = "scrape")
public class scrape {

  public static class Section implements Runnable {
    private Logger logger = LoggerFactory.getLogger("scrape.section");

    @CommandLine.Option(names = "--term") private Integer term;
    @CommandLine.Option(names = "--semester") private String semester;
    @CommandLine.Option(names = "--year") private Integer year;
    @CommandLine.Option(names = "--registrationNumber", required = true)
    private String registrationNumber;
    @CommandLine.Option(names = "--outputFile") private String outputFile;
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

  public static class Sections implements Runnable {
    private Logger logger = LoggerFactory.getLogger("scrape.sections");

    @CommandLine.Option(names = "--term") private Integer term;
    @CommandLine.Option(names = "--semester") private String semester;
    @CommandLine.Option(names = "--year") private Integer year;
    @CommandLine.Option(names = "--school") private String school;
    @CommandLine.Option(names = "--subject") private String subject;
    @CommandLine.Option(names = "--outputFile") private String outputFile;
    @CommandLine.Option(names = "--batchSize") private String batchSize;
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
                                    Integer.parseInt(batchSize)),
                                Boolean.parseBoolean(pretty)));
        } catch (IOException e) {
          logger.warn(e.getMessage());
        }
      } else if (subject == null) {
        try {
          UtilsKt.writeToFileOrStdout(
              JsonMapper.toJson(Scrape_sectionKt.scrapeFromCatalogSection(
                                    term, school, Integer.parseInt(batchSize)),
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
                                    Integer.parseInt(batchSize)),
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

  public static class Catalog implements Runnable {
    private Logger logger = LoggerFactory.getLogger("scrape.catalog");

    @CommandLine.Option(names = "--term") private Integer term;
    @CommandLine.Option(names = "--semester") private String semester;
    @CommandLine.Option(names = "--year") private Integer year;
    @CommandLine.Option(names = "--school") private String school;
    @CommandLine.Option(names = "--subject") private String subject;
    @CommandLine.Option(names = "--output-file") private String outputFile;
    @CommandLine.Option(names = "--batch-size") private Integer batchSize;
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

  public static class School implements Runnable {
    private Logger logger = LoggerFactory.getLogger("scrape.school");

    @CommandLine.Option(names = "--term") private Integer term;
    @CommandLine.Option(names = "--semester") private String semester;
    @CommandLine.Option(names = "--year") private Integer year;
    @CommandLine.Option(names = "--outputFile") private String outputFile;
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
