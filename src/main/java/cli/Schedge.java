package cli;

import picocli.CommandLine;

/*
   @Todo: Add annotation for parameter. Fix the method to parse
   @Help: Add annotations, comments to code
*/
@CommandLine.Command(name = "schedge")
public class Schedge {

  @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

  public Schedge(String[] args) {
    if (args.length < 1) {
      throw new CommandLine.ParameterException(
          spec.commandLine(),
          "Please provide command query, parse, scrape OR db");
    } else {
      new CommandLine(this)
          .addSubcommand("query", new CommandLine(new Query()))
          .addSubcommand("parse", new CommandLine(new Parse()))
          .addSubcommand("scrape", new CommandLine(new Scrape()))
          .addSubcommand("db", new CommandLine(new Database()))
          .addSubcommand("search", new CommandLine(new Search()))
          .addSubcommand("shop", new CommandLine(new Shop()))
          .execute(args);
    }
  }
}
