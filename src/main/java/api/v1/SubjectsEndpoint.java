package api.v1;

import api.Endpoint;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import types.SubjectCode;

public final class SubjectsEndpoint extends Endpoint {

  @Override
  public String getPath() {
    return "/subjects";
  }

  @Override
  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs.operation(openApiOperation -> {
      openApiOperation.description(
          "This endpoint returns an object whose keys are schools, and whose "
          +
          "values are objects with subject codes as keys and subject names as values.");
      openApiOperation.summary("Subjects Endpoint");
    });
  }

  @Override
  public Handler getHandler() {
    return ctx -> {
      ctx.contentType("application/json");
      ctx.json(SubjectCode.allSchools());
    };
  }
}
