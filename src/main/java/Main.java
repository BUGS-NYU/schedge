import cli.Schedge;
import picocli.CommandLine;

public class Main {
  public static void main(String[] args) {
    new CommandLine(new Schedge())
        .addSubcommand("query", new CommandLine(new cli.Query()))
        .addSubcommand("parse", new CommandLine(new cli.Parse()))
        .addSubcommand("scrape", new CommandLine(new cli.Scrape()))
        .addSubcommand("db", new CommandLine(new cli.Database()))
        .execute(args);
  }
}
