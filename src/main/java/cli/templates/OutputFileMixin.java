package cli.templates;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nyu.SubjectCode;
import nyu.Term;
import picocli.CommandLine;
import utils.JsonMapper;
import utils.Utils;

public final class OutputFileMixin {
  @CommandLine.Option(names = "--pretty", defaultValue = "true")
  private boolean pretty;
  @CommandLine.
  Option(names = "--output-file",
         description =
             "output file to read from. If none provided, read from stdout")
  private String outputFile;

  public void writeOutput(Object output) {
    Utils.writeToFileOrStdout(outputFile, JsonMapper.toJson(output, pretty));
  }
}
