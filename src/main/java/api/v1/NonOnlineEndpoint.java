package api.v1;

import api.*;
import database.GetConnection;
import database.courses.*;
import database.epochs.LatestCompleteEpoch;
import database.models.*;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.*;
import java.util.stream.*;
import types.*;

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
        .queryParam("query", String.class,
                    openApiParam -> {
                      openApiParam.description(
                          "query string for text search, optional");
                    })
        .queryParam(
            "full", Boolean.class,
            openApiParam -> {
              openApiParam.description(
                  "if present and equal to 'true', then you'll get full output");
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
      String query = ctx.queryParam("query");

      ctx.status(200);
      Object output = GetConnection.withConnectionReturning(conn -> {
        Integer epoch = LatestCompleteEpoch.getLatestEpoch(conn, term);
        if (epoch == null) {
          return Collections.emptyList();
        }

        if (fullData != null && fullData.toLowerCase().equals("true")) {
          Stream<FullRow> rows;
          if (query != null) {
            rows =
                SearchRows
                    .searchFullRows(conn, epoch, null, null, query, 2, 1, 0, 0)
                    .filter(
                        row -> !row.instructionMode.contentEquals("Online"));
          } else {
            rows = SelectRows.selectFullRows(
                conn, "courses.epoch = ? AND sections.instruction_mode <> ?",
                epoch, "Online");
          }
          return RowsToCourses.fullRowsToCourses(rows).collect(
              Collectors.toList());
        }

        Stream<Row> rows;
        if (query != null) {
          rows =
              SearchRows.searchRows(conn, epoch, null, null, query, 2, 1, 0, 0)
                  .filter(row -> !row.instructionMode.contentEquals("Online"));
        } else {
          rows = SelectRows.selectRows(
              conn, "courses.epoch = ? AND sections.instruction_mode <> ?",
              epoch, "Online");
        }
        return RowsToCourses.rowsToCourses(rows).collect(Collectors.toList());
      });
      ctx.json(output);
    };
  }
}
