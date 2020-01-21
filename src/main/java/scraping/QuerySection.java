package scraping;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import models.Term;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
    @Todo: Add annotation for parameter. Fix the method to parse
    @Help: Add annotations, comments to code
 */
public class QuerySection {
  private static Logger logger =
      LoggerFactory.getLogger("scraping.query_section");
  private static String DATA_URL =
      "https://m.albert.nyu.edu/app/catalog/classsection/NYUNV/";

  public static String querySection(Term term, Integer registrationNumber)
      throws ExecutionException, InterruptedException {
    return querySectionAsync(term, registrationNumber).get();
  }

  // @Todo: Test this code
  public static Vector<String> querySection(Term term,
                                            List<Integer> registrationNumbers,
                                            Integer batchSizeNullable) {
    if (registrationNumbers.size() > 1) {
      logger.info("Querying multiple sections....");
    }
    Integer batchSize =
        batchSizeNullable != null
            ? batchSizeNullable
            : Math.max(5, Math.min(registrationNumbers.size() / 5, 20));
    Vector<String> outputs = new Vector<>();

    new SimpleBatchedFutureEngine<Integer, String>(
        registrationNumbers, batchSize,
        (integer, integer2) -> {
          CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
          client.start();
          CompletableFuture future = new CompletableFuture();
          HttpGet getRequest =
              new HttpGet(DATA_URL + term.getId() + "/" + integer);
          try {
            future.complete(EntityUtils.toString(
                client.execute(getRequest, null).get().getEntity()));
            client.close();
          } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
          }
          return future;
        })
        .forEachRemaining(outputs::add);
    return outputs;
  }

  private static Future<String> querySectionAsync(Term term,
                                                  Integer registrationNumber) {
    logger.info("Querying section in term = " + term.toString() +
                " with registrationNumber = " + registrationNumber);
    if (registrationNumber <= 0) {
      throw new IllegalArgumentException(
          "Registration numbers aren't negative");
    }

    CompletableFuture<String> future = new CompletableFuture<>();
    CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();

    try {
      client.start();
      HttpGet request =
          new HttpGet(DATA_URL + term.getId() + "/" + registrationNumber);
      Future<HttpResponse> response = client.execute(request, null);
      future.complete(EntityUtils.toString(response.get().getEntity()));
      client.close();
    } catch (InterruptedException | ExecutionException | IOException e) {
      e.printStackTrace();
    }
    return future;
  }
}
