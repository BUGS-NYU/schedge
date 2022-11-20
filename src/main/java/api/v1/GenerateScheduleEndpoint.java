package api.v1;

import static actions.ScheduleSections.*;

import api.*;
import database.*;
import database.models.AugmentedMeeting;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import utils.*;

import java.util.ArrayList;

public final class GenerateScheduleEndpoint extends App.Endpoint {

  public String getPath() { return "/generateSchedule/{term}"; }

  @OpenApi(
          path = "/generateSchedule/{term}", methods = HttpMethod.GET,
          summary = "Scheduler",
          description =  "This endpoint returns either an ordered schedule, or a pair"
                  + " 'conflictA' and 'conflictB'. You can use the 'valid' field "
                  + "to check whether the schedule is valid.",
          pathParams =
                  {
                          @OpenApiParam(name = "term",
                                  description =
                                          SchoolInfoEndpoint.TERM_PARAM_DESCRIPTION,
                                  example = "fa2022", required = true)
                  },
          responses =
                  {
                          @OpenApiResponse(status = "200",
                                  description = "Schedule created for the provided courses",
                                  content = @OpenApiContent(from = Schedule.class))
                          ,
                          @OpenApiResponse(status = "400",
                                  description = "One of the values in the path "
                                          + "parameter was "
                                          + "not valid.",
                                  content =
                                  @OpenApiContent(from = App.ApiError.class))}
  )
  public Object handleEndpoint(Context ctx) {
    String termString = ctx.pathParam("term");
    var term = Nyu.Term.fromString(termString);

    String regNumsString = ctx.queryParam("registrationNumbers");
    if (regNumsString == null) {
      throw new RuntimeException("missing required query parameters");
    }

    String[] regNumsStrArray = regNumsString.split(",");
    if (regNumsStrArray.length == 0) {
      throw new RuntimeException("didn't provide any registration numbers");
    }

    var registrationNumbers = new ArrayList<Integer>();
    for (String regNumString : regNumsStrArray) {
      registrationNumbers.add(Integer.parseInt(regNumString));
    }

    Object output = GetConnection.withConnectionReturning(conn -> {
      ArrayList<AugmentedMeeting> meetings =
          SelectAugmentedMeetings.selectAugmentedMeetings(conn, term,
                                                          registrationNumbers);

      return generateSchedule(meetings);
    });

    return output;
  }
}
