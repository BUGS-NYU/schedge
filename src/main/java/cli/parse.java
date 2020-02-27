package cli;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import scraping.models.Course;
import scraping.models.SectionAttribute;
import services.JsonMapper;
import services.ParseCatalog;
import services.ParseSchoolSubjects;
import services.ParseSection;
import utils.UtilsKt;

/*
   @Todo: Add annotation for parameter. Fix the method to parse
   @Help: Add annotations, comments to code
*/
@CommandLine.Command(name = "parse",
                     synopsisSubcommandLabel = "(catalog | section | school)",
                     subcommands = {parse.Catalog.class, parse.Section.class,
                                    parse.School.class})
public class parse implements Runnable {
  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(),
                                             "Missing required subcommand");
  }

  @CommandLine.
  Command(name = "section", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Parse catalog",
          description = "Parse catalog based on term, subject codes, "
                        + "or school for one or multiple subjects/schools")
  public static class Section implements Runnable {
    private Logger logger = LoggerFactory.getLogger("parse.section");

    @CommandLine.Option(names = "--pretty", defaultValue = "false")
    private String pretty;
    @CommandLine.
    Option(names = "--input-file",
           description =
               "intput file to read from. If none provided, read from stdout")
    private String inputFile;
    @CommandLine.
    Option(names = "--output-file",
           description =
               "output file to write to. If none provided, write to stdout")
    private String outputFile;

    @Override
    public void run() {
      try {
        long start = System.nanoTime();
        String input = UtilsKt.readFromFileOrStdin(inputFile);
        SectionAttribute output = ParseSection.parse(input);
        UtilsKt.writeToFileOrStdout(
            outputFile,
            JsonMapper.toJson(output, Boolean.parseBoolean(pretty)));
        long end = System.nanoTime();
        long duration = (end - start) / 1000000000;
        logger.info(duration + " seconds");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @CommandLine.Command(
      name = "catalog", sortOptions = false, headerHeading = "Usage:%n%n",
      synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n", header = "Parse catalog",
      description =
          "Parse catalog based on input file. If not provided, read from stdin")
  public static class Catalog implements Runnable {
    private Logger logger = LoggerFactory.getLogger("parse.catalog");

    @CommandLine.Option(names = "--pretty", defaultValue = "false")
    private String pretty;
    @CommandLine.
    Option(names = "--input-file",
           description =
               "intput file to read from. If none provided, read from stdout")
    private String inputFile;
    @CommandLine.
    Option(names = "--output-file",
           description =
               "output file to write to. If none provided, write to stdout")
    private String outputFile;

    @Override
    public void run() {
      try {
        long start = System.nanoTime();
        String input = UtilsKt.readFromFileOrStdin(inputFile);
        List<Course> output = ParseCatalog.parse(input, null);
        UtilsKt.writeToFileOrStdout(
            JsonMapper.toJson(output, Boolean.parseBoolean(pretty)),
            outputFile);
        long end = System.nanoTime();
        long duration = (end - start) / 1000000000;
        logger.info(duration + " seconds");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  // @ToDo: Fix this with two options
  @CommandLine.
  Command(name = "school", sortOptions = false, headerHeading = "Usage:%n%n",
          synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Parse school/subject",
          description = "Parse school/subject based on input file")
  public static class School implements Runnable {
    private Logger logger = LoggerFactory.getLogger("parse.catalog");

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
    @CommandLine.Option(names = "--pretty", defaultValue = "false")
    private String pretty;
    @CommandLine.
    Option(names = "--input-file",
           description =
               "intput file to read from. If none provided, read from stdout")
    private String inputFile;
    @CommandLine.
    Option(names = "--output-file",
           description =
               "output file to write to. If none provided, write to stdout")
    private String outputFile;

    @Override
    public void run() {
      long start = System.nanoTime();
      String input = UtilsKt.readFromFileOrStdin(inputFile);
      if (school == null && subject == null) {
        Map<String, String> schools = ParseSchoolSubjects.parseSchool(input);
        Map<String, String> subjects = ParseSchoolSubjects.parseSubject(input);
        UtilsKt.writeToFileOrStdout(
            JsonMapper.toJson(schools, Boolean.parseBoolean(pretty)),
            outputFile);

        UtilsKt.writeToFileOrStdout(
            JsonMapper.toJson(subjects, Boolean.parseBoolean(pretty)),
            outputFile);
      } else if (subject == null) {
        Map<String, String> subjects = ParseSchoolSubjects.parseSubject(input);
        UtilsKt.writeToFileOrStdout(
            JsonMapper.toJson(subjects, Boolean.parseBoolean(pretty)),
            outputFile);
      } else {
        Map<String, String> schools = ParseSchoolSubjects.parseSchool(input);
        UtilsKt.writeToFileOrStdout(
            JsonMapper.toJson(schools, Boolean.parseBoolean(pretty)),
            outputFile);
      }
      long end = System.nanoTime();
      long duration = (end - start) / 1000000000;
      logger.info(duration + " seconds");
    }
  }
}
