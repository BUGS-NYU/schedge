package api.v1;

import static utils.Nyu.*;

import actions.ScrapeTerm;
import database.GetConnection;
import io.javalin.Javalin;
import io.javalin.websocket.WsConnectContext;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.eclipse.jetty.websocket.api.CloseStatus;

/**
 * Scraping endpoint; this intentionally doesn't have API documentation,
 * because it's a private API endpoint.
 */
public final class ScrapingEndpoint {
  private static AtomicBoolean MUTEX = new AtomicBoolean(false);
  public static void onConnect(WsConnectContext ctx) {
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
        ctx.send("Done!");
      }
    });
  }

  public static void add(Javalin app) {
    app.ws("/api/scrape/{term}", ws -> {
      ws.onConnect(ctx -> {
        try {
          if (!MUTEX.compareAndSet(false, true)) {
            ctx.closeSession(1000, "Already running a scraping job!");
            return;
          }

          onConnect(ctx);

          ctx.closeSession(1000, "Done!");
        } catch (Exception e) {
          var sw = new StringWriter();
          var pw = new PrintWriter(sw);
          e.printStackTrace(pw);
          var stackTrace = sw.toString();

          ctx.send(e.getMessage());
          ctx.send(stackTrace);

          ctx.closeSession(1000, "Failed: " + e.getMessage());
        } finally {
          MUTEX.compareAndSet(true, false);
        }
      });
    });
  }
}
