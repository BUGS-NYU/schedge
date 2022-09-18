import static picocli.CommandLine.*;
import static utils.TryCatch.*;
import static utils.Utils.*;

import actions.*;
import api.App;
import cli.Mixins;
import cli.Schedge;
import java.util.concurrent.TimeUnit;
import picocli.CommandLine;

@Command(name = "serve", description = "runs the app\n")
public class Main implements Runnable {
  @Mixin Mixins.BatchSize batchSize;

  @Option(names = "--scrape",
          description = "whether or not to scrape while serving")
  boolean scrape;

  public static void main(String[] args) {
    new CommandLine(new Schedge())
        .addSubcommand("scrape", new CommandLine(new cli.Scrape()))
        .addSubcommand("db", new CommandLine(new cli.Database()))
        .addSubcommand("serve", new CommandLine(new Main()))
        .execute(args);
  }

  @Override
  public void run() {
    App.run();

    while (scrape) {
      UpdateData.updateData(batchSize.catalog, batchSize.sections);

      tcFatal(() -> TimeUnit.DAYS.sleep(1), "Failed to sleep");
    }
  }
}
