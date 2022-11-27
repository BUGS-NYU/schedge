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
import scraping.PeopleSoftClassSearch;
import utils.ArrayJS;
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

  private static final AtomicReference<Object> MUTEX =
      new AtomicReference<>(null);
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
            logger.info(e.message);
            break;
          case SUBJECT_START:
            subject.current = e.currentSubject;
            ctx.send(e.message);
            logger.info(e.message);
            break;
          case WARNING:
            ctx.send(e.message);
            logger.warn(e.message);
            break;
          case PROGRESS:
            count.current += e.value;
            if (subject.current != null) {
              ctx.send("Finished " + subject.current + " fetch (Completed " +
                       count.current + "/" + total.current + ")");
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
      var stackTrace = Utils.stackTrace(e);

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

        var id = new Object();
        ctx.attribute("identity", id);

        if (!MUTEX.compareAndSet(null, id)) {
          ctx.closeSession(1000, "Already running a scraping job!");
          return;
        }

        var task = new FutureTask<>(() -> scrape(ctx));
        CURRENT_SCRAPE = task;

        // TODO: correctly handle websocket closing, password checking, etc.
        // Also, do a hash-compare before doing the equality check
        CompletableFuture.runAsync(() -> {
          var closeReason = "Unknown reason";
          try {
            task.run();
            closeReason = task.get();
          } catch (
              CancellationException e) { // Do nothing because job was cancelled
            closeReason = "Job was cancelled";
            logger.info(closeReason);
          } catch (InterruptedException
                       e) { // Do nothing because job was interrupted
            closeReason = "Job was interrupted";
            logger.info(closeReason);
          } catch (ExecutionException e) {
            closeReason = "Job failed";
            logger.error(closeReason, e);
          } finally {
            var newLock = new Object();
            if (!MUTEX.compareAndSet(id, newLock)) {
              return;
            }

            CURRENT_SCRAPE = null;
            ctx.attribute("identity", null);
            ctx.closeSession(1000, closeReason);

            if (!MUTEX.compareAndSet(newLock, null)) {
              logger.warn("Failed to unlock mutex");
            }
          }
        });
      });

      ws.onMessage(ctx -> {});

      ws.onClose(ctx -> {
        var id = ctx.attribute("identity");
        if (id == null)
          return;

        logger.info("Closing scrape early");

        var newLock = new Object();
        if (!MUTEX.compareAndSet(id, newLock)) {
          return;
        }

        CURRENT_SCRAPE.cancel(true);
        CURRENT_SCRAPE = null;

        if (!MUTEX.compareAndSet(newLock, null)) {
          logger.warn("Failed to unlock mutex");
        }
      });
    });
  }
}
