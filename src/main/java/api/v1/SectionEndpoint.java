package api.v1;

import api.*;
import database.GetConnection;
import database.courses.SelectRows;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import types.*;

public final class SectionEndpoint extends App.Endpoint {

  public String getPath() { return "/section/{term}/{registrationNumber}"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint returns a section for a specific year, semester, and registration number.");
          openApiOperation.summary("Section Endpoint");
        })
        .pathParam("term", String.class,
                   openApiParam -> {
                     openApiParam.description(
                         SchoolInfoEndpoint.TERM_PARAM_DESCRIPTION);
                   })
        .pathParam("registrationNumber", Integer.class,
                   openApiParam -> {
                     openApiParam.description(
                         "Must be a valid registrationNumber.");
                   })
        .json("200", Nyu.Section.class,
              openApiParam -> { openApiParam.description("OK."); })
        .json("400", App.ApiError.class, openApiParam -> {
          openApiParam.description(
              "One of the values in the path parameter was not valid.");
        });
  }

  public Object handleEndpoint(Context ctx) {
    String termString = ctx.pathParam("term");
    var term = SchoolInfoEndpoint.parseTerm(termString);

    int registrationNumber =
        Integer.parseInt(ctx.pathParam("registrationNumber"));

    String fullData = ctx.queryParam("full");

    Object output = GetConnection.withConnectionReturning(conn -> {
      if (fullData != null && fullData.toLowerCase().equals("true")) {
        return RowsToCourses
            .fullRowsToCourses(
                SelectRows.selectFullRow(conn, term, registrationNumber))
            .findAny()
            .map(c -> c.sections.get(0))
            .orElse(null);
      }

      return RowsToCourses
          .rowsToCourses(SelectRows.selectRow(conn, term, registrationNumber))
          .findAny()
          .map(c -> c.sections.get(0))
          .orElse(null);
    });

    if (output == null)
      throw new App.ApiError(404, "section not found");

    return output;
  }
}
