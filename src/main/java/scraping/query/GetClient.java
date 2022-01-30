package scraping.query;

import static utils.TryCatch.*;

import io.netty.handler.codec.http.cookie.Cookie;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.asynchttpclient.cookie.CookieStore;
import org.asynchttpclient.uri.Uri;

public final class GetClient {
  private static volatile AsyncHttpClient client;

  // Utility function for sending requests.
  public static Response sendSync(Request request) {
    Throwable[] eRef = new Throwable[1];
    try {
      Future<Response> fut = send(request, (resp, e) -> {
        if (resp == null) {
          eRef[0] = e;
        }

        return resp;
      });

      Response resp = fut.get();
      if (resp != null)
        return resp;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    throw new RuntimeException(eRef[0]);
  }

  // Utility function for sending requests.
  public static synchronized<E> Future<E>
  send(Request request,
       BiFunction<? super Response, Throwable, ? extends E> fn) {
    if (client == null) {
      client = new DefaultAsyncHttpClient(
          new DefaultAsyncHttpClientConfig.Builder()
              .setCookieStore(new BlackholeCookieStore())
              .build());
    }

    return client.executeRequest(request).toCompletableFuture().handleAsync(fn);
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
