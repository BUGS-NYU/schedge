package api.v1;

import static database.GetConnection.*;
import static database.SelectSubjects.*;
import static utils.Nyu.*;

import api.*;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import java.util.*;

public final class SchoolInfoEndpoint extends App.Endpoint {
  public String getPath() { return "/schools/{term}"; }

  public final class Info {
    public Term term;
    public ArrayList<School> schools;
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

  @OpenApi(
      path = "/api/schools/{term}", methods = HttpMethod.GET,
      summary = "School Info",
      description =
          "This endpoint provides general information on the subjects in a term",
      pathParams =
      {
        @OpenApiParam(name = "term",
                      description = SchoolInfoEndpoint.TERM_PARAM_DESCRIPTION,
                      example = "fa2022", required = true)
      },
      responses =
      {
        @OpenApiResponse(status = "200", description = "School information",
                         content = @OpenApiContent(from = Info.class))
        ,
            @OpenApiResponse(status = "400",
                             description = "One of the values in the path "
                                           + "parameter was "
                                           + "not valid.",
                             content =
                                 @OpenApiContent(from = App.ApiError.class))
      })
  public Object
  handleEndpoint(Context ctx) {
    Term term = parseTerm(ctx.pathParam("term"));

    Info info = new Info();
    info.term = term;
    info.schools = withConnectionReturning(conn -> {
      ArrayList<School> schools = selectSchoolsForTerm(conn, term);

      return schools;
    });

    return info;
  }
}
