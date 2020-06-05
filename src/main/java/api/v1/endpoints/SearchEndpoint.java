package api.v1.endpoints;

import static api.v1.SelectCourses.selectCoursesBySectionId;
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
import org.jooq.impl.DSL;

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
            "limit", Integer.class,
            openApiParam -> {
              openApiParam.description(
                  "The maximum number of top-level sections to return. Capped at 50.");
            })
        .queryParam("school", String.class,
                    openApiParam -> {
                      openApiParam.description("The school to search within.");
                    })
        .queryParam(
            "subject", String.class,
            openApiParam -> {
              openApiParam.description(
                  "The subject to search within. Can work cross school.");
            })
        .queryParam(
            "titleWeight", Integer.class,
            openApiParam -> {
              openApiParam.description(
                  "The weight given to course titles in search. Default is 2.");
            })
        .queryParam(
            "descriptionWeight", Integer.class,
            openApiParam -> {
              openApiParam.description(
                  "The weight given to course descriptions in search. Default is 1.");
            })
        .queryParam(
            "notesWeight", Integer.class,
            openApiParam -> {
              openApiParam.description(
                  "The weight given to course notes in search. Default is 0.");
            })
        .queryParam(
            "prereqsWeight", Integer.class,
            openApiParam -> {
              openApiParam.description(
                  "The weight given to course prerequisites in search. Default is 0.");
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

      String school = ctx.queryParam("school"), subject =
                                                    ctx.queryParam("subject");

      int resultSize, titleWeight, descriptionWeight, notesWeight,
          prereqsWeight;
      try {
        resultSize = Optional.ofNullable(ctx.queryParam("limit"))
                         .map(Integer::parseInt)
                         .orElse(50);
        titleWeight = Optional.ofNullable(ctx.queryParam("titleWeight"))
                          .map(Integer::parseInt)
                          .orElse(2);
        descriptionWeight =
            Optional.ofNullable(ctx.queryParam("descriptionWeight"))
                .map(Integer::parseInt)
                .orElse(1);
        notesWeight = Optional.ofNullable(ctx.queryParam("notesWeight"))
                          .map(Integer::parseInt)
                          .orElse(0);
        prereqsWeight = Optional.ofNullable(ctx.queryParam("prereqsWeight"))
                            .map(Integer::parseInt)
                            .orElse(0);
      } catch (NumberFormatException e) {
        ctx.status(400);
        ctx.json(new ApiError("Limit needs to be a positive integer."));
        return;
      }

      GetConnection.withConnection(conn -> {
        Integer epoch = getLatestEpoch(conn, term);
        if (epoch == null) {
          ctx.status(200);
          ctx.json(new ArrayList<>());
          return;
        }

        String fullData = ctx.queryParam("full");
        if (fullData != null && fullData.toLowerCase().equals("true")) {
          ctx.json(RowsToCourses
                       .fullRowsToCourses(SearchRows.searchFullRows(
                           conn, epoch,
                           subject, school, resultSize, args, titleWeight,
                           descriptionWeight, notesWeight, prereqsWeight))
                       .collect(Collectors.toList()));
        } else {
          ctx.json(RowsToCourses
                       .rowsToCourses(SearchRows.searchRows(
                           conn, epoch, subject, school, resultSize, args,
                           titleWeight, descriptionWeight, notesWeight,
                           prereqsWeight))
                       .collect(Collectors.toList()));

          ctx.status(200);
        }
      });
    };
  }
}
