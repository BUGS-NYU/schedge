package scraping.query;

import static scraping.query.GetClient.*;
import static utils.TryCatch.*;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import java.util.*;
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
  private static final String ROOT_URL =
      "https://m.albert.nyu.edu/app/catalog/classSearch";
  private static final Uri DATA_URI =
      Uri.create("https://m.albert.nyu.edu/app/catalog/getClassSearch");

  public static Stream<CatalogQueryData>
  queryCatalog(Term term, Iterable<SubjectCode> subjects, int batchSize) {
    logger.debug("querying catalog for term={} with multiple subjects...",
                 term);

    Ctx[] contexts = new Ctx[batchSize];
    {
      @SuppressWarnings("unchecked")
      Future<Ctx>[] contextFutures = new Future[batchSize];
      logger.info("Sending context requests... (x{})", batchSize);

      for (int i = 0; i < batchSize; i++) {
        contextFutures[i] = getCtx(ROOT_URL);
      }

      logger.info("Collecting context requests... (x{})", batchSize);

      TryCatch tc = tcNew(logger, "Failed to get context.");
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
  queryCatalog(Term term, SubjectCode subject, Ctx context) {
    logger.debug("querying catalog for term=" + term +
                 " and subject=" + subject + "...");

    String csrf = context.csrfToken;
    int id = term.getId();
    String code = subject.code;
    String school = subject.schoolCode;

    String format = "CSRFToken=%s&term=%d&acad_group=%s&subject=%s";
    String params = String.format(format, csrf, id, school, code);
    logger.debug("Params are {}.", params);

    // @Note not sure why all these headers are necessary, but NYU's API will
    // fast-fail if these aren't present. Potentially some kind of anti-scraping
    // protection measure.
    //                              - Albert Liu, Jan 30, 2022 Sun 17:12 EST
    Request request =
        new RequestBuilder()
            .setUri(DATA_URI)
            .setRequestTimeout(10000)
            .setHeader("Referer", ROOT_URL + "/" + term.getId())
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
            .setHeader("Cookie", context.cookies)
            .setMethod("POST")
            .setBody(params)
            .build();

    return GetClient.send(request, (resp, throwable) -> {
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
}
