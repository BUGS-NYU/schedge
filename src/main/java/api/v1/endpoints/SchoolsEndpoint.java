package api.v1.endpoints;

import api.Endpoint;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.DocumentedResponse;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import nyu.SubjectCode;
import org.jetbrains.annotations.NotNull;

public final class SchoolsEndpoint extends Endpoint {

  @NotNull
  @Override
  public String getPath() {
    return "/schools";
  }

  @NotNull
  @Override
  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          // openApiOperation.operationId("Operation Id");
          openApiOperation.description(
              "This endpoint returns an object where keys are school codes, and values are their full names.");
          openApiOperation.summary("Schools Endpoint");
        })
        .queryParam("query", String.class, openApiParam -> {
          openApiParam.description(
              "A query string to pass to the school endpoint.");
        });
  }

  @NotNull
  @Override
  public Handler getHandler() {
    return ctx -> {
      String args = ctx.queryParam("query");
      System.out.println(args);
      ctx.json(SubjectCode.allSchools(args));
    };
  }
}
