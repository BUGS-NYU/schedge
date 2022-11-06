package api;

import api.v1.*;
import database.*;
import io.javalin.Javalin;
import io.javalin.http.*;
import io.javalin.http.staticfiles.Location;
import io.javalin.openapi.OpenApiInfo;
import io.javalin.openapi.plugin.*;
import io.javalin.openapi.plugin.redoc.*;
import java.io.*;
import org.slf4j.*;

public class App {
  private static final Logger logger = LoggerFactory.getLogger("api.App");

  public abstract static class Endpoint implements Handler {
    public abstract String getPath();

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

    public final void addTo(Javalin app) { app.get("/api" + getPath(), this); }
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

    Javalin app = Javalin.create(config -> {
      config.plugins.enableCors(cors -> { // It's a public API
        cors.add(it -> { it.anyHost(); });
      });

      var description = "Schedge is an API to NYU's course catalog. "
                        + "Please note that <b>this API is currently under "
                        + "active development and is subject to change</b>."
                        + "<br/><br/>If you'd like to contribute, "
                        + "<a href=\"https://github.com/A1Liu/schedge\">"
                        + "check out the repository</a>.";

      var info = new OpenApiInfo();
      info.setVersion("2.0.0");
      info.setTitle("Schedge");
      info.setDescription(description);

      // Redoc uses webjars to do... something
      config.staticFiles.enableWebjars();

      // Set up OpenAPI + Redoc
      var htmlPath = "/api";
      var jsonPath = "/api/swagger.json";
      var openApiConfig = new OpenApiConfiguration();
      openApiConfig.setInfo(info);
      openApiConfig.setDocumentationPath(jsonPath);
      var reDocConfig = new ReDocConfiguration();
      reDocConfig.setDocumentationPath(jsonPath);
      reDocConfig.setWebJarPath("/api/webjars");
      reDocConfig.setUiPath(htmlPath);
      config.plugins.register(new OpenApiPlugin(openApiConfig));
      config.plugins.register(new ReDocPlugin(reDocConfig));

      // Add static files for the NextJS UI
      config.staticFiles.add(staticFiles -> {
        staticFiles.hostedPath = "/";
        staticFiles.directory = "/next/";
        staticFiles.location = Location.CLASSPATH;
        staticFiles.precompress = false;
      });
    });

    app.start(4358);

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
    new CampusEndpoint().addTo(app);

    new SearchEndpoint().addTo(app);
    new GenerateScheduleEndpoint().addTo(app);
    new CoursesEndpoint().addTo(app);
  }
}
