package cli;

import java.io.IOException;
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

    @CommandLine.Option(names = "--term", required = true) private String term;

    @CommandLine.Option(names = "--registrationNumber", required = true)
    private String registrationNumber;

    @CommandLine.Option(names = "--outputFile") private String outputFile;

    @CommandLine.Option(names = "--pretty") private String pretty;

    public void run() {
      long start = System.nanoTime();
      try {
        UtilsKt.writeToFileOrStdout(
            outputFile, JsonMapper.toJson(Scrape_sectionKt.scrapeFromSection(
                            Term.fromId(Integer.parseInt(term)),
                            Integer.parseInt(registrationNumber))));
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

    @CommandLine.Option(names = "--term", required = true) private String term;

    @CommandLine.Option(names = "--school") private String school;

    @CommandLine.Option(names = "--subject") private String subject;

    @CommandLine.Option(names = "--outputFile") private String outputFile;

    @CommandLine.Option(names = "--batchSize") private String batchSize;

    @CommandLine.Option(names = "--pretty") private String pretty;

    public void run() {
      long start = System.nanoTime();
      String school = this.school;
      String subject = this.subject;
      Term term = Term.fromId(Integer.parseInt(this.term));
      if (school != null && subject != null) {
        try {
          UtilsKt.writeToFileOrStdout(
              outputFile,
              JsonMapper.toJson(Scrape_sectionKt.scrapeFromCatalogSection(
                                    term,
                                    new SubjectCode(school.toUpperCase(),
                                                    subject.toUpperCase()),
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

    @CommandLine.Option(names = "--term", required = true) private String term;

    @CommandLine.Option(names = "--school") private String school;

    @CommandLine.Option(names = "--subject") private String subject;

    @CommandLine.Option(names = "--outputFile") private String outputFile;

    @CommandLine.Option(names = "--batchSize") private Integer batchSize;

    @CommandLine.Option(names = "--pretty") private String pretty;

    public void run() {
      long start = System.nanoTime();
      String school = this.school;
      String subject = this.subject;
      Term term = Term.fromId(Integer.parseInt(this.term));
      if (school == null) {
        if (subject != null) {
          throw new IllegalArgumentException(
              "--subject doesn't make sense if school is null");
        }
        try {
          UtilsKt.writeToFileOrStdout(
              JsonMapper.toJson(Scrape_catalogKt.scrapeFromCatalog(
                                    term, SubjectCode.allSubjects(), batchSize),
                                Boolean.parseBoolean(pretty)),
              outputFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else if (subject == null) {
        try {
          UtilsKt.writeToFileOrStdout(
              JsonMapper.toJson(Scrape_catalogKt.scrapeAllFromCatalog(
                                    term, school, batchSize),
                                Boolean.parseBoolean(pretty)),
              outputFile);
        } catch (IOException e) {
          logger.warn(e.getMessage());
        }
      } else {
        if (batchSize != null) {
          throw new IllegalArgumentException(
              "Batch size doesn't make sense when only doing on query");
        }
        try {
          UtilsKt.writeToFileOrStdout(
              JsonMapper.toJson(Scrape_catalogKt.scrapeFromCatalog(
                                    term, new SubjectCode(school, subject)),
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

  public static class School implements Runnable {
    private Logger logger = LoggerFactory.getLogger("scrape.school");

    @CommandLine.Option(names = "--term") private String term;

    @CommandLine.Option(names = "--outputFile") private String outputFile;

    @CommandLine.Option(names = "--pretty") private String pretty;

    public void run() {
      long start = System.nanoTime();
      Term term = Term.fromId(Integer.parseInt(this.term));
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
