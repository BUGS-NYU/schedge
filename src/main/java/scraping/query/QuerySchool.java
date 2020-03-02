package scraping.query;

import nyu.Term;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public final class QuerySchool {

  private static final Logger logger =
      LoggerFactory.getLogger("scraping.query.QuerySchool");
  private static final Uri ROOT_URI =
          Uri.create("https://m.albert.nyu.edu/app/catalog/classSearch");

  public static String querySchool(Term term) {
    logger.info("querying school for term={}", term);

    Request request = new RequestBuilder()
            .setUri(ROOT_URI)
                              .setRequestTimeout(60000)
                              .build();

    try {
      return GetClient.getClient().executeRequest(request).get().getResponseBody();
    } catch (InterruptedException | ExecutionException e) {
      logger.error("Error (term=" + term + "): " + e.getMessage());
      return null;
    }
  }
}
