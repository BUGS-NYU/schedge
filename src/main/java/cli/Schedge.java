package cli;

import picocli.CommandLine;

/*
   @Todo: Add annotation for parameter. Fix the method to parse
   @Help: Add annotations, comments to code
*/
@CommandLine.Command(name = "schedge")
public class Schedge {
  public Schedge(String[] args) {
    if (args.length < 1) {
      throw new IllegalArgumentException(
          "Please provide command query, parse, scrape OR db");
    } else {
      new CommandLine(this)
          .addSubcommand("query", new CommandLine(new Query()))
          .addSubcommand("parse", new CommandLine(new Parse()))
          .addSubcommand("scrape", new CommandLine(new Scrape()))
          .addSubcommand("db", new CommandLine(new Database()))
          .execute(args);
    }
  }
}
