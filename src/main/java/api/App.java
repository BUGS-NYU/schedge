package api;

import api.v1.*;
import io.javalin.Javalin;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.swagger.v3.oas.models.info.Info;
import java.io.*;
import java.util.stream.Collectors;
import org.slf4j.*;

public class App {

  private static final Logger logger = LoggerFactory.getLogger("api.App");

  public static void run() {
    Javalin app =
        Javalin
            .create(config -> {
              config.enableCorsForAllOrigins();
              String description =
                  "Schedge is an API to NYU's course catalog. "
                  + "Please note that <b>this API is currently under "
                  + "active development and is subject to change</b>."
                  + "<br/><br/>If you'd like to contribute, "
                  + "<a href=\"https://github.com/A1Liu/schedge\">"
                  + "check out the repository</a>.";
              Info info =
                  new Info().version("0.1").title("Schedge").description(
                      description);
              OpenApiOptions options =
                  new OpenApiOptions(info).path("/swagger.json");
              config.enableWebjars();
              config.registerPlugin(new OpenApiPlugin(options));
            })
            .start(4358);

    String docs = new BufferedReader(
                      new InputStreamReader(
                          Javalin.class.getResourceAsStream("/index.html")))
                      .lines()
                      .collect(Collectors.joining("\n"));

    app.get("/", OpenApiBuilder.documented(OpenApiBuilder.document().ignore(),
                                           ctx -> {
                                             ctx.contentType("text/html");
                                             ctx.result(docs);
                                           }));

    app.exception(Exception.class, (e, ctx) -> {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      String stackTrace = sw.toString();

      String message = String.format(
          "Uncaught Exception: %s\nQuery Parameters are: %s\nPath: %s\n",
          stackTrace, ctx.queryParamMap().toString(), ctx.path());
      logger.warn(message);
    });

    new SubjectsEndpoint().addTo(app);
    new SchoolsEndpoint().addTo(app);
    new CurrentCoursesEndpoint().addTo(app);
    new CoursesEndpoint().addTo(app);
    new GenerateScheduleEndpoint().addTo(app);
    new SearchEndpoint().addTo(app);
    new NonOnlineEndpoint().addTo(app);
    new SectionEndpoint().addTo(app);
  }
}
