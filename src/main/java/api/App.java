package api;

import api.v1.*;
import database.*;
import io.javalin.Javalin;
import io.javalin.http.*;
import io.javalin.http.staticfiles.Location;
import io.javalin.micrometer.MicrometerPlugin;
import io.javalin.openapi.OpenApiInfo;
import io.javalin.openapi.plugin.*;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.slf4j.*;
import utils.Utils;

public class App {
  private static final Logger logger = LoggerFactory.getLogger("api.App");

  public abstract static class Endpoint implements Handler {
    public abstract String getPath();

    public abstract Object handleEndpoint(Context ctx);

    @Override
    public final void handle(Context ctx) {
      try {
        var output = this.handleEndpoint(ctx);
        if (output instanceof ApiError e) {
          ctx.status(e.status);
          ctx.json(e);

          return;
        }

        ctx.status(200);
        ctx.json(output);
      } catch (Exception e) {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));
      }
    }

    public final void addTo(Javalin app) {
      app.get("/api" + getPath(), this);
    }
  }

  public static class ApiError {
    private final int status;
    private final String message;

    public ApiError(String message) {
      this(400, message);
    }
    public ApiError(int status, String message) {
      this.message = message;
      this.status = status;
    }

    public int getStatus() {
      return status;
    }
    public String getMessage() {
      return message;
    }
  }

  public static final int PORT = Utils.getEnvDefault("SCHEDGE_PORT", 4358);
  public static final String DESCR_TEMPLATE = """
    Schedge is an API to NYU's course catalog. Please note that
    <b>this API is a beta build currently under active development
    and is subject to change</b>. <br /><br />
    If you'd like to contribute,
    <a href="https://github.com/A1Liu/schedge">check out the repository</a>.
    <br /> <br />
    <b><big>Build version:
    <a href="https://github.com/A1Liu/schedge/tree/%1$s">%1$s</a>.
    </big></b>
  """;

  public static Javalin makeApp() {
    // Ensure that the connection gets instantiated during startup
    GetConnection.forceInit();

    var registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    // add a tag to all reported values to simplify filtering in large
    // installations:
    registry.config().commonTags("schedge", "Schedge");

    Javalin app = Javalin.create(config -> {
      config.plugins.enableCors(cors -> { // It's a public API
        cors.add(it -> { it.anyHost(); });
      });

      var description = String.format(DESCR_TEMPLATE, Health.BUILD_VERSION);

      var info = new OpenApiInfo();
      info.setVersion("2.0.0 beta");
      info.setTitle("Schedge");
      info.setDescription(description);

      var jsonPath = "/api/swagger.json";
      var openApiConfig = new OpenApiConfiguration();
      openApiConfig.setInfo(info);
      openApiConfig.setDocumentationPath(jsonPath);
      config.plugins.register(new OpenApiPlugin(openApiConfig));

      config.plugins.register(MicrometerPlugin.Companion.create(metrics -> {
        metrics.registry = registry;
        metrics.tags = Tags.empty();
        metrics.tagExceptionName = true;
      }));

      config.staticFiles.add(staticFiles -> { // NextJS UI
        staticFiles.hostedPath = "/";
        staticFiles.directory = "/next";
        staticFiles.location = Location.CLASSPATH;
        staticFiles.precompress = true;
      });

      config.staticFiles.add(staticFiles -> { // ReDoc API Docs
        staticFiles.hostedPath = "/";
        staticFiles.directory = "/api";
        staticFiles.location = Location.CLASSPATH;
        staticFiles.precompress = false;
      });
    });

    app.exception(Exception.class, (e, ctx) -> {
      String stackTrace = Utils.stackTrace(e);

      String message = String.format(
          "Uncaught Exception: %s\nQuery Parameters are: %s\nPath: %s\n",
          stackTrace, ctx.queryParamMap(), ctx.path());
      logger.warn(message);
    });

    new SchoolInfoEndpoint().addTo(app);
    new CampusEndpoint().addTo(app);
    new ListTermsEndpoint().addTo(app);
    new Health().addTo(app);

    new SearchEndpoint().addTo(app);
    new GenerateScheduleEndpoint().addTo(app);
    new CoursesEndpoint().addTo(app);

    ScrapingEndpoint.add(app);

    app.get("/api/prometheus", ctx -> {
      ctx.contentType(TextFormat.CONTENT_TYPE_004).result(registry.scrape());
    });

    new ClassLoaderMetrics().bindTo(registry);
    new JvmMemoryMetrics().bindTo(registry);
    new JvmGcMetrics().bindTo(registry);
    new JvmThreadMetrics().bindTo(registry);
    new UptimeMetrics().bindTo(registry);
    new ProcessorMetrics().bindTo(registry);

    return app;
  }

  public static void run() {
    var app = makeApp();
    app.start(PORT);
  }
}
