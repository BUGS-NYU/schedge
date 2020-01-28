package cli;

import picocli.CommandLine;

/*
   @Todo: Add annotation for parameter. Fix the method to parse
   @Help: Add annotations, comments to code
*/
@CommandLine.Command(name = "schedge")
public class schedge {
  public schedge(String[] args) {
    if (args.length < 1) {
      throw new IllegalArgumentException(
          "Please provide command query, parse, scrape OR db");
    } else {
      new CommandLine(this)
          .addSubcommand("query", new CommandLine(new query()))
          .addSubcommand("parse", new CommandLine(new parse()))
          .addSubcommand("scrape", new CommandLine(new scrape()))
          .addSubcommand("db", new CommandLine(new database()))
          .execute(args);
    }
  }
}
