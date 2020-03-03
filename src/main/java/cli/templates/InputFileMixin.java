package cli.templates;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nyu.SubjectCode;
import nyu.Term;
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
