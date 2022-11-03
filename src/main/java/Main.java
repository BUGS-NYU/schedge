import static picocli.CommandLine.*;

import actions.*;
import api.App;
import cli.Mixins;
import java.util.concurrent.TimeUnit;
import picocli.CommandLine;

/*
   @Help: Add annotations, comments to code
*/
@Command(name = "schedge")
public class Main implements Runnable {

  @Spec private Model.CommandSpec spec;
  @Option(names = {"-h", "--help"}, usageHelp = true,
          description = "display a help message")
  boolean displayHelp;

  public static void main(String[] args) {
    new CommandLine(new Main())
        .addSubcommand("scrape", new CommandLine(new cli.Scrape()))
        .addSubcommand("db", new CommandLine(new cli.Database()))
        .execute(args);
  }

  @Override
  public void run() {
    throw new ParameterException(
        spec.commandLine(),
        "Please provide command query, parse, scrape, or db");
  }

  @Command(name = "serve", description = "runs the app\n")
  public void
  serve(@Mixin Mixins.BatchSize batchSize,
        @Option(names = "--scrape",
                description = "whether or not to scrape while serving")
        boolean scrape) {
    App.run();

    // while (scrape) {
    //   UpdateData.updateData(batchSize.catalog, batchSize.sections);

    //   tcFatal(() -> TimeUnit.DAYS.sleep(1), "Failed to sleep");
    // }
  }
}
