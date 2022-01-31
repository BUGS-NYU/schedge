package register;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import java.util.List;
import java.util.stream.Collectors;
import types.User;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.uri.Uri;
import utils.Client;

public class GetLogin {
  private static final String LOGIN_ROOT_URL_STRING =
      "https://m.albert.nyu.edu/app/profile/login";
  private static final String LOGIN_URI_STRING =
      "https://m.albert.nyu.edu/app/profile/logintoapp";

  private static final Uri LOGIN_ROOT_URI = Uri.create(LOGIN_ROOT_URL_STRING);
  private static final Uri LOGIN_DATA_URI = Uri.create(LOGIN_URI_STRING);

  public static Context.HttpContext
  getLoginSession(User user, Context.HttpContext context) {
    String params = String.format(
        "CSRFToken=%s&username=%s&password=%s&loginAction=&institution=NYUNV",
        context.csrfToken, user.username, user.password);
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

    Response response = Client.sendSync(request);

    // Retrive the session tokens and cookies
    List<Cookie> cookies =
        response.getHeaders()
            .getAll("Set-Cookie")
            .stream()
            .map(cookie -> ClientCookieDecoder.LAX.decode(cookie))
            .collect(Collectors.toList());
    Cookie csrfCookie =
        cookies.stream()
            .filter(cookie -> cookie.name().equals("CSRFCookie"))
            .findAny()
            .orElse(null);
    cookies.addAll(context.cookies);
    return new Context.HttpContext(csrfCookie.value(), cookies);
  }
}
