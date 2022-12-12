package api.v1;

import static utils.ArrayJS.*;
import static utils.Nyu.*;
import static utils.Try.*;

import actions.WriteTerm;
import database.GetConnection;
import io.javalin.Javalin;
import io.javalin.websocket.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import me.tongfei.progressbar.*;
import org.slf4j.*;
import scraping.*;
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
  private static final boolean DISABLED;
  private static final byte[] AUTH_HASH;

  static {
    var password = Utils.getEnvDefault("SCHEDGE_ADMIN_PASSWORD", "");
    var passwordBytes = password.getBytes(StandardCharsets.UTF_8);
    var encoded = Base64.getEncoder().encodeToString(passwordBytes);
    AUTH_STRING = "Basic " + encoded;
    DISABLED = password.isEmpty();

    var digest = tcPass(() -> MessageDigest.getInstance("SHA-256"));
    AUTH_HASH = digest.digest(AUTH_STRING.getBytes(StandardCharsets.UTF_8));
  }

  interface ScrapeJob {
    public TermScrapeResult scrape(Term term, Consumer<ScrapeEvent> event);
  }

  private static String scrape(WsContext ctx) {
    try {
      var term = Term.fromString(ctx.pathParam("term"));

      var source = run(() -> {
        var src = ctx.queryParam("source");
        if (src == null)
          src = "";
        if (src.trim().isEmpty())
          src = "sis.nyu.edu";

        return src;
      });

      var builder = new ProgressBarBuilder();
      builder.setTaskName(source + " " + term.json())
          .setConsumer(new DelegatingProgressBarConsumer(ctx::send, 160))
          .setStyle(ProgressBarStyle.ASCII)
          .setUpdateIntervalMillis(5_000)
          .continuousUpdate();

      var bar = builder.build();

      ScrapeJob job = run(() -> {
        switch (source) {
        case "schedge-v1":
          return ScrapeSchedgeV1::scrapeFromSchedge;
        case "sis.nyu.edu":
          return PeopleSoftClassSearch::scrapeTerm;
        default: {
          var warning = "Invalid source: " + source;
          ctx.send(warning);
          logger.warn(warning);

          return null;
        }
        }
      });

      if (job == null) {
        return "No job to run!";
      }

      GetConnection.withConnection(conn -> {
        var result = job.scrape(term, e -> {
          switch (e.kind) {
          case MESSAGE:
          case SUBJECT_START:
            bar.setExtraMessage(String.format("%1$-25s", e.message));
            logger.info(e.message);
            break;
          case WARNING:
            ctx.send(e.message);
            logger.warn(e.message);
            break;
          case PROGRESS:
            bar.stepBy(e.value);
            break;
          case HINT_CHANGE:
            bar.maxHint(e.value);
            break;
          }
        });

        WriteTerm.writeTerm(conn, result);
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
        if (DISABLED) {
          ctx.closeSession(1000, "Failed: Unauthorized");
          return;
        }

        var authString = ctx.header("Authorization");
        authString = authString != null ? authString : "";

        var digest = tcPass(() -> MessageDigest.getInstance("SHA-256"));
        var authBytes = authString.getBytes(StandardCharsets.UTF_8);
        var authHash = digest.digest(authBytes);

        if (!Arrays.equals(authHash, AUTH_HASH) ||
            !authString.equals(AUTH_STRING)) {
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

        CompletableFuture.runAsync(() -> {
          var closeCode = 1011;
          var closeReason = "Unknown reason";

          try {
            task.run();
            closeReason = task.get();

            if (closeReason.equals("Done!"))
              closeCode = 1000;
          } catch (
              CancellationException e) { // Do nothing because job was cancelled
            closeReason = "Job was cancelled";
            logger.info(closeReason);
          } catch (InterruptedException
                       e) { // Do nothing because job was interrupted
            closeReason = "Job was interrupted";
            logger.info(closeReason);
          } catch (ExecutionException e) {
            closeReason = "Job failed: " + e.getMessage();
            logger.error(closeReason, e);
          } finally {
            var newLock = new Object();
            if (!MUTEX.compareAndSet(id, newLock)) {
              return;
            }

            CURRENT_SCRAPE = null;
            ctx.attribute("identity", null);
            ctx.send(closeReason);
            ctx.closeSession(closeCode, closeReason);

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
