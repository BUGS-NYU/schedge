package api.v1;

import static utils.TryCatch.*;

import api.*;
import database.GetConnection;
import database.epochs.LatestCompleteEpoch;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.*;
import types.*;
import utils.TryCatch;

public final class CurrentCoursesEndpoint extends Endpoint {

  enum SemesterCode {
    su,
    sp,
    fa,
    ja;
  }

  public String getPath() { return "/current/:subject"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint returns a list of courses for the current semester,"
              + " given a school and subject.");
          openApiOperation.summary("Current Courses Endpoint");
        })
        .pathParam(
            "subject", String.class,
            openApiParam -> {
              openApiParam.description(
                  "Must be a valid subject code. Take a look at the docs for the subjects endpoint for more information.");
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

      Term term = Term.getCurrentTerm();

      TryCatch tc = tcNew(e -> {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));
      });

      Subject subject = tc.log(() -> {
        String subjectString = ctx.pathParam("subject").toUpperCase();
        return Subject.fromCode(subjectString);
      });

      if (subject == null)
        return;

      String fullData = ctx.queryParam("full");

      ctx.status(200);
      Object output = GetConnection.withConnectionReturning(conn -> {
        Integer epoch = LatestCompleteEpoch.getLatestEpoch(conn, term);
        if (epoch == null) {
          return Collections.emptyList();
        }
        if (fullData != null && fullData.toLowerCase().equals("true"))
          return SelectCourses.selectFullCourses(
              conn, epoch, Collections.singletonList(subject));
        return SelectCourses.selectCourses(conn, epoch,
                                           Collections.singletonList(subject));
      });

      ctx.json(output);
    };
  }
}
