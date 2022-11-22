package api.v1;

import static utils.Nyu.*;

import actions.ScrapeTerm;
import database.GetConnection;
import io.javalin.Javalin;
import io.javalin.websocket.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.*;
import me.tongfei.progressbar.*;
import utils.Utils;

/**
 * Scraping endpoint; this intentionally doesn't have API documentation,
 * because it's a private API endpoint.
 */
public final class ScrapingEndpoint {
  private static final AtomicReference<Future<String>> MUTEX = new AtomicReference<>(null);
  private static final String PASSWORD =
      Utils.getEnvDefault("SCHEDGE_ADMIN_PASSWORD", "");

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
      ws.onConnect(
          ctx -> { ctx.attribute("messageCount", new AtomicInteger(0)); });

      ws.onMessage(ctx -> {
        var wsMutex = (AtomicInteger)ctx.attribute("messageCount");
        if (wsMutex.incrementAndGet() > 1) {
          ctx.closeSession(1000, "Failed: Sent too many messages");
          return;
        }

        var message = ctx.message();
        if (!message.equals(PASSWORD)) {
          ctx.closeSession(1000, "Failed: Unauthorized");
          return;
        }


        if (!MUTEX.compareAndSet(null)) {
          return "Already running a scraping job!";
        }

        var closeReason = scrape(ctx);
        ctx.closeSession(1000, closeReason);
      });
    });
  }
}
