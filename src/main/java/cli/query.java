package cli;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
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

    @CommandLine.Option(names = "--term", required = true) private Integer term;

    @CommandLine.Option(names = "--school")
    private String school;

    @CommandLine.Option(names = "--subject")
    private String subject;

    @CommandLine.Option(names = "--outputFile") private String outputFile;

    public void run() {
      long start = System.nanoTime();
      Term term = Term.fromId(this.term);
      SubjectCode subjectCode = null;

      UtilsKt.writeToFileOrStdout(outputFile, Query_catalogKt.queryCatalog(term, subjectCode));
      long end = System.nanoTime();
      logger.info((end - start) / 1000000000 + " seconds");
    }
  }

  public static class Section implements Runnable {
    private Logger logger = LoggerFactory.getLogger("query.section");

    @CommandLine.Option(names = "--term", required = true) private Integer term;

    @CommandLine.Option(names = "--registrationNumber", required = true)
    private Integer registrationNumber;

    @CommandLine.Option(names = "--outputFile") private String outputFile;

    public void run() {
      long start = System.nanoTime();
      Term term = Term.fromId(this.term);
      UtilsKt.writeToFileOrStdout(
            Query_sectionKt.querySection(term, registrationNumber), outputFile);
      long end = System.nanoTime();
      logger.info((end - start) / 1000000000 + " seconds");
    }
  }

  public static class School implements Runnable {
    private Logger logger = LoggerFactory.getLogger("query.school");

    @CommandLine.Option(names = "--term", required = true) private Integer term;

    @CommandLine.Option(names = "--outputFile") private String outputFile;

    public void run() {
      long start = System.nanoTime();
      Term term = Term.fromId(this.term);
      UtilsKt.writeToFileOrStdout(Query_schoolKt.querySchool(term), outputFile);
      long end = System.nanoTime();
      logger.info((end - start) / 1000000000 + " seconds");
    }
  }
}
