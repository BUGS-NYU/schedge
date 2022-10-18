package api.v1;

import api.*;
import io.javalin.http.Context;
import io.javalin.openapi.*;

public final class Health extends App.Endpoint {
  public String getPath() { return "/stat"; }

  @OpenApi(path = "/stat", methods = HttpMethod.GET, summary = "Health",
           description = "This endpoint provides information on the health of "
                         + "the Runtime Environment",
           responses =
           {
             @OpenApiResponse(status = "200", description = "OK.",
                              content = @OpenApiContent(from = Integer.class))
             ,
                 @OpenApiResponse(
                     status = "400",
                     description = "Something's messed up with the server",
                     content = @OpenApiContent(from = App.ApiError.class))
           })
  public Object
  handleEndpoint(Context ctx) {
    // @TODO return statistics on application health here.

    // https://stackoverflow.com/questions/17374743/how-can-i-get-the-memory-that-my-java-program-uses-via-javas-runtime-api

    return 1;
  }
}
