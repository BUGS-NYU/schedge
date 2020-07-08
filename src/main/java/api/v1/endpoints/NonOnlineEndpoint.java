package api.v1.endpoints;

import api.Endpoint;
import api.v1.ApiError;
import api.v1.RowsToCourses;
import api.v1.models.Course;
import api.v1.models.Section;
import database.GetConnection;
import database.courses.SelectRows;
import database.epochs.LatestCompleteEpoch;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import nyu.SubjectCode;
import nyu.Term;

public final class NonOnlineEndpoint extends Endpoint {

  enum SemesterCode {
    su,
    sp,
    fa,
    ja;
  }

  public String getPath() { return "/:year/:semester/notOnline"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          // openApiOperation.operationId("Operation Id");
          openApiOperation.description(
              "This endpoint returns a list of courses for a specific year, semester that aren't online.");
          openApiOperation.summary("Non-Online Endpoint");
        })
        .pathParam("year", Integer.class,
                   openApiParam -> {
                     openApiParam.description("Must be a valid year.");
                   })
        .pathParam("semester", SemesterCode.class,
                   openApiParam -> {
                     openApiParam.description("Must be a valid semester code.");
                   })
        .json("400", ApiError.class,
              openApiParam -> {
                openApiParam.description(
                    "One of the values in the path parameter was not valid.");
              })
        .jsonArray("200", Course.class,
                   openApiParam -> { openApiParam.description("OK."); });
  }

  public Handler getHandler() {
    return ctx -> {
      ctx.contentType("application/json");

      int year;
      try {
        year = Integer.parseInt(ctx.pathParam("year"));
      } catch (NumberFormatException e) {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));
        return;
      }

      Term term;
      try {
        term = new Term(ctx.pathParam("semester"), year);
      } catch (IllegalArgumentException e) {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));
        return;
      }

      String fullData = ctx.queryParam("full");

      ctx.status(200);
      Object output = GetConnection.withConnectionReturning(conn -> {
        Integer epoch = LatestCompleteEpoch.getLatestEpoch(conn, term);
        if (epoch == null) {
          return Collections.emptyList();
        }
        if (fullData != null && fullData.toLowerCase().equals("true")) {
          return RowsToCourses
              .fullRowsToCourses(SelectRows.selectFullRows(
                  conn, "courses.epoch = ? AND sections.instruction_mode <> ?",
                  epoch, "Online"))
              .collect(Collectors.toList());
        }

        return RowsToCourses
            .rowsToCourses(SelectRows.selectRows(
                conn, "courses.epoch = ? AND sections.instruction_mode <> ?",
                epoch, "Online"))
            .collect(Collectors.toList());
      });
      ctx.json(output);
    };
  }
}
