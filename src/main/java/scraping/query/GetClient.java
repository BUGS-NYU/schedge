package scraping.query;

import static utils.TryCatch.*;

import io.netty.handler.codec.http.cookie.Cookie;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.cookie.CookieStore;
import org.asynchttpclient.uri.Uri;

public final class GetClient {
  private static volatile AsyncHttpClient client;

  public static synchronized AsyncHttpClient getClient() {
    if (client == null) {
      client = new DefaultAsyncHttpClient(
          new DefaultAsyncHttpClientConfig.Builder()
              .setCookieStore(new BlackholeCookieStore())
              .build());
    }

    return client;
  }

  public static void close() {
    if (client != null)
      tcPass(client::close);
    client = null;
  }

  private static class BlackholeCookieStore implements CookieStore {

    private final static BlackholeCookieStore BLACK_HOLE =
        new BlackholeCookieStore();

    @Override
    public void add(Uri uri, Cookie cookie) {}

    @Override
    public List<Cookie> get(Uri uri) {
      return Collections.emptyList();
    }

    @Override
    public List<Cookie> getAll() {
      return Collections.emptyList();
    }

    @Override
    public boolean remove(Predicate<Cookie> predicate) {
      return false;
    }

    @Override
    public boolean clear() {
      return true;
    }
  };
}
