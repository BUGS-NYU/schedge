package api.v1;

import static utils.TryCatch.*;

import api.*;
import database.GetConnection;
import database.courses.SearchRows;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.*;
import java.util.stream.Collectors;
import types.*;
import types.Term;
import utils.*;

public final class SearchEndpoint extends Endpoint {

  public String getPath() { return "/{term}/search"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint returns a list of courses for a year and semester, given search terms.");
          openApiOperation.summary("Search Endpoint");
        })
        .pathParam(
            "term", String.class,
            openApiParam -> {
              openApiParam.description(
                  "Must be a valid term code, either 'current', 'next', or something "
                  + "like sp2021 for Spring 2021. Use 'su' for Summer, 'sp' "
                  + "for Spring, 'fa' for Fall, and 'ja' for January/JTerm");
            })
        .queryParam(
            "full", Boolean.class, false,
            openApiParam -> {
              openApiParam.description(
                  "if present and equal to 'true', then you'll get full output");
            })
        .queryParam("query", String.class, false,
                    openApiParam -> {
                      openApiParam.description(
                          "A query string to pass to the search engine.");
                    })
        .queryParam(
            "limit", Integer.class, false,
            openApiParam -> {
              openApiParam.description(
                  "The maximum number of top-level sections to return. Capped at 50.");
            })
        .queryParam("school", String.class, false,
                    openApiParam -> {
                      openApiParam.description("The school to search within.");
                    })
        .queryParam(
            "subject", String.class, false,
            openApiParam -> {
              openApiParam.description(
                  "The subject to search within. Can work cross school.");
            })
        .queryParam(
            "titleWeight", Integer.class, false,
            openApiParam -> {
              openApiParam.description(
                  "The weight given to course titles in search. Default is 2.");
            })
        .queryParam(
            "descriptionWeight", Integer.class, false,
            openApiParam -> {
              openApiParam.description(
                  "The weight given to course descriptions in search. Default is 1.");
            })
        .queryParam(
            "notesWeight", Integer.class, false,
            openApiParam -> {
              openApiParam.description(
                  "The weight given to course notes in search. Default is 0.");
            })
        .queryParam(
            "prereqsWeight", Integer.class, false,
            openApiParam -> {
              openApiParam.description(
                  "The weight given to course prerequisites in search. Default is 0.");
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
      TryCatch tc = tcNew(e -> {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));
      });

      Term term = tc.log(() -> {
        String termString = ctx.pathParam("term");
        if (termString.contentEquals("current")) {
          return Term.getCurrentTerm();
        }

        if (termString.contentEquals("next")) {
          return Term.getCurrentTerm().nextTerm();
        }

        int year = Integer.parseInt(termString.substring(2));
        return new Term(termString.substring(0, 2), year);
      });

      if (term == null)
        return;

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
        String fullData = ctx.queryParam("full");
        if (fullData != null && fullData.toLowerCase().equals("true")) {
          ctx.json(RowsToCourses
                       .fullRowsToCourses(SearchRows.searchFullRows(
                           conn, term, subject, school, args, titleWeight,
                           descriptionWeight, notesWeight, prereqsWeight))
                       .limit(resultSize)
                       .collect(Collectors.toList()));
        } else {
          ctx.json(RowsToCourses
                       .rowsToCourses(SearchRows.searchRows(
                           conn, term, subject, school, args, titleWeight,
                           descriptionWeight, notesWeight, prereqsWeight))
                       .limit(resultSize)
                       .collect(Collectors.toList()));
        }

        ctx.status(200);
      });
    };
  }
}
