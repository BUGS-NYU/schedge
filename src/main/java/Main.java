import static picocli.CommandLine.*;
import static utils.TryCatch.*;
import static utils.Utils.*;

import api.App;
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
    new CommandLine(new Main()).execute(args);
  }

  @Override
  public void run() {
    throw new ParameterException(spec.commandLine(),
                                 "Please provide command scrape or db");
  }

  @Command(name = "serve", description = "runs the app\n")
  public void serve() {
    App.run();

    // while (scrape) {
    //   UpdateData.updateData(batchSize.catalog, batchSize.sections);

    //   tcFatal(() -> TimeUnit.DAYS.sleep(1), "Failed to sleep");
    // }
  }
}
