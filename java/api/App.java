package api;

import io.javalin.Javalin;

public class App {
  public static void run() {
    Javalin app = Javalin.create().start(8080);
    app.get("/:term", ctx -> ctx.result("Hello, World!"));
  }
}
