package scraping;

import static org.asynchttpclient.Dsl.*;

import io.netty.handler.codec.http.cookie.Cookie;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
// import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.asynchttpclient.*;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraping.models.Semester;
import scraping.models.SubjectCode;
import scraping.models.Term;
import services.GetClient;

/*
    @Todo: Add annotation for parameter. Fix the method to parse
    @Help: Add annotations, comments to code
 */
public class QueryCatalog {
  private static Logger logger = LoggerFactory.getLogger("scraping.catalog");
  private static String ROOT_URl =
      "https://m.albert.nyu.edu/app/catalog/classSearch";
  private static String DATA_URL =
      "https://m.albert.nyu.edu/app/catalog/getClassSearch";

  public static String queryCatalog(Term term, SubjectCode subjectCode)
      throws ExecutionException, InterruptedException, IOException {
    String result = queryCatalog(term, subjectCode, getHttpContext()).get();
    return result;
  }

  public static Iterator<String> queryCatalog(Term term,
                                            List<SubjectCode> subjectCodes,
                                            Integer batchSizeNullable) {
    if (subjectCodes.size() > 1) {
      logger.info("querying catalog for term = " + term.toString() +
                  " with mutiple subjects");
    }
    int batchSize =
        batchSizeNullable != null
            ? batchSizeNullable
            : Math.max(5, Math.min(subjectCodes.size() / 5, 20));


    List<HttpContext> contexts = IntStream.range(0, batchSize).mapToObj(idx -> {
        try {
            return getContextAsync();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }).collect(Collectors.toList()).stream().map(obj -> {
        try {
            return obj.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }).collect(Collectors.toList());


    return new SimpleBatchedFutureEngine<>(
        subjectCodes, batchSize,
        ((subjectCode,
          integer) -> queryCatalog(term, subjectCode, contexts.get(integer))));
  }

  private static Future<String> queryCatalog(Term term, SubjectCode subjectCode,
                                             HttpContext httpContext) {
    logger.info("querying catalog for term= " + term.getId() +
                " and subject= " + subjectCode.toString());

    Map<String, String> map = new LinkedHashMap<>();
    map.put("CSRFToken", httpContext.csrfToken);
    map.put("term", String.valueOf(term.getId()));
    map.put("acad_group", subjectCode.getSchool());
    map.put("subject", subjectCode.toString());
    String values = map.entrySet()
                        .stream()
                        .map(value -> value.getKey() + "=" + value.getValue())
                        .collect(Collectors.joining("&"));

    AsyncHttpClient asyncHttpClient = GetClient.getClient();

    return asyncHttpClient.preparePost(DATA_URL + "/" + term.getId())
        .setHeader("Referrer", ROOT_URl + "/" + term.getId())
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
        .setHeader("Cookie",
                   httpContext.cookies.stream()
                       .map(value -> value.name() + "=" + value.value())
                       .collect(Collectors.joining(";")))
        .setBody(values)
        .execute()
        .toCompletableFuture()
        .exceptionally(t -> null)
        .thenApply(response -> {
          String responseText = response.getResponseBody();
          if (responseText.equals(
                  "No classes found matching criteria school")) {
            logger.warn("No classes found matching criteria school = " +
                        subjectCode.getSchool() +
                        " & subject = " + subjectCode.getSubject());
            return null;
          }
          return responseText;
        });
  }

  private static HttpContext getHttpContext()
      throws ExecutionException, InterruptedException, IOException {
    return getContextAsync().get();
  }

  private static Future<HttpContext> getContextAsync() throws IOException {
    logger.info("Getting CSRF token ....");
      AsyncHttpClient asyncHttpClient = GetClient.getClient();
      return asyncHttpClient.prepareGet(ROOT_URl)
          .execute()
          .toCompletableFuture()
          .exceptionally(t -> {
              return null;
          })
          .thenApply(response -> {
            List<Cookie> cookies = response.getCookies();
            Cookie tokenCookie =
                cookies.stream()
                    .filter(value -> value.name().equals("CSRFCookie"))
                    .findAny()
                    .orElse(null);
            if (tokenCookie == null) {
                logger.info("Couldn't find cookie with name = CSRFCookie");
                return null;
            }
            return new HttpContext(tokenCookie.value(), cookies);
          });
  }

  private static class HttpContext {
    private String csrfToken;
    private List<Cookie> cookies;

    public HttpContext(String csrfToken, List<Cookie> cookies) {
      this.csrfToken = csrfToken;
      this.cookies = cookies;
    }

    public String toString() {
      return "csrfToken = " + csrfToken + " & cookies = " + cookies;
    }
  }
}
