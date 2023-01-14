package test;

import static utils.JsonMapper.*;
import static utils.Nyu.*;

import api.*;
import io.javalin.testtools.JavalinTest;
import org.junit.*;

public class SearchTest {
  @Test
  public void testIssue208() {
    var testUrl = "/api/search/sp2023?query=web&limit=50";
    var app = App.makeApp();
    JavalinTest.test(
        app,
        (server, client) -> {
          // https://github.com/A1Liu/schedge/issues/208
          try (var resp = client.get(testUrl);
              var respBody = resp.body()) {
            var body = respBody.string();
            System.err.println(body);
            var courses = fromJson(body, Course[].class);

            Assert.assertTrue(courses.length > 0);
            Assert.assertTrue(courses.length <= 50);
          }
        });
  }
}
