package api;

import api.v1.*;
import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.swagger.v3.oas.models.info.Info;
import java.io.*;
import org.slf4j.*;

public class App {

  private static final Logger logger = LoggerFactory.getLogger("api.App");

  public abstract static class Endpoint {
    public abstract String getPath();
    public abstract OpenApiDocumentation
    configureDocs(OpenApiDocumentation docs);
    public abstract Handler getHandler();
    public final void addTo(Javalin app) {
      app.get("/api" + getPath(),
              OpenApiBuilder.documented(
                  configureDocs(OpenApiBuilder.document()), getHandler()));
    }
  }

  public static class ApiError {
    private String message;
    public ApiError(String message) { this.message = message; }
    public String getMessage() { return message; }
  }

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

              config.enableWebjars();

              OpenApiOptions options = new OpenApiOptions(info)
                                           .path("/api/swagger.json")
                                           .reDoc(new ReDocOptions("/api"));
              config.registerPlugin(new OpenApiPlugin(options));

              config.addStaticFiles(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/next/";
                staticFiles.location = Location.CLASSPATH;
                staticFiles.precompress = true;
              });
            })
            .start(4358);

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

    new InfoEndpoint().addTo(app);

    new SearchEndpoint().addTo(app);
    new GenerateScheduleEndpoint().addTo(app);
    new CoursesEndpoint().addTo(app);
    new SectionEndpoint().addTo(app);
  }
}
