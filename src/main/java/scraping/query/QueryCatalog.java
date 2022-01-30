package scraping.query;

import static utils.TryCatch.*;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import nyu.SubjectCode;
import nyu.Term;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import org.slf4j.*;
import scraping.models.CatalogQueryData;
import utils.SimpleBatchedFutureEngine;
import utils.TryCatch;

public final class QueryCatalog {

  private static Logger logger =
      LoggerFactory.getLogger("scraping.query.QueryCatalog");
  private static final String ROOT_URL_STRING =
      "https://m.albert.nyu.edu/app/catalog/classSearch";
  private static final Uri ROOT_URI = Uri.create(ROOT_URL_STRING);
  private static final Uri DATA_URI =
      Uri.create("https://m.albert.nyu.edu/app/catalog/getClassSearch");

  // does anybody even use this?
  public static CatalogQueryData queryCatalog(Term term, SubjectCode subject) {
    CatalogQueryData queryData = null;

    try {
      queryData = queryCatalog(term, subject, getContextAsync().get()).get();
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(
          "No classes found matching criteria subject=" + subject.code, e);
    }

    if (queryData == null)
      throw new RuntimeException("No classes found matching criteria subject=" +
                                 subject.code);

    return queryData;
  }

  public static Stream<CatalogQueryData>
  queryCatalog(Term term, Iterable<SubjectCode> subjects, int batchSize) {
    logger.debug("querying catalog for term={} with multiple subjects...",
                 term);

    HttpContext[] contexts = new HttpContext[batchSize];
    {
      @SuppressWarnings("unchecked")
      Future<HttpContext>[] contextFutures = new Future[batchSize];
      logger.info("Sending context requests... (x{})", batchSize);

      for (int i = 0; i < batchSize; i++) {
        contextFutures[i] = getContextAsync();
      }

      logger.info("Collecting context requests... (x{})", batchSize);

      TryCatch tc = tcNew(logger, "Failed to get HttpContext.");
      for (int i = 0; i < batchSize; i++) {
        int idx = i;
        contexts[idx] = tc.pass(() -> nonnull(contextFutures[idx].get()));
      }
    }

    logger.info("Collected context requests... (x{})", batchSize);

    return StreamSupport
        .stream(
            new SimpleBatchedFutureEngine<>(
                subjects.iterator(), batchSize,
                (subject, idx) -> queryCatalog(term, subject, contexts[idx]))
                .spliterator(),
            false)
        .filter(i -> i != null);
  }

  /**
   * Note: This is the meat of the catalog query.
   * To make the query, two most important things are
   * school code and subject code. For NYU Shanghai, the
   * school code: UI and subject code: {subject}-SHU.
   * For NYU Undegraduate School of Public Health, school
   * code: UU and subject code: {subject}-GU. They will be
   * changed at runtime.
   * @param term
   * @param subject
   * @param context
   */
  private static Future<CatalogQueryData>
  queryCatalog(Term term, SubjectCode subject, HttpContext context) {
    logger.debug("querying catalog for term=" + term +
                 " and subject=" + subject + "...");

    String csrf = context.csrfToken;
    int id = term.getId();
    String code = subject.code;
    String school = subject.schoolCode;

    String format = "CSRFToken=%s&term=%d&acad_group=%s&subject=%s";
    String params = String.format(format, csrf, id, school, code);
    logger.debug("Params are {}.", params);

    Request request =
        new RequestBuilder()
            .setUri(DATA_URI)
            .setRequestTimeout(10000)
            .setHeader("Referer", ROOT_URL_STRING + "/" + term.getId())
            .setHeader("Host", "m.albert.nyu.edu")
            .setHeader("Accept-Language", "en-US,en;q=0.5")
            .setHeader("Accept-Encoding", "gzip, deflate, br")
            .setHeader("Content-Type",
                       "application/x-www-form-urlencoded; charset=UTF-8")
            .setHeader("X-Requested-With", "XMLHttpRequest")
            .setHeader("Origin", "https://m.albert.nyu.edu")
            .setHeader("DNT", "1")
            .setHeader("Connection", "keep-alive")
            .setHeader("Referer",
                       "https://m.albert.nyu.edu/app/catalog/classSearch")
            .setHeader("Cookie", context.cookies.stream()
                                     .map(it -> it.name() + '=' + it.value())
                                     .collect(Collectors.joining("; ")))
            .setMethod("POST")
            .setBody(params)
            .build();

    return GetClient.getClient()
        .executeRequest(request)
        .toCompletableFuture()
        .handleAsync((resp, throwable) -> {
          if (resp == null) {
            logger.error("Exception thrown for request: subject={}", subject,
                         throwable);

            return null;
          }

          String body = resp.getResponseBody();
          if (body.contentEquals("No classes found matching your criteria.")) {
            logger.warn("No classes found matching criteria subject={}",
                        subject.code);

            return null;
          }

          return new CatalogQueryData(subject, body);
        });
  }

  private static Future<HttpContext> getContextAsync() {
    logger.debug("Getting CSRF token...");
    Request request =
        new RequestBuilder().setUri(ROOT_URI).setMethod("GET").build();

    return GetClient.getClient()
        .executeRequest(request)
        .toCompletableFuture()
        .handleAsync((resp, throwable) -> {
          if (resp == null) {
            logger.error(throwable.getMessage());
            return null;
          }

          List<Cookie> cookies =
              resp.getHeaders()
                  .getAll("Set-Cookie")
                  .stream()
                  .map(cookie -> ClientCookieDecoder.STRICT.decode(cookie))
                  .collect(Collectors.toList());

          Cookie csrfCookie =
              cookies.stream()
                  .filter(cookie -> cookie.name().contentEquals("CSRFCookie"))
                  .findAny()
                  .orElse(null);

          if (csrfCookie == null) {
            logger.error("Couldn't find cookie with name=CSRFCookie");
            return null;
          }

          logger.debug("Retrieved CSRF token `{}`", csrfCookie.value());

          return new HttpContext(csrfCookie.value(), cookies);
        });
  }

  private static class HttpContext {
    final String csrfToken;
    final List<Cookie> cookies;

    HttpContext(String tok, List<Cookie> cookies) {
      this.csrfToken = tok;
      this.cookies = cookies;
    }
  }
}
