package cli;

import static picocli.CommandLine.*;

import api.App;
import picocli.CommandLine;
import utils.Nyu;

@Command(name = "schedge")
public class Main implements Runnable {

  @Spec private Model.CommandSpec spec;

  @Option(
      names = {"-h", "--help"},
      usageHelp = true,
      description = "display a help message")
  boolean displayHelp;

  public static void main(String[] args) {
    var exitCode =
        new CommandLine(new Main())
            .addSubcommand("scrape", new CommandLine(new cli.Scrape()))
            .addSubcommand("db", new CommandLine(new cli.Database()))
            .registerConverter(Nyu.Term.class, new Mixins.TermConverter())
            .execute(args);

    if (exitCode != 0) System.exit(exitCode);
  }

  @Override
  public void run() {
    throw new ParameterException(
        spec.commandLine(), "Please provide command query, parse, scrape, or db");
  }

  @Command(name = "serve", description = "runs the app\n")
  public void serve() {
    App.run();
  }
}
