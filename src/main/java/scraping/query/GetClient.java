package scraping.query;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import java.io.IOException;

public final class GetClient {
  private static AsyncHttpClient client;

  public static AsyncHttpClient getClient() {
    if (client == null)
      client = new DefaultAsyncHttpClient(
          new DefaultAsyncHttpClientConfig.Builder()
              .setCookieStore(BlackholeCookieStore.BLACK_HOLE)
              .build());
    return client;
  }

  public static void close() {
    if (client != null) {
      try {
        client.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    client = null;
  }
}
