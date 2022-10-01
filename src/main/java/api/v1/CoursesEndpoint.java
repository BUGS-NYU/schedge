package api.v1;

import api.*;
import database.GetConnection;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.*;
import types.*;

public final class CoursesEndpoint extends App.Endpoint {
  public String getPath() { return "/courses/{term}/{subject}"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint returns a list of courses for a specific semester and subject.");
          openApiOperation.summary("Courses Endpoint");
        })
        .pathParam("term", String.class,
                   openApiParam -> {
                     openApiParam.description(
                         SchoolInfoEndpoint.TERM_PARAM_DESCRIPTION);
                   })
        .pathParam(
            "subject", String.class,
            openApiParam -> {
              openApiParam.description(
                  "Must be a valid subject code. Take a look at the docs for the schools endpoint for more information.");
            })
        .queryParam(
            "full", Boolean.class, false,
            openApiParam -> {
              openApiParam.description(
                  "Whether to return campus, description, grading, nodes, "
                  + "and prerequisites.");
            })
        .jsonArray("200", Nyu.Course.class,
                   openApiParam -> { openApiParam.description("OK."); })
        .json("400", App.ApiError.class, openApiParam -> {
          openApiParam.description(
              "One of the values in the path parameter was not valid.");
        });
  }

  public Object handleEndpoint(Context ctx) {
    String termString = ctx.pathParam("term");
    var term = SchoolInfoEndpoint.parseTerm(termString);

    var subject = ctx.pathParam("subject").toUpperCase();
    String fullData = ctx.queryParam("full");

    Object output = GetConnection.withConnectionReturning(conn -> {
      List<String> subjects = Collections.singletonList(subject);

      if (fullData != null && fullData.toLowerCase().contentEquals("true")) {
        return SelectCourses.selectFullCourses(conn, term, subjects);
      }

      return SelectCourses.selectCourses(conn, term, subjects);
    });

    return output;
  }
}
