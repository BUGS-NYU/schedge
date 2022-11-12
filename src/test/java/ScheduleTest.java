import actions.*;
import api.*;
import api.v1.*;
import database.*;
import io.javalin.*;
import org.junit.*;
import io.javalin.testtools.JavalinTest;
import utils.*;

public class ScheduleTest {
    static final Javalin app = App.makeApp();

    @Before
    public void before() {
        // Force the connection to initialize with migrations
        GetConnection.withConnection(conn ->  Migrations.runMigrations(conn));

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
                var schedule = JsonMapper.fromJson(body, ScheduleSections.Schedule.class);

                Assert.assertFalse(schedule.valid);
            }
        });


    }
}