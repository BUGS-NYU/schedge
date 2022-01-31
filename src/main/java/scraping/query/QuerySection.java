package scraping.query;

import static utils.TryCatch.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import nyu.Term;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.SimpleBatchedFutureEngine;

public final class QuerySection {
  private static Logger logger =
      LoggerFactory.getLogger("scraping.query.QuerySection");

  private static final String ROOT_URL =
      "https://m.albert.nyu.edu/app/catalog/classSearch";
  private static String DATA_URL_STRING =
      "https://m.albert.nyu.edu/app/catalog/classsection/NYUNV/";

  public static String querySection(Term term, int registrationNumber) {
    String queryData = null;
    try {
      queryData = querySectionAsync(term, registrationNumber).get();
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(
          "No section found matching criteria registrationNumber=" +
              registrationNumber,
          e);
    }

    if (queryData == null)
      throw new RuntimeException(
          "No section found matching criteria registrationNumber=" +
          registrationNumber);

    return queryData;
  }

  public static Stream<String>
  querySections(Term term, List<Integer> registrationNumbers, int batchSize) {
    return StreamSupport
        .stream(new SimpleBatchedFutureEngine<>(
                    registrationNumbers.iterator(), batchSize,
                    (registrationNumber,
                     __) -> querySectionAsync(term, registrationNumber))
                    .spliterator(),
                false)
        .filter(i -> i != null);
  }

  public static Future<String> querySectionAsync(Term term,
                                                 int registrationNumber) {
    return querySectionAsync(term, registrationNumber, str -> str);
  }

  public static <T> Future<T> querySectionAsync(Term term,
                                                int registrationNumber,
                                                Function<String, T> transform) {
    if (registrationNumber < 0)
      throw new IllegalArgumentException(
          "Registration numbers aren't negative!");

    logger.debug("Querying section in term=" + term +
                 " with registrationNumber=" + registrationNumber);

    Request request =
        new RequestBuilder()
            .setUri(Uri.create(DATA_URL_STRING + term.getId() + "/" +
                               registrationNumber))
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
            .setRequestTimeout(20000)
            .setMethod("GET")
            .build();

    return GetClient.send(request, (resp, throwable) -> {
      if (resp == null) {
        logger.error("Querying section failed: term={}, registrationNumber={}",
                     term, registrationNumber, throwable);

        return null;
      }

      return transform.apply(resp.getResponseBody());
    });
  }
}
