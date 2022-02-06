package api.v1;

import static actions.ScheduleSections.*;
import static utils.TryCatch.*;

import api.*;
import database.*;
import database.models.AugmentedMeeting;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.ArrayList;
import types.*;
import utils.*;

public final class GenerateScheduleEndpoint extends Endpoint {

  enum SemesterCode {
    su,
    sp,
    fa,
    ja;
  }

  public String getPath() { return "/{term}/generateSchedule"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint returns either an ordered schedule, or a pair"
              + " 'conflictA' and 'conflictB'. You can use the 'valid' field "
              + "to check whether the schedule is valid.");
          openApiOperation.summary("Schedule Checking Endpoint");
        })
        .pathParam(
            "term", String.class,
            openApiParam -> {
              openApiParam.description(
                  "Must be a valid term code, either 'current', 'next', or something "
                  + "like sp2021 for Spring 2021. Use 'su' for Summer, 'sp' "
                  + "for Spring, 'fa' for Fall, and 'ja' for January/JTerm");
            })
        .queryParam("registrationNumbers", String.class, false,
                    openApiParam
                    -> openApiParam.description("CSV of registration numbers"))
        .json("400", ApiError.class,
              openApiParam -> {
                openApiParam.description(
                    "One of the values in the path parameter was not valid.");
              })
        .json("200", Schedule.class, openApiParam -> {
          openApiParam.description("OK.");

          ArrayList<Section> sections = new ArrayList<>();
        });
  }

  public Handler getHandler() {
    return ctx -> {
      TryCatch tc = tcNew(e -> {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));
      });

      Term term = tc.log(() -> {
        String termString = ctx.pathParam("term");
        if (termString.contentEquals("current")) {
          return Term.getCurrentTerm();
        }

        if (termString.contentEquals("next")) {
          return Term.getCurrentTerm().nextTerm();
        }

        int year = Integer.parseInt(termString.substring(2));
        return new Term(termString.substring(0, 2), year);
      });

      if (term == null)
        return;

      String regNumsString = ctx.queryParam("registrationNumbers");
      if (regNumsString == null) {
        ctx.status(400);
        ctx.json(new ApiError("missing required query parameters"));
        return;
      }

      String[] regNumsStrArray = regNumsString.split(",");
      if (regNumsStrArray.length == 0) {
        ctx.status(400);
        ctx.json(new ApiError("didn't provide any regstration numbers"));
        return;
      }

      ArrayList<Integer> registrationNumbers = tc.log(() -> {
        ArrayList<Integer> numbers = new ArrayList<>();

        for (String regNumString : regNumsStrArray) {
          numbers.add(Integer.parseInt(regNumString));
        }

        return numbers;
      });

      if (registrationNumbers == null)
        return;

      Object output = GetConnection.withConnectionReturning(conn -> {
        Integer epoch = Epoch.getLatestEpoch(conn, term);
        if (epoch == null) {
          return new Schedule(new ArrayList<>());
        }

        ArrayList<AugmentedMeeting> meetings =
            SelectAugmentedMeetings.selectAugmentedMeetings(
                conn, epoch, registrationNumbers);

        return generateSchedule(meetings);
      });

      ctx.status(200);
      ctx.json(output);
    };
  }
}
