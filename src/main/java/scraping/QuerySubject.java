package scraping;

import java.io.IOException;
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
public class QuerySubject {
  private static Logger logger = LoggerFactory.getLogger("query.subjects");
  private static String ROOT_URl =
      "https://m.albert.nyu.edu/app/catalog/classSearch";

  public static String querySubject(Term term) {
    logger.info("querying school for term =" + term.getId());
    String output = "";
    try {
      output = querySubjectAsync(term).get();
    } catch (InterruptedException | ExecutionException e) {
      System.out.println("No classes found matching your term");
    }
    return output;
  }

  public static Future<String> querySubjectAsync(Term term) {
    CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
    CompletableFuture<String> future = new CompletableFuture<>();
    try {
      client.start();
      HttpGet request = new HttpGet(ROOT_URl + "/" + term.getId());
      HttpResponse response = client.execute(request, null).get();
      future.complete(EntityUtils.toString(response.getEntity()));
      client.close();
    } catch (InterruptedException | ExecutionException | IOException e) {
      e.printStackTrace();
    }
    return future;
  }
}
