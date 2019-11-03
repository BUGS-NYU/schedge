package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import models.Course;
import models.Subject;
import models.Term;
import services.JsonMapper;

public class App {
  public static void run() {
    Javalin app = Javalin.create().start(8080);
    app.get("/:term/:school/:subject", ctx -> {
      Term term = Term.fromId(Integer.parseInt(ctx.pathParam("term")));
      Subject subject =
          new Subject(ctx.pathParam("subject"), ctx.pathParam("school"));

      Course[] courses = Course.getCourses(term, subject);
      ctx.result(JsonMapper.toJson(courses));
    });
  }
}
