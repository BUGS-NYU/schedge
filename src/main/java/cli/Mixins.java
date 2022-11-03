package cli;

import static picocli.CommandLine.*;
import static utils.Try.*;

import java.util.*;
import picocli.CommandLine;
import utils.*;
import utils.Nyu;

public final class Mixins {
  @Spec private CommandLine.Model.CommandSpec spec;

  public static final class BatchSize {
    @Option(names = "--batch-size-catalog",
            description = "batch size for querying the catalog",
            defaultValue = "20")
    public int catalog;
    @Option(names = "--batch-size-sections",
            description = "batch size for querying sections",
            defaultValue = "10")
    public int sections;

    @Spec private CommandLine.Model.CommandSpec spec;
  }

  public static final class InputFile {
    @Option(names = "--input-file",
            description =
                "intput file to read from. If none provided, read from stdout")
    private String inputFile;

    public String getInput() { return Utils.readFromFileOrStdin(inputFile); }
  }

  public static final class OutputFile {
    @Option(names = "--pretty", defaultValue = "true",
            description = "Either true or false")
    private boolean pretty;

    @Option(names = "--output-file",
            description =
                "output file to write to. If none provided, write to stdout")
    private String outputFile;

    public void writeOutput(Object output) {
      var jsonData = tcIgnore(() -> JsonMapper.toJson(output, pretty));

      if (jsonData == null) {
        jsonData = output.toString();
      }

      Utils.writeToFileOrStdout(outputFile, jsonData);
    }
  }

  public static final class Term {
    @Spec private CommandLine.Model.CommandSpec spec;

    @Option(names = "--term",
            description = "Term is the shortcut for year and semester. "
                          + "To get term value, take year - 1900 then append \n"
                          + "ja = 2, sp = 4, su = 6 or fa = 8.\n Eg: "
                          + "Fall 2020 = (2020 - 1900) + 4 = 120 + 4 = 1204")
    private Integer termId;

    @Option(names = "--semester", description = "semester: ja, sp, su, or fa")
    private String semester;

    @Option(names = "--year", description = "year to scrape from")
    private Integer year;

    @Option(names = {"-h", "--help"}, usageHelp = true,
            description = "display a help message")
    boolean displayHelp;

    public Nyu.Term getTermAllowNull() {
      if (termId != null && (semester != null || year != null)) {
        throw new CommandLine.MutuallyExclusiveArgsException(
            spec.commandLine(),
            "--term and --semester/--year are mutually exclusive");
      } else if (termId == null && semester == null && year == null) {
        return null;
      } else if (termId == null) {
        if (semester == null || year == null) {
          throw new CommandLine.ParameterException(
              spec.commandLine(), "Must provide both --semester AND --year");
        }
        return new Nyu.Term(semester, year);
      } else {
        return Nyu.Term.fromId(termId);
      }
    }

    public Nyu.Term getTerm() {
      Nyu.Term t = getTermAllowNull();
      if (t == null) {
        throw new CommandLine.ParameterException(
            spec.commandLine(),
            "Must provide at least one: --term   OR   --semester AND --year");
      } else {
        return t;
      }
    }
  }
}
