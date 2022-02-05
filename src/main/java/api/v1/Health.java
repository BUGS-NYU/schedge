package api.v1;

import static utils.TryCatch.*;

import api.*;
import database.GetConnection;
import database.epochs.LatestCompleteEpoch;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.*;
import types.*;
import utils.TryCatch;

public final class Health extends Endpoint {
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

  public Handler getHandler() {
    return ctx -> {
      // @TODO return statistics on application health here.

      // https://stackoverflow.com/questions/17374743/how-can-i-get-the-memory-that-my-java-program-uses-via-javas-runtime-api

      ctx.json(1);
    };
  }
}
