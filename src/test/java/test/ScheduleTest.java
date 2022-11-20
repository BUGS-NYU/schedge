package test;

import static utils.JsonMapper.*;

import actions.*;
import api.*;
import io.javalin.testtools.JavalinTest;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
        var schedule = fromJson(body, ScheduleSections.Schedule.class);

        Assert.assertFalse("Schedule should be invalid", schedule.valid);
        Assert.assertNotNull("Schedule should have conflict",
                             schedule.conflictA);
        Assert.assertNotNull("Schedule should have conflict",
                             schedule.conflictB);
      }
    });
  }

  @Test
  public void testConflictDifferentCampuses() {
    // 23191, Paris - 09/01/2022 - 12/08/2022 Wed 4.30 PM - 6.00 PM
    // 7443, NYC - 09/01/2022 - 12/14/2022 Mon,Wed 9.30 AM - 10.45 AM

    var paris = ZoneId.of("Europe/Paris");
    var nyc = ZoneId.of("America/New_York");

    var dt1 = "2022-09-07T16:30:00Z";
    var dt2 = "2022-09-07T09:30:00Z";
    var dt3 = "2022-09-07T10:45:00Z";

    var paris1 = Nyu.Meeting.parseTime(dt1).withZoneSameLocal(paris);
    var nyc2 = Nyu.Meeting.parseTime(dt2).withZoneSameLocal(nyc);
    var nyc3 = Nyu.Meeting.parseTime(dt3).withZoneSameLocal(nyc);

    Assert.assertTrue("Times as-stated should conflict (paris1 after nyc2)",
                      paris1.isAfter(nyc2));
    Assert.assertTrue("Times as-stated should conflict (paris1 before nyc3)",
                      paris1.isBefore(nyc3));

    var testUrl = "/api/generateSchedule/fa2022?registrationNumbers=23191,7443";
    var app = App.makeApp();
    JavalinTest.test(app, (server, client) -> {
      try (var resp = client.get(testUrl); var respBody = resp.body()) {
        var body = respBody.string();
        var schedule = fromJson(body, ScheduleSections.Schedule.class);

        Assert.assertFalse("Schedule should be invalid", schedule.valid);
        Assert.assertNotNull("Schedule should have conflict",
                             schedule.conflictA);
        Assert.assertNotNull("Schedule should have conflict",
                             schedule.conflictB);

        System.err.println(schedule.conflictA.campus);

        System.err.println(schedule.conflictB.campus);
        if (schedule.conflictA.campus.contains("Paris")) {
          var temp = schedule.conflictA;
          schedule.conflictA = schedule.conflictB;
          schedule.conflictB = temp;
        }

        Assert.assertEquals(Nyu.Campus.timezoneForCampus(schedule.conflictA.campus), nyc);
        Assert.assertEquals(Nyu.Campus.timezoneForCampus(schedule.conflictB.campus), paris);
        Assert.assertEquals(schedule.conflictA.beginDate.withZoneSameInstant(nyc), nyc2);
        Assert.assertEquals(schedule.conflictB.beginDate.withZoneSameInstant(paris), paris1);
      }
    });
  }
}