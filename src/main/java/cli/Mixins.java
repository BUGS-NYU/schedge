package cli;

import static picocli.CommandLine.*;

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
      var jsonData = Try.tcIgnore(() -> JsonMapper.toJson(output, pretty));

      if (jsonData == null) {
        jsonData = output.toString();
      }

      Utils.writeToFileOrStdout(outputFile, jsonData);
    }
  }

  public static final class Term {
    @Spec private CommandLine.Model.CommandSpec spec;

    @Option(names = "--term",
            description = "example: fa2020, where: fa=Fall, ja=January, sp=Spring, su=Summer", converter = TermConverter.class)
    public Nyu.Term term;
  }

  public static class TermConverter implements ITypeConverter<Nyu.Term> {
    public TermConverter() {}

    @Override
    public Nyu.Term convert(String value) {
      return Nyu.Term.fromString(value);
    }
  }
}
