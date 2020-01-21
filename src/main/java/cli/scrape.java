package cli;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import scraping.models.SubjectCode;
import scraping.models.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import scraping.QuerySubject;
import scraping.ScrapeCatalog;
import scraping.ScrapeSection;
import services.JsonMapper;
import services.ParseSchoolSubjects;
import services.Utils;

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
        Utils.writeToFileOrStdOut(
            JsonMapper.toJson(ScrapeSection.scrapeFromSection(
                Term.fromId(Integer.parseInt(term)),
                Integer.parseInt(registrationNumber))),
            outputFile);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
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
          Utils.writeToFileOrStdOut(
              JsonMapper.toJson(ScrapeSection.scrapeFromCatalogSection(
                                    logger, term,
                                    new SubjectCode(school, subject),
                                    Integer.parseInt(batchSize)),
                                Boolean.valueOf(pretty)),
              outputFile);
        } catch (IOException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      } else if (subject == null) {
        try {
          Utils.writeToFileOrStdOut(
              JsonMapper.toJson(
                  ScrapeSection.scrapeFromCatalogSection(
                      logger, term, school, Integer.parseInt(batchSize)),
                  Boolean.valueOf(pretty)),
              outputFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        try {
          Utils.writeToFileOrStdOut(
              JsonMapper.toJson(ScrapeSection.scrapeFromAllCatalog(
                                    logger, term, SubjectCode.allSubjects(),
                                    Integer.parseInt(batchSize)),
                                Boolean.valueOf(pretty)),
              outputFile);
        } catch (IOException e) {
          e.printStackTrace();
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
          Utils.writeToFileOrStdOut(
              JsonMapper.toJson(
                  ScrapeCatalog.scrapeFromCatalog(
                      logger, term, SubjectCode.allSubjects(), batchSize),
                  Boolean.valueOf(pretty)),
              outputFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else if (subject == null) {
        try {
          Utils.writeToFileOrStdOut(
              JsonMapper.toJson(ScrapeCatalog.scrapeAllFromCatalog(
                                    logger, term, school, batchSize),
                                Boolean.valueOf(pretty)),
              outputFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        if (batchSize != null) {
          throw new IllegalArgumentException(
              "Batch size doesn't make sense when only doing on query");
        }
        try {
          Utils.writeToFileOrStdOut(
              JsonMapper.toJson(
                  ScrapeCatalog.scrapeFromCatalog(
                      logger, term, new SubjectCode(school, subject)),
                  Boolean.parseBoolean(pretty)),
              outputFile);
        } catch (IOException | InterruptedException | ExecutionException e) {
          e.printStackTrace();
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
        Utils.writeToFileOrStdOut(
            JsonMapper.toJson(ParseSchoolSubjects.parseSchool(
                                  QuerySubject.querySubject(term)),
                              Boolean.parseBoolean(pretty)),
            outputFile);
        Utils.writeToFileOrStdOut(
            JsonMapper.toJson(ParseSchoolSubjects.parseSubject(
                                  QuerySubject.querySubject(term)),
                              Boolean.parseBoolean(pretty)),
            outputFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info(duration + " seconds");
    }
  }
}
