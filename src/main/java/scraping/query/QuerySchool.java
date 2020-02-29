package scraping.query;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import nyu.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QuerySchool {

  private static final Logger logger =
      LoggerFactory.getLogger("scraping.query.QuerySchool");
  private static final URI ROOT_URI =
      URI.create("https://m.albert.nyu.edu/app/catalog/classSearch");

  public static String querySchool(Term term) {
    logger.info("querying school for term={}", term);

    HttpRequest request = HttpRequest.newBuilder(ROOT_URI)
                              .timeout(Duration.ofSeconds(60))
                              .build();

    try {
      return GetClient.getClient()
          .send(request, HttpResponse.BodyHandlers.ofString())
          .body();
    } catch (InterruptedException | IOException e) {
      logger.error("Error (term=" + term + "): " + e.getMessage());
      return null;
    }
  }
}
