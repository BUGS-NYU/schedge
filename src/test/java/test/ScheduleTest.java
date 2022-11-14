package test;

import actions.*;
import api.*;
import io.javalin.testtools.JavalinTest;
import org.junit.*;
import utils.*;

public class ScheduleTest {
  @Test
  public void testConflictIssue90() {
    // https://github.com/A1Liu/schedge/issues/90
    var testUrl = "/api/generateSchedule/sp2021?registrationNumbers=23069,7626";
    var app = App.makeApp();
    JavalinTest.test(app, (server, client) -> {
      try (var resp = client.get(testUrl); var respBody = resp.body()) {

        var body = respBody.string();
        var schedule =
            JsonMapper.fromJson(body, ScheduleSections.Schedule.class);

        Assert.assertFalse(schedule.valid);
      }
    });
  }
}