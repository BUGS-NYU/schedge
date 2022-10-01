package api.v1;

import static actions.ScheduleSections.*;
import static types.Nyu.*;

import api.*;
import database.*;
import database.models.AugmentedMeeting;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.ArrayList;
import types.*;

public final class GenerateScheduleEndpoint extends App.Endpoint {

  public String getPath() { return "/generateSchedule/{term}"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint returns either an ordered schedule, or a pair"
              + " 'conflictA' and 'conflictB'. You can use the 'valid' field "
              + "to check whether the schedule is valid.");
          openApiOperation.summary("Schedule Checking Endpoint");
        })
        .pathParam("term", String.class,
                   openApiParam -> {
                     openApiParam.description(
                         SchoolInfoEndpoint.TERM_PARAM_DESCRIPTION);
                   })
        .queryParam("registrationNumbers", String.class, false,
                    openApiParam
                    -> openApiParam.description("CSV of registration numbers"))
        .json("400", App.ApiError.class,
              openApiParam -> {
                openApiParam.description(
                    "One of the values in the path parameter was not valid.");
              })
        .json("200", Schedule.class, openApiParam -> {
          openApiParam.description("OK.");

          ArrayList<Section> sections = new ArrayList<>();
        });
  }

  public Object handleEndpoint(Context ctx) {
    String termString = ctx.pathParam("term");
    var term = SchoolInfoEndpoint.parseTerm(termString);

    String regNumsString = ctx.queryParam("registrationNumbers");
    if (regNumsString == null) {
      throw new RuntimeException("missing required query parameters");
    }

    String[] regNumsStrArray = regNumsString.split(",");
    if (regNumsStrArray.length == 0) {
      throw new RuntimeException("didn't provide any regstration numbers");
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
