package api.v1;

import static utils.Nyu.*;

import actions.ScrapeTerm;
import database.GetConnection;
import io.javalin.Javalin;
import io.javalin.websocket.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import me.tongfei.progressbar.*;
import org.slf4j.*;
import utils.Utils;

/**
 * Scraping endpoint; this intentionally doesn't have API documentation,
 * because it's a private API endpoint.
 */
public final class ScrapingEndpoint {
  private static final Logger logger =
      LoggerFactory.getLogger("api.v1.ScrapingEndpoint");
  public static final class Ref<T> { // IDK if this already exists
    public T current;

    public Ref(T t) { this.current = t; }

    public Ref() { this.current = null; }
  }

  private static final AtomicBoolean MUTEX = new AtomicBoolean(false);
  private static volatile Future<String> CURRENT_SCRAPE = null;

  // The default is the base-64 of "schedge:admin"
  // To run this in dev, please run `yarn wscat
  private static final String AUTH_STRING;

  static {
    var defaultPassword = "schedge:admin";
    var encoded = Base64.getEncoder().encodeToString(
        defaultPassword.getBytes(StandardCharsets.UTF_8));
    var authString = Utils.getEnvDefault("SCHEDGE_ADMIN_PASSWORD", encoded);
    AUTH_STRING = "Basic " + authString;
  }

  private static String scrape(WsContext ctx) {
    try {
      var term = Term.fromString(ctx.pathParam("term"));

      var subject = new Ref<String>();
      var count = new Ref<>(0);
      var total = new Ref<>("?");
      GetConnection.withConnection(conn -> {
        ScrapeTerm.scrapeTerm(conn, term, e -> {
          switch (e.kind) {
          case MESSAGE:
            ctx.send(e.message);
            break;
          case SUBJECT_START:
            subject.current = e.currentSubject;
            ctx.send(e.message);
            break;
          case WARNING:
            ctx.send(e.message);
            logger.warn(e.message);
            break;
          case PROGRESS:
            count.current += e.value;
            if (subject.current != null) {
              ctx.send("Finished fetch for " + subject.current +
                       " (Completed " + count.current + "/" + total.current +
                       ")");
            }
            break;
          case HINT_CHANGE:
            if (e.value < 0) {
              total.current = "?";
            } else {
              total.current = "" + e.value;
            }
            break;
          }
        });
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
        // Also, do a hash-compare before doing the equality check
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
