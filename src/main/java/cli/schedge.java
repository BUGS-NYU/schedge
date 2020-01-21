package cli;

import picocli.CommandLine;

/*
    @Todo: Add annotation for parameter. Fix the method to parse
    @Help: Add annotations, comments to code
 */
@CommandLine.Command(name = "schedge")
public class schedge {
  public schedge(String[] args) {
    new CommandLine(this)
        .addSubcommand("query",
                       new CommandLine(new query())
                           .addSubcommand("school", new query.School())
                           .addSubcommand("catalog", new query.Catalog())
                           .addSubcommand("section", new query.Section()))
        .addSubcommand("parse",
                       new CommandLine(new parse())
                           .addSubcommand("school", new parse.School())
                           .addSubcommand("catalog", new parse.Catalog())
                           .addSubcommand("section", new parse.Section()))
        .addSubcommand("scrape",
                       new CommandLine(new scrape())
                           .addSubcommand("school", new scrape.School())
                           .addSubcommand("section", new scrape.Section())
                           .addSubcommand("sections", new scrape.Sections())
                           .addSubcommand("catalog", new scrape.Catalog()))
        .execute(args);
  }
}
