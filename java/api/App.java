package api;

import io.javalin.Javalin;
import models.SubjectCode;
import models.Term;

public class App {
  public static void run() {
    Javalin app = Javalin.create().start(80);
    app.get("/:term/:school/:subject", ctx -> {
      Term term = Term.fromId(Integer.parseInt(ctx.pathParam("term")));
      SubjectCode subjectCode =
          new SubjectCode(ctx.pathParam("subject"), ctx.pathParam("school"));
    });
  }
}
