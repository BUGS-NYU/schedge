package api;

import static database.courses.SelectCoursesBySectionId.*;
import static io.javalin.plugin.openapi.dsl.DocumentedContentKt.guessContentType;

import api.models.Course;
import api.models.Section;
import database.GetConnection;
import database.epochs.LatestCompleteEpoch;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import io.swagger.v3.oas.models.examples.Example;
import java.util.ArrayList;
import java.util.List;
import nyu.SubjectCode;
import nyu.Term;
import search.SearchCourses;

class SearchEndpoint extends Endpoint {

  enum SemesterCode {
    su,
    sp,
    fa,
    ja;
  }

  String getPath() { return "/:year/:semester/search"; }

  OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
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
                  "The maximum number of results to return. Capped at 200.");
            })
        .json("400", ApiError.class,
              openApiParam -> {
                openApiParam.description(
                    "One of the values in the path parameter was not valid.");
              })
        .jsonArray("200", Course.class, openApiParam -> {
          openApiParam.description("OK.");

          ArrayList<Section> sections = new ArrayList<>();

          openApiParam.getContent()
              .get(guessContentType(Course.class))
              .addExamples("course",
                           new Example().value(new Course(
                               "Intro to Computer SCI", "101",
                               new SubjectCode("CSCI", "UA"), sections)));
        });
  }

  Handler getHandler() {
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
        ctx.json(new ApiError("Need to provide a query messager."));
        return;
      }

      String resultSizeString = ctx.queryParam("limit");

      Integer resultSizeValue;
      try {
        resultSizeValue = resultSizeString == null
                              ? null
                              : Integer.parseInt(resultSizeString);
        if (resultSizeValue != null && resultSizeValue > 200)
          resultSizeValue = 200;

      } catch (NumberFormatException e) {
        ctx.status(400);
        ctx.json(new ApiError("Limit needs to be an integer."));
        return;
      }

      Integer resultSize = resultSizeValue;
      ctx.status(200);
      Object output = GetConnection.withContextReturning(context -> {
        int epoch = LatestCompleteEpoch.getLatestEpoch(context, term);
        List<Integer> result =
            SearchCourses.searchCourses(epoch, args, resultSize);

        return selectCoursesBySectionId(context, epoch, result);
      });
      ctx.json(output);
    };
  }
}
