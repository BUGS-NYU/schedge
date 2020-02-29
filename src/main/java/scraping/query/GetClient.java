package scraping.query;

import java.net.http.HttpClient;

public final class GetClient {
  private static HttpClient client;

  public static HttpClient getClient() {
    if (client == null)
      client = HttpClient.newHttpClient();
    return client;
  }
}
