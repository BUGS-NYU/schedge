package cli;

import api.App;
import cli.validation.ValidateCatalogArgs;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;
import nyu.SubjectCode;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import scraping.ScrapeCatalog;
import services.*;
import utils.Utils;

@CommandLine.
Command(name = "db",
        synopsisSubcommandLabel = "(scrape | query | update | serve)",
        subcommands = {Database.Scrape.class, Database.Query.class,
                       Database.Serve.class, Database.Update.class})
public class Database implements Runnable {
  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(),
                                             "Missing required subcommand");
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

    @Override
    public void run() {
      long start = System.nanoTime();

      ValidateCatalogArgs
          .validate(term, semester, year, school, subject, batchSize,
                    (t, obj) -> {
                      if (obj instanceof Stream) {
                        ((Stream<List<scraping.models.Course>>)obj)
                            .iterator()
                            .forEachRemaining(courseList -> {
                              InsertCourses.insertCourses(t, courseList);
                            });

                      } else {
                        InsertCourses.insertCourses(
                            t, (List<scraping.models.Course>)obj);
                      }
                    })

          .andRun(ScrapeCatalog::scrapeFromCatalog,
                  ScrapeCatalog::scrapeFromCatalog);

      GetConnection.close();
      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info(duration + " seconds");
    }
  }

  @CommandLine.Command(
      name = "update", sortOptions = false, headerHeading = "Usage:%n%n",
      synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n", header = "Scrape section",
      description =
          "Update section information based on term and registration number, OR school and subject")
  public static class Update implements Runnable {
    private Logger logger = LoggerFactory.getLogger("scrape.section");

    @CommandLine.Option(names = "--term", description = "term to query from")
    private Integer termId;
    @CommandLine.
    Option(names = "--semester", description = "semester: ja, sp, su, or fa")
    private String semester;
    @CommandLine.Option(names = "--year", description = "year to scrape from")
    private Integer year;
    @CommandLine.
    Option(names = "--batch-size",
           description = "batch size if query more than one catalog")
    private Integer batchSize;

    public void run() {
      long start = System.nanoTime();
      Term term;
      if (termId == null && semester == null && year == null) {
        throw new IllegalArgumentException(
            "Must provide at least one. Either --term OR --semester AND --year");
      }

      if (termId == null) {
        if (semester == null || year == null) {
          throw new IllegalArgumentException(
              "Must provide both --semester AND --year");
        }
        term = new Term(semester, year);
      } else {
        term = Term.fromId(termId);
      }

      try {
        UpdateSections.updateSections(term, batchSize);
      } catch (SQLException e) {
        e.printStackTrace();
      }
      GetConnection.close();
      long end = System.nanoTime();
      double duration = (end - start) / 1000000000.0;
      logger.info(duration + "seconds");
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

    @Override
    public void run() {
      long start = System.nanoTime();
      List<api.models.Course> courses = null;
      Term term;
      if (this.term == null && this.semester == null && this.year == null) {
        throw new IllegalArgumentException(
            "Must provide at least one. Either --term OR --semester AND --year");
      } else if (this.term == null) {
        if (this.semester == null || this.year == null) {
          throw new IllegalArgumentException(
              "Must provide both --semester AND --year");
        }
        term = new Term(Term.semesterFromString(this.semester), year);
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

      GetConnection.close();
      Utils.writeToFileOrStdout(outputFile, JsonMapper.toJson(courses));

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
    @Override
    public void run() {
      App.run();
    }
  }
}
