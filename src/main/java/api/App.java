package api;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import io.swagger.v3.oas.models.info.Info;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
  public static void run() {

    // @TODO Use https://javalin.io/plugins/openapi#getting-started as guide for
    // documenting API, then use Redoc for docs generation
    Javalin app =
        Javalin
            .create(config -> {
              config.enableCorsForAllOrigins();
              String description =
                  "Schedge is an API to NYU's course catalog. "
                  + "Please note that <b>this API is currently under "
                  + "active development and is subject to change</b>."
                  + "<br/> <br/> If you'd like to contribute, "
                  +
                  "<a href=\"https://github.com/A1Liu/schedge\">check out the repository</a>.";
              Info info =
                  new Info().version("1.0").title("Schedge").description(
                      description);
              OpenApiOptions options =
                  new OpenApiOptions(info).path("/swagger.json");
              config.enableWebjars();
              config.registerPlugin(new OpenApiPlugin(options));
            })
            .start(80);
    Logger logger = LoggerFactory.getLogger("app");

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

    new SubjectsEndpoint().addTo(app);
    new SchoolsEndpoint().addTo(app);
    new CoursesEndpoint().addTo(app);
  }
}

abstract class Endpoint {

  @NotNull abstract String getPath();

  @NotNull
  abstract OpenApiDocumentation configureDocs(OpenApiDocumentation docs);

  @NotNull abstract Handler getHandler();

  public final void addTo(Javalin app) {
    app.get(getPath(),
            OpenApiBuilder.documented(configureDocs(OpenApiBuilder.document()),
                                      getHandler()));
  }
}

class ApiError {
  // private int status;
  private String message;

  ApiError(String message) {
    // this.status = status;
    this.message = message;
  }

  // public int getStatus() { return status; }

  public String getMessage() { return message; }
}
