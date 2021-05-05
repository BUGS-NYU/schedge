package cli;

import picocli.CommandLine;

/*
   @Help: Add annotations, comments to code
*/
@CommandLine.Command(name = "schedge")
public class Schedge implements Runnable {

  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;
  @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
                      description = "display a help message")
  boolean displayHelp;

  public void run() {
    throw new CommandLine.ParameterException(
        spec.commandLine(),
        "Please provide command query, parse, scrape, or db");
  }
}
