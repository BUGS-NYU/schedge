package api.v1.endpoints;

import static actions.ScheduleSections.*;

import api.Endpoint;
import api.v1.ApiError;
import api.v1.SelectCourses;
import api.v1.models.Course;
import api.v1.models.Section;
import database.GetConnection;
import database.SelectAugmentedMeetings;
import database.epochs.LatestCompleteEpoch;
import database.models.AugmentedMeeting;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.ArrayList;
import java.util.Collections;
import nyu.SubjectCode;
import nyu.Term;

public final class GenerateScheduleEndpoint extends Endpoint {

  enum SemesterCode {
    su,
    sp,
    fa,
    ja;
  }

  public String getPath() { return "/:year/:semester/generateSchedule"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint returns either an ordered schedule, or a pair"
              + " 'conflictA' and 'conflictB'. You can use the 'valid' field "
              + "to check whether the schedule is valid.");
          openApiOperation.summary("Schedule Checking Endpoint");
        })
        .pathParam("year", Integer.class,
                   openApiParam -> {
                     openApiParam.description("Must be a valid year.");
                   })
        .pathParam("semester", SemesterCode.class,
                   openApiParam -> {
                     openApiParam.description("Must be a valid semester code.");
                   })
        .queryParam("registrationNumbers", String.class,
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
      ctx.contentType("application/json");

      int year;
      ArrayList<Integer> registrationNumbers = new ArrayList<>();
      try {
        year = Integer.parseInt(ctx.pathParam("year"));

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

        for (String regNumString : regNumsStrArray) {
          registrationNumbers.add(Integer.parseInt(regNumString));
        }

      } catch (NumberFormatException e) {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));
        return;
      }

      Term term;
      try {
        term = new Term(ctx.pathParam("semester"), year);
      } catch (IllegalArgumentException e) {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));
        return;
      }

      String fullData = ctx.queryParam("full");

      ctx.status(200);
      Object output = GetConnection.withConnectionReturning(conn -> {
        Integer epoch = LatestCompleteEpoch.getLatestEpoch(conn, term);
        if (epoch == null) {
          return new Schedule(new ArrayList<>());
        }

        ArrayList<AugmentedMeeting> meetings =
            SelectAugmentedMeetings.selectAugmentedMeetings(
                conn, epoch, registrationNumbers);

        return generateSchedule(meetings);
      });
      ctx.json(output);
    };
  }
}
