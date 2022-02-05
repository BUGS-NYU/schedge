package api.v1;

import api.*;
import database.GetConnection;
import database.epochs.LatestCompleteEpoch;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.*;
import types.*;

public final class CoursesEndpoint extends Endpoint {

  enum SemesterCode {
    su,
    sp,
    fa,
    ja;
  }

  public String getPath() { return "/{year}/{semester}/{subject}"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint returns a list of courses for a specific year, semester, school, and subject.");
          openApiOperation.summary("Courses Endpoint");
        })
        .pathParam("year", Integer.class,
                   openApiParam -> {
                     openApiParam.description("Must be a valid year.");
                   })
        .pathParam("semester", SemesterCode.class,
                   openApiParam -> {
                     openApiParam.description("Must be a valid semester code.");
                   })
        .pathParam(
            "subject", String.class,
            openApiParam -> {
              openApiParam.description(
                  "Must be a valid subject code. Take a look at the docs for the schools endpoint for more information.");
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
      int year;
      try {
        year = Integer.parseInt(ctx.pathParam("year"));
      } catch (NumberFormatException e) {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));

        return;
      }

      Term term;
      Subject subject;
      try {
        term = new Term(ctx.pathParam("semester"), year);

        String subjectString = ctx.pathParam("subject").toUpperCase();
        subject = Subject.fromCode(subjectString);

      } catch (IllegalArgumentException e) {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));

        return;
      }

      String fullData = ctx.queryParam("full");

      Object output = GetConnection.withConnectionReturning(conn -> {
        Integer epoch = LatestCompleteEpoch.getLatestEpoch(conn, term);
        if (epoch == null)
          return Collections.emptyList();

        if (fullData != null && fullData.toLowerCase().equals("true"))
          return SelectCourses.selectFullCourses(
              conn, epoch, Collections.singletonList(subject));

        return SelectCourses.selectCourses(conn, epoch,
                                           Collections.singletonList(subject));
      });

      ctx.status(200);
      ctx.json(output);
    };
  }
}
