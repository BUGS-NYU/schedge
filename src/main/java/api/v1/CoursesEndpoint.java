package api.v1;

import static utils.Nyu.*;

import api.*;
import api.App.ApiError;
import database.GetConnection;
import database.SelectSubjects;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import java.util.*;
import utils.ArrayJS;

public final class CoursesEndpoint extends App.Endpoint {
  public String getPath() {
    return "/courses/{term}/{subject}";
  }

  @OpenApi(
      path = "/api/courses/{term}/{subject}",
      methods = HttpMethod.GET,
      summary = "Courses",
      description = "Lists all courses for a specific semester and subject.",
      pathParams = {
        @OpenApiParam(
            name = "subject",
            description =
                "Must be a valid subject code. Take a look at the docs for "
                    + "the schools endpoint for more information.",
            example = "MATH-UA",
            required = true),
        @OpenApiParam(
            name = "term",
            description = SchoolInfoEndpoint.TERM_PARAM_DESCRIPTION,
            example = "fa2022",
            required = true)
      },
      responses = {
        @OpenApiResponse(
            status = "200",
            description = "Status of the executed command",
            content = @OpenApiContent(from = Course[].class)),
        @OpenApiResponse(
            status = "400",
            description = "One of the values in the path " + "parameter was " + "not valid.",
            content = @OpenApiContent(from = App.ApiError.class))
      })
  public Object handleEndpoint(Context ctx) {
    String termString = ctx.pathParam("term");
    var term = Term.fromString(termString);

    var subject = ctx.pathParam("subject").toUpperCase();

    return GetConnection.withConnectionReturning(
        conn -> {
          var inputSubjects = Collections.singletonList(subject);
          var courses = SelectCourses.selectCourses(conn, term, inputSubjects);

          if (courses.size() == 0) {
            var termSubjects = SelectSubjects.selectSubjectsForTerm(conn, term);
            var matchingSubject = ArrayJS.find(termSubjects, sub -> sub.code().equals(subject));

            if (matchingSubject.isEmpty()) {
              return new ApiError(
                  "the subject \"" + subject + "\" is invalid for the term " + term);
            }
          }

          return courses;
        });
  }
}
