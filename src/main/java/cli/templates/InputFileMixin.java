package cli.templates;

import picocli.CommandLine;
import utils.Utils;

public final class InputFileMixin {
  @CommandLine.
  Option(names = "--input-file",
         description =
             "intput file to read from. If none provided, read from stdout")
  private String inputFile;

  public String getInput() { return Utils.readFromFileOrStdin(inputFile); }
}
