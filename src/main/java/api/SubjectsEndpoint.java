package api;

import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import models.SubjectCode;
import org.jetbrains.annotations.NotNull;

class SubjectsEndpoint extends Endpoint {

  @NotNull
  @Override
  String getPath() {
    return "/subjects";
  }

  @NotNull
  @Override
  OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          // openApiOperation.operationId("Operation Id");
          openApiOperation.description(
              "This endpoint returns a list of subjects, optionally filtered by a school.");
          openApiOperation.summary("Subjects Endpoint");
        })
        .queryParam(
            "school", String.class,
            openApiParam -> {
              openApiParam.description(
                  "The school code you'd like to get subjects for (case insensitive). "
                  +
                  "If not supplied, then this endpoint will return all subjects.");
            })
        .json("400", ApiError.class,
              openApiParam -> {
                openApiParam.description(
                    "The school provided was not a valid school.");
              })
        .jsonArray("200", SubjectCode.class,
                   openApiParam -> { openApiParam.description("OK."); });
  }

  @NotNull
  @Override
  Handler getHandler() {
    return ctx -> {
      String school = ctx.queryParam("school");
      ctx.contentType("application/json");
      if (school == null) {
        ctx.json(SubjectCode.allSubjects());
      } else
        try {
          ctx.json(SubjectCode.allSubjects(school));
        } catch (IllegalArgumentException e) {
          ctx.json(new ApiError(e.getMessage()));
        }
    };
  }
}
