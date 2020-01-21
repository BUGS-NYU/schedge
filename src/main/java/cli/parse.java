package cli;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import scraping.models.Course;
import scraping.models.SectionAttribute;
import services.*;

/*
    @Todo: Add annotation for parameter. Fix the method to parse
    @Help: Add annotations, comments to code
 */
@CommandLine.Command(name = "parse")
public class parse implements Runnable {
  @Override
  public void run() {
    return;
  }

  @CommandLine.Command(name = "section")
  public static class Section implements Runnable {
    private Logger logger = LoggerFactory.getLogger("parse.section");

    @CommandLine.Option(names = "--pretty", defaultValue = "false")
    private String pretty;

    @CommandLine.Option(names = "--inputFile") private String inputFile;

    @CommandLine.Option(names = "--outputFile") private String outputFile;

    @Override
    public void run() {
      try {
        long start = System.nanoTime();
        String input = Utils.readFromFileOrStdin(inputFile);
        SectionAttribute output = ParseSection.parse(input);
        Utils.writeToFileOrStdOut(
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

  @CommandLine.Command(name = "catalog")
  public static class Catalog implements Runnable {
    private Logger logger = LoggerFactory.getLogger("parse.catalog");

    @CommandLine.Option(names = "--pretty", defaultValue = "false")
    private String pretty;

    @CommandLine.Option(names = "--inputFile") private String inputFile;

    @CommandLine.Option(names = "--outputFile") private String outputFile;

    @Override
    public void run() {
      try {
        long start = System.nanoTime();
        String input = Utils.readFromFileOrStdin(inputFile);
        List<Course> output = ParseCatalog.parse(logger, input);
        Utils.writeToFileOrStdOut(
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

  @CommandLine.Command(name = "section")
  public static class School implements Runnable {
    private Logger logger = LoggerFactory.getLogger("parse.catalog");

    @CommandLine.Option(names = "--pretty", defaultValue = "false")
    private String pretty;

    @CommandLine.Option(names = "--inputFile") private String inputFile;

    @CommandLine.Option(names = "--outputFile") private String outputFile;

    @Override
    public void run() {
      try {
        long start = System.nanoTime();
        String input = Utils.readFromFileOrStdin(inputFile);
        Map<String, String> schools = ParseSchoolSubjects.parseSchool(input);
        Map<String, String> subjects = ParseSchoolSubjects.parseSubject(input);
        Utils.writeToFileOrStdOut(
            JsonMapper.toJson(schools, Boolean.parseBoolean(pretty)),
            outputFile);

        Utils.writeToFileOrStdOut(
            JsonMapper.toJson(subjects, Boolean.parseBoolean(pretty)),
            outputFile);
        long end = System.nanoTime();
        long duration = (end - start) / 1000000000;
        logger.info(duration + " seconds");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
