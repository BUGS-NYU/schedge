package scraping.query;

import static utils.TryCatch.*;

import java.io.IOException;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

public final class GetClient {
  private static volatile AsyncHttpClient client;

  public static AsyncHttpClient getClient() {
    if (client == null)
      client = new DefaultAsyncHttpClient(
          new DefaultAsyncHttpClientConfig.Builder()
              .setCookieStore(BlackholeCookieStore.BLACK_HOLE)
              .build());
    return client;
  }

  public static void close() {
    if (client != null)
      tcPass(client::close);
    client = null;
  }
}
