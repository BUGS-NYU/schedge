package api.v1.endpoints;

import static io.javalin.plugin.openapi.dsl.DocumentedContentKt.guessContentType;

import api.Endpoint;
import api.v1.ApiError;
import api.v1.RowsToCourses;
import api.v1.SelectCourses;
import api.v1.models.Course;
import api.v1.models.Section;
import database.GetConnection;
import database.courses.SelectRows;
import database.epochs.LatestCompleteEpoch;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import nyu.SubjectCode;
import nyu.Term;

public final class SectionEndpoint extends Endpoint {

  enum SemesterCode {
    su,
    sp,
    fa,
    ja;
  }

  public String getPath() { return "/:year/:semester/:registrationNumber"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          // openApiOperation.operationId("Operation Id");
          openApiOperation.description(
              "This endpoint returns a section for a specific year, semester, and registration number.");
          openApiOperation.summary("Section Endpoint");
        })
        .pathParam("year", Integer.class,
                   openApiParam -> {
                     openApiParam.description("Must be a valid year.");
                   })
        .pathParam("semester", SemesterCode.class,
                   openApiParam -> {
                     openApiParam.description("Must be a valid semester code.");
                   })
        .pathParam("registrationNumber", Integer.class,
                   openApiParam -> {
                     openApiParam.description(
                         "Must be a valid registrationNumber.");
                   })
        .json("400", ApiError.class,
              openApiParam -> {
                openApiParam.description(
                    "One of the values in the path parameter was not valid.");
              })
        .json("200", Course.class, openApiParam -> {
          openApiParam.description("OK.");

          ArrayList<Section> sections = new ArrayList<>();
        });
  }

  public Handler getHandler() {
    return ctx -> {

      int year;
      try {
        year = Integer.parseInt(ctx.pathParam("year"));
      } catch (NumberFormatException e) {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));
        return;
      }

      Term term;
      Integer registrationNumber;
      try {
        term = new Term(ctx.pathParam("semester"), year);
        registrationNumber =
            Integer.parseInt(ctx.pathParam("registrationNumber"));
      } catch (IllegalArgumentException e) {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));
        return;
      }

      String fullData = ctx.queryParam("full");

      Object output = GetConnection.withContextReturning(context -> {
        Integer epoch = LatestCompleteEpoch.getLatestEpoch(context, term);
        if (epoch == null) {
          return Collections.emptyList();
        }
        if (fullData != null && fullData.toLowerCase().equals("true"))
          return RowsToCourses
              .fullRowsToCourses(
                  SelectRows.selectFullRow(context, epoch, registrationNumber))
              .findAny()
              .map(c -> c.getSections().get(0))
              .orElse(null);
        return RowsToCourses
            .rowsToCourses(
                SelectRows.selectRow(context, epoch, registrationNumber))
            .findAny()
            .map(c -> c.getSections().get(0))
            .orElse(null);
      });
      if (output == null) {
        ctx.status(404);
        ctx.result("not found");
      } else {
        ctx.contentType("application/json");
        ctx.status(200);
        ctx.json(output);
      }
    };
  }
}
