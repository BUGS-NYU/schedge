package api.v1.endpoints;

import static api.v1.SelectCoursesBySectionId.selectCoursesBySectionId;
import static database.epochs.LatestCompleteEpoch.getLatestEpoch;
import static io.javalin.plugin.openapi.dsl.DocumentedContentKt.guessContentType;

import api.Endpoint;
import api.v1.ApiError;
import api.v1.RowsToCourses;
import api.v1.models.*;
import database.GetConnection;
import database.courses.SearchRows;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.*;
import java.util.stream.Collectors;
import nyu.SubjectCode;
import nyu.Term;

public final class SearchEndpoint extends Endpoint {

  enum SemesterCode {
    su,
    sp,
    fa,
    ja;
  }

  public String getPath() { return "/:year/:semester/search"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          // openApiOperation.operationId("Operation Id");
          openApiOperation.description(
              "This endpoint returns a list of courses for a year and semester, given search terms.");
          openApiOperation.summary("Search Endpoint");
        })
        .pathParam("year", Integer.class,
                   openApiParam -> {
                     openApiParam.description("Must be a valid year.");
                   })
        .pathParam("semester", SemesterCode.class,
                   openApiParam -> {
                     openApiParam.description("Must be a valid semester code.");
                   })
        .queryParam("query", String.class,
                    openApiParam -> {
                      openApiParam.description(
                          "A query string to pass to the search engine.");
                    })
        .queryParam(
            "limit", String.class,
            openApiParam -> {
              openApiParam.description(
                  "The maximum number of sections to return. Capped at 200.");
            })
        .json("400", ApiError.class,
              openApiParam -> {
                openApiParam.description(
                    "One of the values in the path parameter was not valid.");
              })
        .jsonArray("200", Course.class, openApiParam -> {
          openApiParam.description("OK.");

          ArrayList<Section> sections = new ArrayList<>();
        });
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

      String args = ctx.queryParam("query");
      if (args == null) {
        ctx.status(400);
        ctx.json(new ApiError("Need to provide a query."));
        return;
      } else if (args.length() > 50) {
        ctx.status(400);
        ctx.json(new ApiError("Query can be at most 50 characters long."));
      }

      int resultSize;
      try {
        resultSize = Optional.ofNullable(ctx.queryParam("limit"))
                         .map(Integer::parseInt)
                         .map(i -> i > 200 ? 200 : i)
                         .orElse(50);
      } catch (NumberFormatException e) {
        ctx.status(400);
        ctx.json(new ApiError("Limit needs to be a positive integer."));
        return;
      }

      GetConnection.withContext(context -> {
        Integer epoch = getLatestEpoch(context, term);
        if (epoch == null) {
          ctx.status(200);
          ctx.json(new ArrayList<>());
          return;
        }

        String fullData = ctx.queryParam("full");
        if (fullData != null && fullData.toLowerCase().equals("true")) {
          ctx.json(RowsToCourses
                       .fullRowsToCourses(SearchRows.searchFullRows(
                           context, epoch, resultSize, args, 4, 3, 2, 1))
                       .collect(Collectors.toList()));
        } else
          ctx.json(RowsToCourses
                       .rowsToCourses(SearchRows.searchRows(
                           context, epoch, resultSize, args, 4, 3, 2, 1))
                       .collect(Collectors.toList()));
        ctx.status(200);
      });
    };
  }
}
