package cli;

import static picocli.CommandLine.*;

/*
   @Help: Add annotations, comments to code
*/
@Command(name = "schedge")
public class Schedge implements Runnable {

  @Spec private Model.CommandSpec spec;
  @Option(names = {"-h", "--help"}, usageHelp = true,
          description = "display a help message")
  boolean displayHelp;

  public void run() {
    throw new ParameterException(
        spec.commandLine(),
        "Please provide command query, parse, scrape, or db");
  }
}
