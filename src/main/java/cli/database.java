package cli;

import api.App;
import api.models.Course;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.SQLException;
import java.util.List;
import models.Semester;
import models.SubjectCode;
import models.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import services.*;
import utils.UtilsKt;

@CommandLine.
Command(name = "db", synopsisSubcommandLabel = "(scrape | query | serve)",
        subcommands = {database.Scrape.class, database.Scrape.class,
                       database.Serve.class})
public class database implements Runnable {
  private CommandLine.Model.CommandSpec spec;
  public void run() {
    throw new CommandLine.ParameterException(
        spec.commandLine(), ""
                                + "Missing arguments for command lines");
  }

  @CommandLine.Command(
      name = "scrape", sortOptions = false, headerHeading = "Usage:%n%n",
      synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n", header = "Scrape section from db",
      description =
          "Scrape section based on term and registration number, OR school and subject from db")
  public static class Scrape implements Runnable {
    private Logger logger = LoggerFactory.getLogger("db.scrape");
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
      if (this.school == null) {
        if (subject != null) {
          throw new IllegalArgumentException(
              "--subject doesn't make sense if school is null");
        }
        Scrape_catalogKt
            .scrapeFromCatalog(term, SubjectCode.allSubjects(), batchSize)
            .iterator()
            .forEachRemaining(course -> {
              try {
                InsertCourses.insertCourses(term, course);
              } catch (SQLException e) {
                e.printStackTrace();
              }
            });
      } else if (subject == null) {
        Scrape_catalogKt
            .scrapeFromCatalog(term, SubjectCode.allSubjects(school), batchSize)
            .iterator()
            .forEachRemaining(courses -> {
              try {
                InsertCourses.insertCourses(term, courses);
              } catch (SQLException e) {
                e.printStackTrace();
              }
            });
      } else {
        if (batchSize != null) {
          throw new IllegalArgumentException(
              "--batch-size doesn't make sense if scrape one catalog");
        }
        try {
          InsertCourses.insertCourses(
              term, Scrape_catalogKt.scrapeFromCatalog(
                        term, new SubjectCode(subject, school)));
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      try {
        GetConnection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }

      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info(duration + " seconds");
    }
  }

  @CommandLine.Command(
      name = "query", sortOptions = false, headerHeading = "Usage:%n%n",
      synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n", header = "Query section",
      description =
          "QUery section based on term and registration number, OR school and subject from db")
  public static class Query implements Runnable {
    private Logger logger = LoggerFactory.getLogger("db.query");
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
    @CommandLine.Option(names = "--pretty") private String pretty;
    @CommandLine.
    Option(names = "--output-file", description = "output file to write to")
    private String outputFile;

    public void run() {
      long start = System.nanoTime();
      List<Course> courses = null;
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
        courses = SelectCourses.selectCourses(term, SubjectCode.allSubjects());
      } else {
        try {
          courses = SelectCourses.selectCourses(
              term, new SubjectCode(subject, school));
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      try {
        GetConnection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }

      try {
        UtilsKt.writeToFileOrStdout(outputFile, JsonMapper.toJson(courses));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }

      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info(duration + " seconds");
    }
  }

  @CommandLine.
  Command(name = "serve", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Serve data",
          description = "Serve data through an API")
  public static class Serve implements Runnable {
    private Logger logger = LoggerFactory.getLogger("db.serve");
    public void run() { App.run(); }
  }
}
