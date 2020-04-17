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
  private static final String SHOPPING_CART_ADD_DATA_URL_STRING =
      "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD/";
  
  private static final String SHOPPING_CART_REMOVE_DATA_URL_STRING =
          "https://m.albert.nyu.edu/app/student/enrollmentcart/enroll/NYUNV/UGRD/";


  private static final Uri LOGIN_ROOT_URI = Uri.create(LOGIN_ROOT_URL_STRING);
  private static final Uri LOGIN_DATA_URI = Uri.create(LOGIN_URI_STRING);

  public static List<Cookie> getLoginSession(String username, String password, Context.HttpContext context) {

  }
  

  public static List<Cookie> addToCart(String username, String password,
                                       Term term, int registrationNumber,
                                       Context.HttpContext context) {
    // Make request to the server for login info and session tokens
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
    //Retrive the session tokens and cookies
    List<Cookie> cookies =
        response.getHeaders()
            .getAll("Set-Cookie")
            .stream()
            .map(cookie -> ClientCookieDecoder.LAX.decode(cookie))
            .collect(Collectors.toList());
    cookies.addAll(context.cookies);


    /**
     * Make the request given session token and shopping cart
     * "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD/1204/7669";
     */
    Request request2 =
        new RequestBuilder()
            .setUri(Uri.create(SHOPPING_CART_ADD_DATA_URL_STRING +
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
          System.out.println("Hello?");
          System.out.println(response2.getStatusCode());
          return null;
        }));
    return null;
  }

  public static List<Cookie> removeFromCart(String username, String password,
                                       Term term, int registrationNumber,
                                       Context.HttpContext context) {
//    https://m.albert.nyu.edu/app/student/enrollmentcart/enroll/NYUNV/UGRD/1204
//    institution=NYUNV&acad_career=UGRD&strm=1204&confirm_enrollment=N&CSRFToken=c8e0cd2ed1e3ee27661c75959e99629b&delete=&selected%5B%5D=7669
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
    "https://m.albert.nyu.edu/app/student/enrollmentcart/enroll/NYUNV/UGRD/1204"
    "Can change to more by adding more selected field
     */
    String deleteForm = String.format(
            "institution=NYUNV&acad_career=UGRD&strm=%s&confirm_enrollment=N&CSRFToken=%s&delete=&selected[]=%s",
            term.getId(), context.csrfToken, registrationNumber
    );
    System.out.println(deleteForm);
    System.out.println(cookies.toString());
    Request request2 =
            new RequestBuilder()
                    .setUri(Uri.create("https://m.albert.nyu.edu/app/student/enrollmentcart/enroll/NYUNV/UGRD/1204"))
                    .setRequestTimeout(60000)
                    .setHeader(
                            "Referer",
                            "https://m.albert.nyu.edu/app/student/enrollmentcart/cart/NYUNV/UGRD/1204")
                    .setHeader("Host", "m.albert.nyu.edu")
                    .setHeader("Accept-Language", "en-US,en;q=0.5")
                    .setHeader("Accept-Encoding", "gzip, deflate, br")
                    .setHeader("Content-Type",
                            "application/x-www-form-urlencoded; charset=UTF-8")
                    .setHeader("X-Requested-With", "XMLHttpRequest")
                    .setHeader("Origin", "https://m.albert.nyu.edu")
                    .setHeader("DNT", "1")
                    .setHeader("Connection", "keep-alive")
                    .setHeader("Cookie", cookies.stream()
                            .map(it -> it.name() + '=' + it.value())
                            .collect(Collectors.joining("; ")))
                    .setMethod("POST")
                    .setBody(deleteForm)
                    .build();
    GetClient.getClient()
            .executeRequest(request2)
            .toCompletableFuture()
            .handleAsync(((response2, throwable) -> {
              System.out.println(response2.getResponseBody());
              System.out.println(response2.getHeaders());
              System.out.println(response2.getStatusCode());
              return null;
            }));
    return null;
  }


}
