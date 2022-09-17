package api.v1;

import api.*;
import database.*;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.*;
import types.*;

public final class SubjectsEndpoint extends App.Endpoint {
  public String getPath() { return "/subjects/{term}"; }

  public final class Info {
    public Term currentTerm;
    public Map<String, Subject.School> schools;
    public Map<String, String> subjectNames;
  }

  public static final String TERM_PARAM_DESCRIPTION =
      "Must be a valid term code, either 'current', 'next', or something "
      + "like sp2021 for Spring 2021. Use 'su' for Summer, 'sp' "
      + "for Spring, 'fa' for Fall, and 'ja' for January/JTerm";

  public static Term parseTerm(String termString) {
    if (termString.contentEquals("current")) {
      return Term.getCurrentTerm();
    }

    if (termString.contentEquals("next")) {
      return Term.getCurrentTerm().nextTerm();
    }

    int year = Integer.parseInt(termString.substring(2));
    return new Term(termString.substring(0, 2), year);
  }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint provides general information on the subjects in a term");
          openApiOperation.summary("Subjects Endpoint");
        })
        .pathParam("term", String.class,
                   openApiParam -> {
                     openApiParam.description(TERM_PARAM_DESCRIPTION);
                   })
        .json("200", Info.class,
              openApiParam -> { openApiParam.description("OK."); });
  }

  public Handler getHandler() {
    return ctx -> {
      Term term = parseTerm(ctx.pathParam("term"));

      ArrayList<SelectSubjects.Subject> subjects =
          GetConnection.withConnectionReturning(conn -> {
            return SelectSubjects.selectSubjectsForTerm(conn, term);
          });

      ctx.status(200);
      ctx.json(subjects);
    };
  }
}
