package test;

import static utils.JsonMapper.*;

import actions.*;
import api.*;
import io.javalin.testtools.JavalinTest;
import java.time.ZoneId;
import org.junit.*;
import utils.*;

public class ScheduleTest {
  @Test
  public void testConflictIssue90() {
    // https://github.com/A1Liu/schedge/issues/90
    var testUrl = "/api/generateSchedule/sp2021?registrationNumbers=23069,7626";
    var app = App.makeApp();
    JavalinTest.test(
        app,
        (server, client) -> {
          try (var resp = client.get(testUrl);
              var respBody = resp.body()) {

            var body = respBody.string();
            var schedule = fromJson(body, ScheduleSections.Schedule.class);

            Assert.assertFalse("Schedule should be invalid", schedule.valid);
            Assert.assertNotNull("Schedule should have conflict", schedule.conflictA);
            Assert.assertNotNull("Schedule should have conflict", schedule.conflictB);
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

    Assert.assertTrue("Times as-stated should conflict (paris1 after nyc2)", paris1.isAfter(nyc2));
    Assert.assertTrue(
        "Times as-stated should conflict (paris1 before nyc3)", paris1.isBefore(nyc3));

    var testUrl = "/api/generateSchedule/fa2022?registrationNumbers=23191,7443";
    var app = App.makeApp();
    JavalinTest.test(
        app,
        (server, client) -> {
          try (var resp = client.get(testUrl);
              var respBody = resp.body()) {
            var body = respBody.string();
            var schedule = fromJson(body, ScheduleSections.Schedule.class);

            Assert.assertEquals(
                "Schedule should have the right number of meetings", 3, schedule.size());
            Assert.assertFalse("Schedule should be invalid", schedule.valid);
            Assert.assertNotNull("Schedule should have conflict", schedule.conflictA);
            Assert.assertNotNull("Schedule should have conflict", schedule.conflictB);

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

  @Test
  public void testConflictDifferentCampusesDaylightSavings() {
    // 23190, Paris - 09/01/2022 - 12/08/2022 Mon,Wed 3.00 PM - 4.15 PM
    // 7444, NYC - 09/01/2022 - 12/14/2022 Mon,Wed 11.00 AM - 12.15 PM

    var paris = ZoneId.of("Europe/Paris");
    var nyc = ZoneId.of("America/New_York");

    var dt1 = "2022-09-05T15:00:00Z";
    var dt2 = "2022-11-01T16:15:00Z";
    var dt3 = "2022-09-05T11:00:00Z";
    // var dt4 = "2022-09-05T12:15:00Z";

    var paris1 = Nyu.Meeting.parseTime(dt1).withZoneSameLocal(paris);
    var paris2 = Nyu.Meeting.parseTime(dt2).withZoneSameLocal(paris);
    var nyc3 = Nyu.Meeting.parseTime(dt3).withZoneSameLocal(nyc);
    // var nyc4 = Nyu.Meeting.parseTime(dt4).withZoneSameLocal(nyc);

    Assert.assertTrue(
        "Times as-stated should not conflict (paris1 before nyc3)", paris1.isBefore(nyc3));
    Assert.assertFalse(
        "Times as-stated should not conflict (paris2 before nyc3)", paris2.isBefore(nyc3));

    var dt5 = "2022-11-01T15:00:00Z";
    var dt6 = "2022-11-01T16:15:00Z";
    var dt7 = "2022-11-01T11:00:00Z";
    var dt8 = "2022-11-01T12:15:00Z";

    var paris5 = Nyu.Meeting.parseTime(dt5).withZoneSameLocal(paris);
    var paris6 = Nyu.Meeting.parseTime(dt6).withZoneSameLocal(paris);
    var nyc7 = Nyu.Meeting.parseTime(dt7).withZoneSameLocal(nyc);
    var nyc8 = Nyu.Meeting.parseTime(dt8).withZoneSameLocal(nyc);

    Assert.assertTrue(
        "Times after Paris DST change should conflict (paris5 before nyc8)", paris5.isBefore(nyc8));
    Assert.assertTrue(
        "Times after Paris DST change should conflict (nyc7 before paris6)", nyc7.isBefore(paris6));

    var testUrl = "/api/generateSchedule/fa2022?registrationNumbers=23190,7444";
    var app = App.makeApp();
    JavalinTest.test(
        app,
        (server, client) -> {
          try (var resp = client.get(testUrl);
              var respBody = resp.body()) {
            var body = respBody.string();
            var schedule = fromJson(body, ScheduleSections.Schedule.class);

            Assert.assertEquals(
                "Schedule should have the right number of meetings", 4, schedule.size());
            Assert.assertFalse("Schedule should be invalid", schedule.valid);
            Assert.assertNotNull("Schedule should have conflict", schedule.conflictA);
            Assert.assertNotNull("Schedule should have conflict", schedule.conflictB);

            System.err.println(schedule.conflictA.campus);

            System.err.println(schedule.conflictB.campus);
            if (schedule.conflictA.campus.contains("Paris")) {
              var temp = schedule.conflictA;
              schedule.conflictA = schedule.conflictB;
              schedule.conflictB = temp;
            }

            Assert.assertEquals(Nyu.Campus.timezoneForCampus(schedule.conflictA.campus), nyc);
            Assert.assertEquals(Nyu.Campus.timezoneForCampus(schedule.conflictB.campus), paris);
            Assert.assertEquals(schedule.conflictA.beginDate.withZoneSameInstant(nyc), nyc3);
            Assert.assertEquals(schedule.conflictB.beginDate.withZoneSameInstant(paris), paris1);
          }
        });
  }
}
