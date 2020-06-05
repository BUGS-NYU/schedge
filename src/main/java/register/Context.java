package register;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import nyu.Term;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import scraping.query.GetClient;

import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class Context {
  private final static String ROOT_URI =
      "https://m.albert.nyu.edu/app/student/enrollmentcart/enroll/NYUNV/UGRD";
  public static Future<HttpContext> getContextAsync(Term term) {
    Request request = new RequestBuilder()
                          .setUri(Uri.create(ROOT_URI + term.getId()))
                          .setMethod("GET")
                          .build();

    return GetClient.getClient()
        .executeRequest(request)
        .toCompletableFuture()
        .handleAsync((resp, throwable) -> {
          if (resp == null) {
            return null;
          }

          List<Cookie> cookies =
              resp.getHeaders()
                  .getAll("Set-Cookie")
                  .stream()
                  .map(cookie -> ClientCookieDecoder.STRICT.decode(cookie))
                  .collect(Collectors.toList());
          Cookie csrfCookie =
              cookies.stream()
                  .filter(cookie -> cookie.name().equals("CSRFCookie"))
                  .findAny()
                  .orElse(null);
          if (csrfCookie == null) {
            return null;
          }
          return new HttpContext(csrfCookie.value(), cookies);
        });
  }

  // @Todo: Move this to be a class on its own
  public static class HttpContext {
    final String csrfToken;
    final List<Cookie> cookies;

    HttpContext(String tok, List<Cookie> cookies) {
      this.csrfToken = tok;
      this.cookies = cookies;
    }
  }
}
