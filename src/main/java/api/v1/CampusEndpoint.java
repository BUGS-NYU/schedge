package api.v1;

import static utils.Nyu.*;

import api.*;
import database.GetConnection;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import java.util.*;

public final class CampusEndpoint extends App.Endpoint {

  public String getPath() { return "/campus"; }

  class Data {
    public HashMap<String, Campus> campuses;

    public HashMap<String, Campus> getCampuses() { return campuses; }
  }

  @OpenApi(
      path = "/api/campus", methods = HttpMethod.GET, summary = "Campuses",
      description = "Lists all campuses that Schedge is currently aware of.",
      responses =
      {
        @OpenApiResponse(status = "200",
                         description = "Success. Lists out campuses",
                         content = @OpenApiContent(from = Data.class))
        ,
            @OpenApiResponse(status = "400",
                             description = "One of the values in the path "
                                           + "parameter was "
                                           + "not valid.",
                             content =
                                 @OpenApiContent(from = App.ApiError.class))
      })
  public Object
  handleEndpoint(Context ctx) {
    var data = new Data();
    data.campuses = Campus.campuses;

    return data;
  }
}
