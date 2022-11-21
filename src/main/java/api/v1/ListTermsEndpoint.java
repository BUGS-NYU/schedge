package api.v1;

import static database.GetConnection.withConnectionReturning;
import static database.SelectTerms.*;
import static utils.Nyu.*;

import api.*;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import java.util.*;

public final class ListTermsEndpoint extends App.Endpoint {
  public String getPath() { return "/terms"; }

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
      path = "/api/terms", methods = HttpMethod.GET, summary = "Terms List",
      description =
          "This endpoint provides a list of terms that Schedge has data for",
      responses =
      {
        @OpenApiResponse(status = "200", description = "List of terms",
                         content = @OpenApiContent(from = String[].class))
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
    return withConnectionReturning(conn -> selectTerms(conn));
  }
}
