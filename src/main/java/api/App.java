package api;

import io.javalin.Javalin;
import models.Course;
import models.SubjectCode;
import models.Term;
import services.JsonMapper;
import services.SelectCourses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
  public static void run() {
    Javalin app = Javalin.create().start(80);
    Logger logger = LoggerFactory.getLogger("app");

    app.get("/:term/:school/:subject", ctx -> {
      Term term = Term.fromId(Integer.parseInt(ctx.pathParam("term")));
      SubjectCode subject =
          new SubjectCode(ctx.pathParam("subject"), ctx.pathParam("school"));

      ctx.result(JsonMapper.toJson(
          SelectCourses.selectCourses(logger, term, subject)));
    });
  }
}
