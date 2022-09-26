package api.v1;

import api.*;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;

public final class Health extends App.Endpoint {
  public String getPath() { return "/stat"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint provides information on the health of "
              + "the Runtime Environment");
          openApiOperation.summary("Health Endpoint");
        })
        .jsonArray("200", Integer.class,
                   openApiParam -> { openApiParam.description("OK."); });
  }

  public Object handleEndpoint(Context ctx) {
    // @TODO return statistics on application health here.

    // https://stackoverflow.com/questions/17374743/how-can-i-get-the-memory-that-my-java-program-uses-via-javas-runtime-api

    return 1;
  }
}
