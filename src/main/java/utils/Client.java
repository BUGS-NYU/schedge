package utils;

import static utils.TryCatch.*;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.Collectors;
import org.asynchttpclient.*;
import org.asynchttpclient.cookie.CookieStore;
import org.asynchttpclient.uri.Uri;
import org.slf4j.*;

public final class Client {
  private static Logger logger =
      LoggerFactory.getLogger("utils.Client");

  private static volatile AsyncHttpClient client;

  public static class Ctx {
    public String csrfToken;
    public String cookies;
  }

  public static CompletableFuture<Ctx> getCtx(String uri) {
    Request request = new RequestBuilder()
                          .setUri(Uri.create(uri))
                          .setRequestTimeout(10000)
                          .setMethod("GET")
                          .build();

    return send(request, (resp, e) -> {
      if (resp == null) {
        logger.error("Failed to get context: uri={}", uri, e);

        return null;
      }

      Map<String, String> cookies = cookiesFrom(resp);
      String csrf = cookies.get("CSRFCookie");
      if (csrf == null) {
        logger.error("Missing cookie with name=CSRFCookie: cookies={}",
                     cookies);

        return null;
      }

      String cookieString = cookies.entrySet()
                                .stream()
                                .map(it -> it.getKey() + '=' + it.getValue())
                                .collect(Collectors.joining("; "));

      Ctx context = new Ctx();
      context.csrfToken = csrf;
      context.cookies = cookieString;

      return context;
    });
  }

  public static Map<String, String> cookiesFrom(Response resp) {
    HashMap<String, String> cookies = new HashMap<>();

    for (String header : resp.getHeaders().getAll("Set-Cookie")) {
      Cookie cookie = ClientCookieDecoder.STRICT.decode(header);
      cookies.put(cookie.name(), cookie.value());
    }

    return cookies;
  }

  // Utility function for sending requests. Mostly useful during
  // development, before it's known what exact behavior is best.
  public static Response sendSync(Request request) {
    Throwable[] eRef = new Throwable[1];
    try {
      Future<Response> fut = send(request, (resp, e) -> {
        eRef[0] = e;

        return resp;
      });

      Response resp = fut.get();
      if (resp != null)
        return resp;

      throw eRef[0];
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  // Utility function for sending requests.
  public static synchronized<E> CompletableFuture<E>
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
