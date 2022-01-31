package api.v1;

import api.Endpoint;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import types.SubjectCode;

public final class SchoolsEndpoint extends Endpoint {

  @Override
  public String getPath() {
    return "/schools";
  }

  @Override
  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs.operation(openApiOperation -> {
      openApiOperation.description(
          "This endpoint returns an object where keys are school codes, and values are their full names.");
      openApiOperation.summary("Schools Endpoint");
    });
  }

  @Override
  public Handler getHandler() {
    return ctx -> { ctx.json(SubjectCode.allSchools()); };
  }
}
