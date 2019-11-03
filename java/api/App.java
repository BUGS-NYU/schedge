package api;

import io.javalin.Javalin;
import models.Course;
import models.Subject;
import models.Term;

public class App {
  public static void run() {
    Javalin app = Javalin.create().start(8080);
    app.get("/:term/:school/:subject", ctx -> {
      Term term = Term.fromId(Integer.parseInt(ctx.pathParam("term")));
      Subject subject =
          new Subject(ctx.pathParam("subject"), ctx.pathParam("school"));

      // ctx.result(term.toString() + ' ' + subject.toString());
      StringBuilder s = new StringBuilder();
      for (Course course : Course.getCourses(term, subject)) {
        System.out.println(course.toString());
        s.append(course.toString());
      }
      ctx.result(s.toString());
    });
  }
}
