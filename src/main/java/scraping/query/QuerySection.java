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

    // @TODO CSRF tokens seem to now be necessary to get responses from this API
    //                                  - Albert Liu, Jan 30, 2022 Sun 16:49 EST
    logger.debug("Querying section in term=" + term +
                 " with registrationNumber=" + registrationNumber);

    Request request = new RequestBuilder()
                          .setUri(Uri.create(DATA_URL_STRING + term.getId() +
                                             "/" + registrationNumber))
                          .setRequestTimeout(10000)
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
