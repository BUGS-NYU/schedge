package register;

import static register.GetLogin.getLoginSession;

import io.netty.handler.codec.http.cookie.Cookie;
import java.util.List;
import java.util.stream.Collectors;
import nyu.Term;
import nyu.User;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import scraping.query.GetClient;

public class EnrollCourses {
  private static final String DATA_URL_STRING =
      "https://m.albert.nyu.edu/app/student/enrollmentcart/enroll/NYUNV/UGRD/";
  private static final String ENROLL_ROOT_URL_STRING =
      "https://m.albert.nyu.edu/app/student/enrollmentcart/cart";
  public static String setUpForm(Term term, List<Integer> registrationNumbers,
                                 String action, Context.HttpContext context) {
    // adding &selected[]=%s for courses in the shopping cart
    String form = String.format(
        "institution=NYUNV&acad_career=UGRD&strm=%s&confirm_enrollment=N&CSRFToken=%s",
        term.getId(), context.csrfToken);
    form += action;
    for (Integer regNumber : registrationNumbers) {
      form += "&selected[]=" + regNumber;
    }
    return form;
  }

  public static void enrollCourse(User user, Term term,
                                  List<Integer> registrationNumbers,
                                  Context.HttpContext context) {
    List<Cookie> cookies = getLoginSession(user, context);
    String enrollForm =
        setUpForm(term, registrationNumbers, "&enroll=", context);
    System.out.println(enrollForm);
    Request request =
        new RequestBuilder()
            .setUri(Uri.create(DATA_URL_STRING + term.getId()))
            .setRequestTimeout(60000)
            .setHeader("Referer", ENROLL_ROOT_URL_STRING)
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
            .setBody(enrollForm)
            .build();
    GetClient.getClient()
        .executeRequest(request)
        .toCompletableFuture()
        .handleAsync(((resp, throwable) -> {
          return null; // adding handler here
        }));
  }

  public static void removeFromCart(User user, Term term,
                                    List<Integer> registrationNumbers,
                                    Context.HttpContext context) {
    List<Cookie> cookies = getLoginSession(user, context);
    /*
    "https://m.albert.nyu.edu/app/student/enrollmentcart/enroll/NYUNV/UGRD/1204"
    "Can change to more by adding more selected field
     */
    String deleteForm =
        setUpForm(term, registrationNumbers, "&delete=", context);
    Request request =
        new RequestBuilder()
            .setUri(Uri.create(DATA_URL_STRING + term.getId()))
            .setRequestTimeout(60000)
            .setHeader("Referer", DATA_URL_STRING + term.getId())
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
        .executeRequest(request)
        .toCompletableFuture()
        .handleAsync(((resp, throwable) -> null));
  }
}
