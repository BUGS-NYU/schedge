package test;

import static actions.CopyTermFromProduction.*;
import static utils.JsonMapper.*;
import static utils.Nyu.*;

import api.*;
import io.javalin.testtools.JavalinTest;
import org.junit.*;

public class CoursesTest {
  @Test
  public void testFeature182() {
    var subjects = new String[] {"CSCI-UA", "SCA-UA_1", "MATH-UA"};

    var testUrl = "/api/courses/sp2021/";
    var app = App.makeApp();
    JavalinTest.test(
        app,
        (server, client) -> {
          // https://github.com/A1Liu/schedge/issues/181
          for (var subject : subjects) {
            try (var resp = client.get(testUrl + subject);
                var respBody = resp.body()) {
              var body = respBody.string();
              var courses = fromJson(body, Course[].class);

              for (var course : courses) {
                Assert.assertTrue(course.subjectCode.equals(subject));
              }
            }
          }
        });
  }

  @Test
  public void testCoursesInvalidSubject() {
    var subjects = new String[] {"CSCI-UAd", "CSCI-d", "mEdew"};

    var testUrl = "/api/courses/sp2021/";
    var app = App.makeApp();
    JavalinTest.test(
        app,
        (server, client) -> {
          for (var subject : subjects) {
            try (var resp = client.get(testUrl + subject);
                var respBody = resp.body()) {

              Assert.assertEquals(resp.code(), 400);

              var body = respBody.string();
              var expected =
                  "{\"status\":400,\"message\":\"the subject \\\""
                      + subject
                      + "\\\" is invalid for the term Term[semester=sp, year=2021]\"}";
              Assert.assertEquals(body.toLowerCase(), expected.toLowerCase());
            }
          }
        });
  }
}
