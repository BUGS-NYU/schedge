package api;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.info.Info;
import models.Course;
import models.SubjectCode;
import models.Term;
import org.jetbrains.annotations.NotNull;
import services.JsonMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import services.SelectCourses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import models.Semester;
import api.SubjectsEndpoint;
import java.io.InputStream;
import java.util.stream.Collectors;

public class App {
  public static void run() {

    // @TODO Use https://javalin.io/plugins/openapi#getting-started as guide for
    // documenting API, then use Redoc for docs generation
    Javalin app = Javalin
                      .create(config -> {
                        config.enableCorsForAllOrigins();
                        // config.defaultContentType = "application/json";
                        Info info =
                            new Info().version("1.0").description("Schedge");
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

     app.get("/", ctx -> {
       ctx.contentType("text/html");
       ctx.result(docs);
     });

    app.get("/schools", ctx -> {
      ctx.result(JsonMapper.toJson(SubjectCode.allSchools()));
    });

    new SubjectsEndpoint().addTo(app);

    app.get("/subjects/:school", ctx -> {
      try {
        List<SubjectCode> subjects =
            SubjectCode.allSubjects(ctx.pathParam("school"));
        ctx.result(JsonMapper.toJson(subjects));
      } catch (IllegalArgumentException e) {
        ctx.result("{"
                   + "\"error\":\"" + e.getMessage() + "\"}");
      }
    });

    app.get("/:year/:semester/:school/:subject", ctx -> {
      try {
        int year = Integer.parseInt(ctx.pathParam("year"));
        Semester sem = Semester.fromCode(ctx.pathParam("semester"));
        Term term = new Term(sem, year);
        SubjectCode subject =
            new SubjectCode(ctx.pathParam("subject"), ctx.pathParam("school"));

        ctx.result(JsonMapper.toJson(
            SelectCourses.selectCourses(logger, term, subject)));
      } catch (NumberFormatException e) {
        ctx.result("{"
                   + "\"error\":\"" + e.getMessage() + "\"}");
      } catch (IllegalArgumentException e) {
        ctx.result("{"
                   + "\"error\":\"" + e.getMessage() + "\"}");
      }
    });
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
