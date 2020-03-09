package api.v1.endpoints;

import api.Endpoint;
import api.v1.ApiError;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import nyu.SubjectCode;
import org.jetbrains.annotations.NotNull;

public final class SubjectsEndpoint extends Endpoint {

  @NotNull
  @Override
  public String getPath() {
    return "/subjects";
  }

  @NotNull
  @Override
  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint returns an object whose keys are schools, and whose "
            + "values are objects with subject codes as keys and subject names as values.");
          openApiOperation.summary("Subjects Endpoint");
        });
  }

  @NotNull
  @Override
  public Handler getHandler() {
    return ctx -> {
      ctx.contentType("application/json");
      ctx.json(SubjectCode.getAvailableSubjectInfo());
    };
  }
}
