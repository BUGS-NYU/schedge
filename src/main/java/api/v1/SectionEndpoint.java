package api.v1;

import static utils.TryCatch.*;

import api.*;
import database.GetConnection;
import database.courses.SelectRows;
import database.epochs.LatestCompleteEpoch;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.*;
import types.*;
import utils.*;

public final class SectionEndpoint extends Endpoint {

  enum SemesterCode {
    su,
    sp,
    fa,
    ja;
  }

  public String getPath() { return "/{term}/section/{registrationNumber}"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint returns a section for a specific year, semester, and registration number.");
          openApiOperation.summary("Section Endpoint");
        })
        .pathParam(
            "term", String.class,
            openApiParam -> {
              openApiParam.description(
                  "Must be a valid term code, either 'current', 'next', or something "
                  + "like sp2021 for Spring 2021. Use 'su' for Summer, 'sp' "
                  + "for Spring, 'fa' for Fall, and 'ja' for January/JTerm");
            })
        .pathParam("registrationNumber", Integer.class,
                   openApiParam -> {
                     openApiParam.description(
                         "Must be a valid registrationNumber.");
                   })
        .json("200", Section.class,
              openApiParam -> { openApiParam.description("OK."); })
        .json("400", ApiError.class, openApiParam -> {
          openApiParam.description(
              "One of the values in the path parameter was not valid.");
        });
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

      Integer registrationNumber =
          tc.log(() -> Integer.parseInt(ctx.pathParam("registrationNumber")));

      if (registrationNumber == null)
        return;

      String fullData = ctx.queryParam("full");

      Object output = GetConnection.withConnectionReturning(conn -> {
        Integer epoch = LatestCompleteEpoch.getLatestEpoch(conn, term);
        if (epoch == null) {
          return Collections.emptyList();
        }
        if (fullData != null && fullData.toLowerCase().equals("true"))
          return RowsToCourses
              .fullRowsToCourses(
                  SelectRows.selectFullRow(conn, epoch, registrationNumber))
              .findAny()
              .map(c -> c.sections.get(0))
              .orElse(null);

        return RowsToCourses
            .rowsToCourses(
                SelectRows.selectRow(conn, epoch, registrationNumber))
            .findAny()
            .map(c -> c.sections.get(0))
            .orElse(null);
      });

      if (output == null) {
        ctx.status(404);
        ctx.result("not found");
      } else {
        ctx.status(200);
        ctx.json(output);
      }
    };
  }
}
