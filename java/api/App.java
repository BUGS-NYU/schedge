package api;

import io.javalin.Javalin;

public class App {
  public static void run() {
    Javalin app = Javalin.create().start(8080);
    app.get("/:term/:school", ctx -> ctx.result("Hello, World!"));
    app.get("/:term/:school/:subject", ctx -> ctx.result("Hello, World!"));
  }
}
