package cli;

import models.Semester;
import models.SubjectCode;
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
@CommandLine.Command(name = "query", synopsisSubcommandLabel = "COMMAND")
public class query implements Runnable {
  @CommandLine.Spec CommandLine.Model.CommandSpec spec;

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(),
                                             "Missing required subcommand");
  }

  public static class Catalog implements Runnable {
    private Logger logger = LoggerFactory.getLogger("query.catalog");

    @CommandLine.Option(names = "--term") private Integer term;
    @CommandLine.Option(names = "--semester") private String semester;
    @CommandLine.Option(names = "--year") private Integer year;
    @CommandLine.Option(names = "--school") private String school;
    @CommandLine.Option(names = "--subject") private String subject;
    @CommandLine.Option(names = "--batch-size") private Integer batchSize;
    @CommandLine.Option(names = "--output-file") private String outputFile;

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
        Query_catalogKt.queryCatalog(term, SubjectCode.allSubjects(), batchSize)
            .iterator()
            .forEachRemaining(data -> System.out.println(data));
      } else if (subject == null) {
        Query_catalogKt
            .queryCatalog(term, SubjectCode.allSubjects(school), batchSize)
            .iterator()
            .forEachRemaining(data -> System.out.println(data));
      } else {
        System.out.println(Query_catalogKt.queryCatalog(
            term, new SubjectCode(subject, school)));
      }
      long end = System.nanoTime();
      logger.info((end - start) / 1000000000 + " seconds");
    }
  }

  public static class Section implements Runnable {
    private Logger logger = LoggerFactory.getLogger("query.section");

    @CommandLine.Option(names = "--term") private Integer term;
    @CommandLine.Option(names = "--semester") private String semester;
    @CommandLine.Option(names = "--year") private Integer year;
    @CommandLine.Option(names = "--registrationNumber", required = true)
    private Integer registrationNumber;
    @CommandLine.Option(names = "--outputFile") private String outputFile;

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
      UtilsKt.writeToFileOrStdout(
          Query_sectionKt.querySection(term, registrationNumber), outputFile);
      long end = System.nanoTime();
      logger.info((end - start) / 1000000000 + " seconds");
    }
  }

  public static class School implements Runnable {
    private Logger logger = LoggerFactory.getLogger("query.school");

    @CommandLine.Option(names = "--term") private Integer term;
    @CommandLine.Option(names = "--semester") private String semester;
    @CommandLine.Option(names = "--year") private Integer year;
    @CommandLine.Option(names = "--outputFile") private String outputFile;

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
      UtilsKt.writeToFileOrStdout(Query_schoolKt.querySchool(term), outputFile);
      long end = System.nanoTime();
      logger.info((end - start) / 1000000000 + " seconds");
    }
  }
}
