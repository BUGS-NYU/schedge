import cli.Schedge;
import picocli.CommandLine;

public class Main {
  public static void main(String[] args) {
    new CommandLine(new Schedge())
        .addSubcommand("scrape", new CommandLine(new cli.Scrape()))
        .addSubcommand("db", new CommandLine(new cli.Database()))
        .execute(args);
  }
}
