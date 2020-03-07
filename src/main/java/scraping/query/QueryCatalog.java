package scraping.query;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import nyu.SubjectCode;
import nyu.Term;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.models.CatalogQueryData;
import utils.SimpleBatchedFutureEngine;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class QueryCatalog {

  private static Logger logger =
      LoggerFactory.getLogger("scraping.query.QueryCatalog");
  private static final String ROOT_URL_STRING =
      "https://m.albert.nyu.edu/app/catalog/classSearch";
  private static final Uri ROOT_URI = Uri.create(ROOT_URL_STRING);
  private static final Uri DATA_URI =
      Uri.create("https://m.albert.nyu.edu/app/catalog/getClassSearch");

  public static CatalogQueryData queryCatalog(Term term,
                                              SubjectCode subjectCode) {
    CatalogQueryData queryData = null;
    try {
      queryData =
          queryCatalog(term, subjectCode, getContextAsync().get()).get();
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(
          "No classes found matching criteria school=" + subjectCode.school +
              ", subject=" + subjectCode.getAbbrev(),
          e);
    }

    if (queryData == null)
      throw new RuntimeException(
          "No classes found matching criteria school=" + subjectCode.school +
          ", subject=" + subjectCode.getAbbrev());
    return queryData;
  }

  public static Stream<CatalogQueryData>
  queryCatalog(Term term, Iterable<SubjectCode> subjectCodes,
               Integer batchSizeNullable) {
    logger.debug("querying catalog for term={} with multiple subjects...",
                 term);

    int batchSize = batchSizeNullable != null
                        ? batchSizeNullable
                        : 20; // @Performance What should this number be?

    HttpContext[] contexts = new HttpContext[batchSize];
    {
      @SuppressWarnings("unchecked")
      Future<HttpContext>[] contextFutures = new Future[batchSize];
      logger.info("Sending context requests... (x{})", batchSize);
      for (int i = 0; i < batchSize; i++) {
        contextFutures[i] = getContextAsync();
      }
      logger.info("Collecting context requests... (x{})", batchSize);
      for (int i = 0; i < batchSize; i++) {
        try {
          contexts[i] = contextFutures[i].get();
        } catch (ExecutionException | InterruptedException e) {
          throw new RuntimeException("Failed to get HttpContext.", e);
        }
        if (contexts[i] == null)
          throw new RuntimeException("Failed to get HttpContext.");
      }
    }

    logger.info("Collected context requests... (x{})", batchSize);

    return StreamSupport
        .stream(new SimpleBatchedFutureEngine<>(
                    subjectCodes, batchSize,
                    (subjectCode,
                     idx) -> queryCatalog(term, subjectCode, contexts[idx]))
                    .spliterator(),
                false)
        .filter(i -> i != null);
  }

  private static Future<CatalogQueryData>
  queryCatalog(Term term, SubjectCode subjectCode, HttpContext context) {
    logger.debug("querying catalog for term=" + term +
                 " and subject=" + subjectCode + "...");

    String params = String.format(
        "CSRFToken=%s&term=%d&acad_group=%s&subject=%s", context.csrfToken,
        term.getId(), subjectCode.school, subjectCode.getAbbrev());

    logger.debug("Params are {}.", params);
    Request request =
        new RequestBuilder()
            .setUri(DATA_URI)
            .setRequestTimeout(60000)
            .setHeader("Referer", ROOT_URL_STRING + "/" + term.getId())
            .setHeader("Referrer", "${ROOT_URL}/${term.id}")
            .setHeader("Host", "m.albert.nyu.edu")
            .setHeader("Accept-Language", "en-US,en;q=0.5")
            .setHeader("Accept-Encoding", "gzip, deflate, br")
            .setHeader("Content-Type",
                       "application/x-www-form-urlencoded; charset=UTF-8")
            .setHeader("X-Requested-With", "XMLHttpRequest")
            // .header("Content-Length", "129")
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
            logger.error("Error (subjectCode={}): {}", subjectCode,
                         throwable.getMessage());
            return null;
          }

          if (resp.getResponseBody().equals(
                  "No classes found matching your criteria.")) {
            logger.warn(
                "No classes found matching criteria school={}, subject={}",
                subjectCode.school, subjectCode.getAbbrev());
            return null;
          } else {
            return new CatalogQueryData(subjectCode, resp.getResponseBody());
          }
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
                  .filter(cookie -> cookie.name().equals("CSRFCookie"))
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
