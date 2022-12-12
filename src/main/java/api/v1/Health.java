package api.v1;

import api.*;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import org.jetbrains.annotations.NotNull;

public final class Health extends App.Endpoint {
  public static final String BUILD_VERSION = "SCHEDGE DEVELOPMENT BUILD";

  public String getPath() { return "/stat"; }

  public static final class HealthInfo {
    public boolean getAlive() { return true; }
    @NotNull
    public String getVersion() {
      return BUILD_VERSION;
    }
  }

  @OpenApi(path = "/stat", methods = HttpMethod.GET, summary = "Health",
           description = "This endpoint provides information on the health of "
                         +
                         "the Runtime Environment. Currently very incomplete.",
           responses =
           {
             @OpenApiResponse(status = "200", description = "OK.",
                              content =
                                  @OpenApiContent(from = HealthInfo.class))
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
    var info = new HealthInfo();

    return info;
  }
}
