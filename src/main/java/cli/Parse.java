package cli;

import cli.templates.InputFileMixin;
import cli.templates.OutputFileMixin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import scraping.parse.ParseCatalog;
import scraping.parse.ParseSchoolSubjects;
import scraping.parse.ParseSection;

import java.util.Map;
/*
   @Todo: Add annotation for parameter. Fix the method to parse
   @Help: Add annotations, comments to code
*/
@CommandLine.
Command(name = "parse",
        description = "Parsing NYU data based on different categories",
        synopsisSubcommandLabel = "(catalog | section | school)",
        subcommands = {Parse.School.class})
public class Parse implements Runnable {
  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;
  @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
                      description = "display a help message")
  boolean displayHelp;

  Logger logger = LoggerFactory.getLogger("cli.Parse");
  @Override
  public void run() {
    throw new CommandLine.ParameterException(
        spec.commandLine(),
        "\nMissing required subcommand. Try ./schedge parse [subcommand] --help to"
            + " display help message for possible subcommands");
  }

  @CommandLine.
  Command(name = "section", sortOptions = false,
          headerHeading = "Command: ", descriptionHeading = "%nDescription:%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n", header = "Parse section data",
          description = "Parse section based on term, subject codes, "
                        + "or school for one or multiple subjects/schools")
  public void
  section(@CommandLine.Mixin InputFileMixin in,
          @CommandLine.Mixin OutputFileMixin out) {
    long start = System.nanoTime();
    out.writeOutput(ParseSection.parse(in.getInput()));
    long end = System.nanoTime();
    long duration = (end - start) / 1000000000;
    logger.info(duration + " seconds");
  }

  @CommandLine.Command(
      name = "catalog", sortOptions = false,
      headerHeading = "Command: ", descriptionHeading = "%nDescription:%n",
      parameterListHeading = "%nParameters:%n",
      optionListHeading = "%nOptions:%n", header = "Parse catalog data",
      description =
          "Parse catalog based on input file. If not provided, read from stdin")
  public void
  catalog(@CommandLine.Mixin InputFileMixin in,
          @CommandLine.Mixin OutputFileMixin out) {
    long start = System.nanoTime();
    out.writeOutput(ParseCatalog.parse(in.getInput(), null));
    long end = System.nanoTime();
    long duration = (end - start) / 1000000000;
    logger.info(duration + " seconds");
  }

  // @ToDo: Fix this with two options
  @CommandLine.
  Command(name = "school", sortOptions = false,
          headerHeading = "Command: ", descriptionHeading = "%nDescription:%n",
          parameterListHeading = "%nParameters:%n",
          optionListHeading = "%nOptions:%n",
          header = "Parse school/subject data",
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
    @CommandLine.Mixin private InputFileMixin inputFile;
    @CommandLine.Mixin private OutputFileMixin outputFile;

    private static class SchoolSubjectPair {
      Map<String, String> schools, subjects;
      SchoolSubjectPair(Map<String, String> subjects,
                        Map<String, String> schools) {
        this.schools = schools;
        this.subjects = subjects;
      }
      Map<String, String> getSchools() { return schools; }
      Map<String, String> getSubjects() { return subjects; }
    }

    @Override
    public void run() {
      long start = System.nanoTime();
      String input = inputFile.getInput();
      if (school == null && subject == null) {
        Map<String, String> schools = ParseSchoolSubjects.parseSchool(input);
        Map<String, String> subjects = ParseSchoolSubjects.parseSubject(input);
        outputFile.writeOutput(new SchoolSubjectPair(subjects, schools));
      } else if (subject == null) {
        Map<String, String> subjects = ParseSchoolSubjects.parseSubject(input);
        outputFile.writeOutput(subjects);
      } else {
        Map<String, String> schools = ParseSchoolSubjects.parseSchool(input);
        outputFile.writeOutput(schools);
      }
      long end = System.nanoTime();
      long duration = (end - start) / 1000000000;
      logger.info(duration + " seconds");
    }
  }
}
