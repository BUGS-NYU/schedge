package cli;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import models.SubjectCode;
import models.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import scraping.QueryCatalog;
import scraping.QuerySection;
import scraping.QuerySubject;
import services.Utils;

/*
    @Todo: Add annotation for parameter. Fix the method to parse
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

    @CommandLine.Option(names = "--school", required = true)
    private String school;

    @CommandLine.Option(names = "--subject", required = true)
    private String subject;

    @CommandLine.Option(names = "--outputFile") private String outputFile;

    public void run() {
      long start = System.nanoTime();
      Term term = Term.fromId(this.term);
      SubjectCode subjectCode = new SubjectCode(this.subject, this.school);
      try {
        Utils.writeToFileOrStdOut(QueryCatalog.queryCatalog(term, subjectCode),
                                  outputFile);
      } catch (ExecutionException | InterruptedException | IOException e) {
        e.printStackTrace();
      }
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
      try {
        Utils.writeToFileOrStdOut(
            QuerySection.querySection(term, registrationNumber), outputFile);
      } catch (ExecutionException | InterruptedException | IOException e) {
        e.printStackTrace();
      }
      long end = System.nanoTime();
      logger.info((end - start) / 1000000000 + " seconds");
    }
  }

  public static class Subject implements Runnable {
    private Logger logger = LoggerFactory.getLogger("query.subject");

    @CommandLine.Option(names = "--term", required = true) private Integer term;

    @CommandLine.Option(names = "--outputFile") private String outputFile;

    public void run() {
      long start = System.nanoTime();
      Term term = Term.fromId(this.term);
      try {
        Utils.writeToFileOrStdOut(QuerySubject.querySubject(term), outputFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
      long end = System.nanoTime();
      logger.info((end - start) / 1000000000 + " seconds");
    }
  }
}
