package scraping;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import nyu.SubjectCode;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.models.CatalogQueryData;

public final class QueryCatalog {

  private static Logger logger =
      LoggerFactory.getLogger("scraping.QueryCatalog");
  private static final String ROOT_URL_STRING =
      "https://m.albert.nyu.edu/app/catalog/classSearch";
  private static final URI ROOT_URI;
  private static final URI DATA_URI;
  private static final HttpClient client = HttpClient.newHttpClient();

  static {
    try {
      ROOT_URI = new URI(ROOT_URL_STRING);
      DATA_URI = new URI("https://m.albert.nyu.edu/app/catalog/getClassSearch");
    } catch (URISyntaxException e) {
      throw new RuntimeException();
    }
  }

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
  queryCatalog(Term term, List<SubjectCode> subjectCodes,
               Integer batchSizeNullable) {
    if (subjectCodes.isEmpty())
      throw new IllegalArgumentException(
          "Need to provide at least one subject!");
    if (subjectCodes.size() > 1) {
      logger.debug("querying catalog for term={} with multiple subjects...",
                   term);
    } else {
      logger.debug("querying catalog for term={} and subject={}", term,
                   subjectCodes.get(0));
    }

    int batchSize =
        batchSizeNullable != null
            ? batchSizeNullable
            : max(5, min(subjectCodes.size() / 5,
                         20)); // @Performance What should this number be?

    HttpContext[] contexts = new HttpContext[batchSize];
    {
      @SuppressWarnings("unchecked")
      Future<HttpContext>[] contextFutures = new Future[batchSize];
      for (int i = 0; i < batchSize; i++) {
        contextFutures[i] = getContextAsync();
      }
      for (int i = 0; i < batchSize; i++) {
        try {
          contexts[i] = contextFutures[i].get();
        } catch (ExecutionException | InterruptedException e) {
          e.printStackTrace();
          contexts[i] = null;
        }
        if (contexts[i] == null)
          throw new RuntimeException("Failed to get HttpContext.");
      }
    }

    return StreamSupport
        .stream(new SimpleBatchedFutureEngine<SubjectCode, CatalogQueryData>(
                    subjectCodes, batchSize,
                    (subjectCode, idx) -> {
                      return queryCatalog(term, subjectCode, contexts[idx]);
                    })
                    .spliterator(),
                false)
        .filter(i -> i != null);
  }

  private static Future<CatalogQueryData>
  queryCatalog(Term term, SubjectCode subjectCode, HttpContext context) {
    logger.info("querying catalog for term=" + term +
                " and subject=" + subjectCode + "...");

    String params = String.format(
        "CSRFToken=%s&term=%d&acad_group=%s&subject=%s", context.csrfToken,
        term.getId(), subjectCode.school, subjectCode.getAbbrev());

    logger.debug("Params are {}.", params);

    HttpRequest request =
        HttpRequest.newBuilder(DATA_URI)
            .header("Referer", ROOT_URL_STRING + "/" + term.getId())
            .header("Referrer", "${ROOT_URL}/${term.id}")
            .header("Host", "m.albert.nyu.edu")
            .header("Accept-Language", "en-US,en;q=0.5")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8")
            .header("X-Requested-With", "XMLHttpRequest")
            // .header("Content-Length", "129")
            .header("Origin", "https://m.albert.nyu.edu")
            .header("DNT", "1")
            .header("Connection", "keep-alive")
            .header("Referer",
                    "https://m.albert.nyu.edu/app/catalog/classSearch")
            .header("Cookie", context.cookies.stream()
                                  .map(it -> it.toString())
                                  .collect(Collectors.joining(";")))
            .POST(HttpRequest.BodyPublishers.ofString(params))
            .build();

    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .handleAsync((resp, throwable) -> {
          if (resp.body().equals("No classes found matching your criteria.")) {
            logger.warn(
                "No classes found matching criteria school={}, subject={}",
                subjectCode.school, subjectCode.getAbbrev());
            return null;
          } else {
            return new CatalogQueryData(subjectCode, resp.body());
          }
        });
  }

  private static Future<HttpContext> getContextAsync() {
    logger.info("Getting CSRF token...");
    HttpRequest request = HttpRequest.newBuilder(ROOT_URI)
                              .timeout(Duration.ofSeconds(10))
                              .GET()
                              .build();

    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .handleAsync((resp, throwable) -> {
          List<HttpCookie> cookies =
              resp.headers()
                  .map()
                  .getOrDefault("Set-Cookie", new ArrayList<>())
                  .stream()
                  .flatMap(cookieList -> HttpCookie.parse(cookieList).stream())
                  .collect(Collectors.toList());
          HttpCookie csrfCookie =
              cookies.stream()
                  .filter(cookie -> cookie.getName().equals("CSRFCookie"))
                  .findAny()
                  .orElse(null);
          if (csrfCookie == null) {
            logger.error("Couldn't find cookie with name=CSRFCookie");
            return null;
          }
          logger.info("Retrieved CSRF token `{}`", csrfCookie.getValue());
          return new HttpContext(csrfCookie.getValue(), cookies);
        });
  }

  private static class HttpContext {
    final String csrfToken;
    final List<HttpCookie> cookies;

    HttpContext(String tok, List<HttpCookie> cookies) {
      this.csrfToken = tok;
      this.cookies = cookies;
    }
  }
}
