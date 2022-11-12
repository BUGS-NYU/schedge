package test;

import static test.Util.*;

import actions.*;
import api.*;
import api.v1.*;
import database.*;
import io.javalin.*;
import io.javalin.testtools.JavalinTest;
import org.junit.*;
import utils.*;

public class ScheduleTest {

  @Before
  public void before() {

    new ListTermsEndpoint().addTo(app);
    new GenerateScheduleEndpoint().addTo(app);
  }

  @Test
  public void testConflictIssue90() {
    // https://github.com/A1Liu/schedge/issues/90
    var testUrl = "/api/generateSchedule/sp2021?registrationNumbers=23069,7626";
    JavalinTest.test(app, (server, client) -> {
      client.get("/api/terms");

      try (var resp = client.get(testUrl); var respBody = resp.body()) {

        var body = respBody.string();
        var schedule =
            JsonMapper.fromJson(body, ScheduleSections.Schedule.class);

        Assert.assertFalse(schedule.valid);
      }
    });
  }
}