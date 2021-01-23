package api.v1.endpoints;

import static database.epochs.LatestCompleteEpoch.getLatestEpoch;

import api.Endpoint;
import api.v1.ApiError;
import api.v1.RowsToCourses;
import api.v1.models.Course;
import api.v1.models.Section;
import database.GetConnection;
import database.courses.SearchRows;
import database.instructors.SelectInstructors;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import nyu.Term;

public final class RatingEndPoint extends Endpoint {

  public String getPath() { return "/rating"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint returns a list of courses for a year and semester, given search terms.");
          openApiOperation.summary("Rating Search Endpoint");
        })
        .queryParam("query", String.class,
                    openApiParam -> {
                      openApiParam.description(
                          "A query string to pass to the search engine.");
                    })
        .queryParam(
            "limit", Integer.class,
            openApiParam -> {
              openApiParam.description(
                  "The maximum number of top-level sections to return. Capped at 50.");
            })
        .queryParam(
            "instructorWeight", Integer.class,
            openApiParam -> {
              openApiParam.description(
                  "The weight given to course titles in search. Default is 2.");
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

      String args = ctx.queryParam("query");
      if (args == null) {
        ctx.status(400);
        ctx.json(new ApiError("Need to provide a query."));
        return;
      } else if (args.length() > 50) {
        ctx.status(400);
        ctx.json(new ApiError("Query can be at most 50 characters long."));
      }

      int resultSize, instructorWeight;

      try {
        resultSize = Optional.ofNullable(ctx.queryParam("limit"))
                         .map(Integer::parseInt)
                         .orElse(50);
        instructorWeight =
            Optional.ofNullable(ctx.queryParam("instructorWeight"))
                .map(Integer::parseInt)
                .orElse(2);
      } catch (NumberFormatException e) {
        ctx.status(400);
        ctx.json(new ApiError("Limit needs to be a positive integer."));
        return;
      }

      GetConnection.withConnection(conn -> {
        ctx.json(SelectInstructors.selectComment(conn, args, instructorWeight)
                     .limit(resultSize)
                     .collect(Collectors.toList()));
        ctx.status(200);
      });
    };
  }
}
