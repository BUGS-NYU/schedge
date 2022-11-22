package api.v1;

import static utils.Nyu.*;

import actions.ScrapeTerm;
import database.GetConnection;
import io.javalin.Javalin;
import io.javalin.websocket.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import me.tongfei.progressbar.*;
import utils.Utils;

/**
 * Scraping endpoint; this intentionally doesn't have API documentation,
 * because it's a private API endpoint.
 */
public final class ScrapingEndpoint {
  private static final AtomicBoolean MUTEX = new AtomicBoolean(false);

  // Base-64 of "schedge:admin"
  private static final String AUTH =
      Utils.getEnvDefault("SCHEDGE_ADMIN_PASSWORD", "c2NoZWRnZTphZG1pbg==");
  private static final String AUTH_STRING = "Basic " + AUTH;

  private static String scrape(WsContext ctx) {
    try {
      var term = Term.fromString(ctx.pathParam("term"));

      var consumer = new DelegatingProgressBarConsumer(ctx::send);
      var builder = new ProgressBarBuilder()
                        .setConsumer(consumer)
                        .setStyle(ProgressBarStyle.ASCII)
                        .setUpdateIntervalMillis(15_000)
                        .setEtaFunction(state -> Optional.empty())
                        .setTaskName("Scrape " + term.json());

      GetConnection.withConnection(conn -> {
        try (var bar = builder.build()) {
          ScrapeTerm.scrapeTerm(conn, term, bar);
        }
      });

      return "Done!";
    } catch (Exception e) {
      var sw = new StringWriter();
      var pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      var stackTrace = sw.toString();

      ctx.send(e.getMessage());
      ctx.send(stackTrace);

      return "Failed: " + e.getMessage();
    }
  }

  public static void add(Javalin app) {
    app.ws("/api/scrape/{term}", ws -> {
      ws.onConnect(ctx -> {
        var authString = ctx.header("Authorization");
        if (authString == null || !authString.equals(AUTH_STRING)) {
          ctx.closeSession(1000, "Failed: Unauthorized");
          return;
        }

        if (!MUTEX.compareAndSet(false, true)) {
          ctx.closeSession(1000, "Already running a scraping job!");
          return;
        }

        // TODO: correctly handle websocket closing, password checking, etc.
        try {
          var closeReason = scrape(ctx);
          ctx.closeSession(1000, closeReason);
        } finally {
          MUTEX.compareAndSet(true, false);
        }
      });

      ws.onMessage(ctx -> {});
    });
  }
}
