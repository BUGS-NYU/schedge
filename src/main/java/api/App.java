package api;

import api.v1.*;
import database.GetConnection;
import database.Migrations;
import io.javalin.Javalin;
import io.javalin.http.Context;
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

  public abstract static class Endpoint implements Handler {
    public abstract String getPath();
    public abstract OpenApiDocumentation
    configureDocs(OpenApiDocumentation docs);

    public abstract Object handleEndpoint(Context ctx);

    @Override
    public final void handle(Context ctx) {
      try {
        Object output = this.handleEndpoint(ctx);

        ctx.status(200);
        ctx.json(output);
      } catch (ApiError e) {
        ctx.status(e.status);
        ctx.json(e);
      } catch (Exception e) {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));
      }
    }

    public final void addTo(Javalin app) {
      var docs = this.configureDocs(OpenApiBuilder.document());
      app.get("/api" + getPath(), OpenApiBuilder.documented(docs, this));
    }
  }

  public static class ApiError extends RuntimeException {
    private final int status;
    private final String message;

    public ApiError(String message) { this(400, message); }
    public ApiError(int status, String message) {
      this.message = message;
      this.status = status;
    }

    public String getMessage() { return message; }
  }

  public static void run() {
    GetConnection.withConnection(conn -> Migrations.runMigrations(conn));

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

    new SchoolInfoEndpoint().addTo(app);

    new SearchEndpoint().addTo(app);
    new GenerateScheduleEndpoint().addTo(app);
    new CoursesEndpoint().addTo(app);
  }
}
