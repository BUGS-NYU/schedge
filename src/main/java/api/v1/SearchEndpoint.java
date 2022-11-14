package api.v1;

import api.*;
import database.GetConnection;
import database.SearchRows;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import java.util.*;
import java.util.stream.Collectors;
import utils.Nyu;

public final class SearchEndpoint extends App.Endpoint {

  public String getPath() { return "/search/{term}"; }

  @OpenApi(
      path = "/api/search/{term}", methods = HttpMethod.GET, summary = "Search",
      description =
          "This endpoint returns a list of courses for a year and semester, given search terms.",
      pathParams =
      {
        @OpenApiParam(name = "term",
                      description = SchoolInfoEndpoint.TERM_PARAM_DESCRIPTION,
                      example = "fa2022", required = true)
      },
      queryParams =
      {
        @OpenApiParam(name = "query",
                      description = "Query string created by the user",
                      example = "Linear algebra", required = true)
        ,
            @OpenApiParam(
                name = "limit", type = Integer.class,
                description =
                    "Maximum number of courses in the result. Defaults to 20, and is capped at 50.",
                example = "20")
      },
      responses =
      {
        @OpenApiResponse(status = "200", description = "Search results",
                         content = @OpenApiContent(from = Nyu.Course[].class))
        ,
            @OpenApiResponse(
                status = "400",
                description =
                    "Didn't provide a query, or the path param was an invalid term.",
                content = @OpenApiContent(from = App.ApiError.class))
      })
  public Object
  handleEndpoint(Context ctx) {
    String termString = ctx.pathParam("term");
    var term = Nyu.Term.fromString(termString);

    String args = ctx.queryParam("query");
    if (args == null) {
      throw new RuntimeException("Need to provide a query.");
    } else if (args.length() > 50) {
      throw new RuntimeException("Query can be at most 50 characters long.");
    }

    int resultSize;
    try {
      resultSize = Optional.ofNullable(ctx.queryParam("limit"))
                       .map(Integer::parseInt)
                       .orElse(20);
    } catch (NumberFormatException e) {
      throw new RuntimeException("Limit needs to be a positive integer.");
    }

    Object output = GetConnection.withConnectionReturning(conn -> {
      var rows = SearchRows.searchRows(conn, term, args, resultSize);
      return RowsToCourses.rowsToCourses(rows)
          .limit(resultSize)
          .collect(Collectors.toList());
    });

    return output;
  }
}
