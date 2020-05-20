package cli;

import picocli.CommandLine;

/*
   @Todo: Add annotation for parameter. Fix the method to parse
   @Help: Add annotations, comments to code
*/
@CommandLine.Command(name = "schedge")
public class Schedge implements Runnable {

  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;
  
  public void run() {
    throw new CommandLine.ParameterException(
        spec.commandLine(),
        "Please provide command query, parse, scrape OR db");
  }
}
