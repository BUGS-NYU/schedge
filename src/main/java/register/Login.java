package register;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import nyu.Term;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.uri.Uri;
import scraping.query.GetClient;

public class Login {
  private static final String LOGIN_ROOT_URL_STRING =
      "https://m.albert.nyu.edu/app/profile/login";
  private static final String LOGIN_URI_STRING =
      "https://m.albert.nyu.edu/app/profile/logintoapp";
  // We will do undergrad for now/ Remember to add term and registration number
  // at the end
  private static final String SHOPPING_CART_DATA_URL_STRING =
      "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD";
  private static final Uri LOGIN_ROOT_URI = Uri.create(LOGIN_ROOT_URL_STRING);
  private static final Uri LOGIN_DATA_URI = Uri.create(LOGIN_URI_STRING);

  public static List<Cookie> addToCart(String username, String password,
                                       Term term, int registrationNumber,
                                       Context.HttpContext context) {
    String params = String.format(
        "CSRFToken=%s&username=%s&password=%s&loginAction=&institution=NYUNV",
        context.csrfToken, username, password);
    Request request =
        new RequestBuilder()
            .setUri(LOGIN_DATA_URI)
            .setRequestTimeout(60000)
            .setHeader("Referer", LOGIN_ROOT_URI)
            .setHeader("Host", "m.albert.nyu.edu")
            .setHeader("Accept-Language", "en-US,en;q=0.5")
            .setHeader("Accept-Encoding", "gzip, deflate, br")
            .setHeader("Content-Type",
                       "application/x-www-form-urlencoded; charset=UTF-8")
            .setHeader("X-Requested-With", "XMLHttpRequest")
            .setHeader("Origin", "https://m.albert.nyu.edu")
            .setHeader("DNT", "1")
            .setHeader("Connection", "keep-alive")
            .setHeader("Cookie", context.cookies.stream()
                                     .map(it -> it.name() + '=' + it.value())
                                     .collect(Collectors.joining("; ")))
            .setMethod("POST")
            .setBody(params)
            .build();
    Response response = null;
    try {
      response = GetClient.getClient()
                     .executeRequest(request)
                     .toCompletableFuture()
                     .get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    List<Cookie> cookies =
        response.getHeaders()
            .getAll("Set-Cookie")
            .stream()
            .map(cookie -> ClientCookieDecoder.LAX.decode(cookie))
            .collect(Collectors.toList());
    cookies.addAll(context.cookies);
    /*
    "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD/1204/7669";
     */
    Request request2 =
        new RequestBuilder()
            .setUri(Uri.create(SHOPPING_CART_DATA_URL_STRING + "/" +
                               term.getId() + "/" + registrationNumber))
            .setRequestTimeout(60000)
            .setHeader(
                "Referer",
                "https://m.albert.nyu.edu/app/student/enrollmentcart/cart")
            .setHeader("Host", "m.albert.nyu.edu")
            .setHeader("Accept-Language", "en-US,en;q=0.5")
            .setHeader("Accept-Encoding", "gzip, deflate, br")
            .setHeader("Content-Type",
                       "application/x-www-form-urlencoded; charset=UTF-8")
            .setHeader("X-Requested-With", "XMLHttpRequest")
            .setHeader("Content-Length", "129")
            .setHeader("Origin", "https://m.albert.nyu.edu")
            .setHeader("DNT", "1")
            .setHeader("Connection", "keep-alive")
            .setHeader("Cookie", cookies.stream()
                                     .map(it -> it.name() + '=' + it.value())
                                     .collect(Collectors.joining("; ")))
            .setMethod("GET")
            .build();
    GetClient.getClient()
        .executeRequest(request2)
        .toCompletableFuture()
        .handleAsync(((response2, throwable) -> {
          System.out.println(response2.getStatusCode());
          return null;
        }));
    return null;
  }
}
