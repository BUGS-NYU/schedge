package register;

import io.netty.handler.codec.http.cookie.Cookie;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import nyu.Term;
import nyu.User;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import scraping.query.GetClient;
import utils.SimpleBatchedFutureEngine;

import static register.Context.getContextAsync;

public class AddToCart {
  // We will do undergrad for now/ Remember to add term and registration number
  // at the end
  private static final String SHOPPING_CART_ADD_DATA_URL_STRING =
      "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD/";
  private static final String ROOT_URL_STRING =
      "https://m.albert.nyu.edu/app/student/enrollmentcart/cart";
  private static final String ADD_RELATED_DATA_URL_STRING =
          "https://m.albert.nyu.edu/app/student/enrollmentcart/addRelated";
  private static final String ADD_RELATED_ROOT_URL_STRING =
          "https://m.albert.nyu.edu/app/student/enrollmentcart/selectRelated/NYUNV/UGRD/";
  private static final String SELECT_OPTIONS_ROOT_URL_STRING =
          "https://m.albert.nyu.edu/app/student/enrollmentcart/selectOptions/NYUNV/UGRD/";
  private static final Uri ADD_OPTIONS_URI =
          Uri.create("https://m.albert.nyu.edu/app/student/enrollmentcart/addOptions");

  public static void
  addToCart(User user, Term term, List<Integer> registrationNumbers) {
    int size = registrationNumbers.size();
    Context.HttpContext[] contexts = new Context.HttpContext[size];
    {
      @SuppressWarnings("unchecked")
      Future<Context.HttpContext>[] contextFutures = new Future[size];
      for (int i = 0; i < size; i++) {
        contextFutures[i] = getContextAsync(term);
      }
      for (int i = 0; i < size; i++) {
        try {
          contexts[i] = contextFutures[i].get();
        } catch (ExecutionException | InterruptedException e) {
          throw new RuntimeException("Failed to get HttpContext.", e);
        }
        if (contexts[i] == null)
          throw new RuntimeException("Failed to get HttpContext.");
      }
    }

    SimpleBatchedFutureEngine engine = new SimpleBatchedFutureEngine<>(
            registrationNumbers, size,
            (regNumber, idx) -> addToCart(user, term, regNumber, contexts[idx])
    );

    engine.iterator();
  }

  public static Future<Void> addToCart(User user, Term term, int registrationNumber,
                               Context.HttpContext context) {
    // @TODO: Turn this into static if possible
    Context.HttpContext newContext = GetLogin.getLoginSession(user, context);

    /**
     * Make the request given session token and shopping cart
     * "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD/1204/7669";
     */
    Request request =
        new RequestBuilder()
            .setUri(Uri.create(SHOPPING_CART_ADD_DATA_URL_STRING +
                               term.getId() + "/" + registrationNumber))
            .setRequestTimeout(60000)
            .setHeader("Referer", ROOT_URL_STRING)
            .setHeader("Host", "m.albert.nyu.edu")
            .setHeader("Accept-Language", "en-US,en;q=0.5")
            .setHeader("Accept-Encoding", "gzip, deflate, br")
            .setHeader("Content-Type",
                       "application/x-www-form-urlencoded; charset=UTF-8")
            .setHeader("X-Requested-With", "XMLHttpRequest")
            .setHeader("Origin", "https://m.albert.nyu.edu")
            .setHeader("DNT", "1")
            .setHeader("Connection", "keep-alive")
            .setHeader("Cookie", newContext.cookies.stream()
                                     .map(it -> it.name() + '=' + it.value())
                                     .collect(Collectors.joining("; ")))
            .setMethod("GET")
            .build();
    return GetClient.getClient()
        .executeRequest(request)
        .toCompletableFuture()
        .handleAsync(((resp, throwable) -> {
          //if status code is 200 then there is an error
          return null;
        }));

//    return null;
  }

  public static Future<String> addRelated(User user, Term term, int registrationNumber, int sectionRelated,
                                         Context.HttpContext context) {
    // @TODO: Turn this into static if possible. Turn this into a context object
    Context.HttpContext newContext = GetLogin.getLoginSession(user, context);

    /**
     * Make the request given session token and shopping cart
     * "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD/1204/7669";
     */
    Request request =
            new RequestBuilder()
                    .setUri(Uri.create(SHOPPING_CART_ADD_DATA_URL_STRING +
                            term.getId() + "/" + registrationNumber))
                    .setRequestTimeout(60000)
                    .setHeader("Referer", ROOT_URL_STRING)
                    .setHeader("Host", "m.albert.nyu.edu")
                    .setHeader("Accept-Language", "en-US,en;q=0.5")
                    .setHeader("Accept-Encoding", "gzip, deflate, br")
                    .setHeader("Content-Type",
                            "application/x-www-form-urlencoded; charset=UTF-8")
                    .setHeader("X-Requested-With", "XMLHttpRequest")
                    .setHeader("Origin", "https://m.albert.nyu.edu")
                    .setHeader("DNT", "1")
                    .setHeader("Connection", "keep-alive")
                    .setHeader("Cookie", newContext.cookies.stream()
                            .map(it -> it.name() + '=' + it.value())
                            .collect(Collectors.joining("; ")))
                    .setMethod("GET")
                    .build();
    GetClient.getClient()
            .executeRequest(request)
            .toCompletableFuture()
            .handleAsync(((resp, throwable) -> { return null; }));

    String form = String.format(
            "institution=NYUNV&acad_career=UGRD&strm=%s&class_nbr=%s&CSRFToken=%s&component1=%s",
            term.getId(), registrationNumber, newContext.csrfToken, sectionRelated
    );

    /**
     * Make the request given session token and shopping cart
     * "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD/1204/7669";
     */
    Request sectionRequest =
            new RequestBuilder()
                    .setUri(Uri.create(ADD_RELATED_DATA_URL_STRING))
                    .setRequestTimeout(60000)
                    .setHeader("Referer", ADD_RELATED_ROOT_URL_STRING + term.getId() + "/" + registrationNumber)
                    .setHeader("Host", "m.albert.nyu.edu")
                    .setHeader("Accept-Language", "en-US,en;q=0.5")
                    .setHeader("Accept-Encoding", "gzip, deflate, br")
                    .setHeader("Content-Type",
                            "application/x-www-form-urlencoded; charset=UTF-8")
                    .setHeader("X-Requested-With", "XMLHttpRequest")
                    .setHeader("Origin", "https://m.albert.nyu.edu")
                    .setHeader("DNT", "1")
                    .setHeader("Connection", "keep-alive")
                    .setHeader("Cookie", newContext.cookies.stream()
                            .map(it -> it.name() + '=' + it.value())
                            .collect(Collectors.joining("; ")))
                    .setMethod("POST")
                    .setBody(form)
                    .build();
    GetClient.getClient()
            .executeRequest(sectionRequest)
            .toCompletableFuture()
            .handleAsync(((resp, throwable) -> { return null; }));
    // @ToDo: Handle this later
    return null;
  }

  /**
   * Currently only work for Fall 2020. Fix later
   */
  public static Future<String> addOptions(Context.HttpContext context, String option,
                                          Integer permissionNo, Integer units, String school,
                                          Term term, Integer registrationNumber) {
    //@ToDo: Order the args in right orders. Change the start date based on term
    String form = String.format(
            "CSRFToken=%s&wait_list_ok=%s&permission_nbr=%s&units=%s&" +
            "start_date=09/02/2020&rqmnt_designtn_opt=N&instructor=" +
            "&institution=NYUNV&acad_career=%s&strm=%s&class_nbr=%s&options_submit=",
            context.cookies, option, permissionNo, units, school, term, registrationNumber
    );

    /**
     * Make the request given session token and shopping cart
     * "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD/1204/7669";
     */
    Request request =
            new RequestBuilder()
                    .setUri(ADD_OPTIONS_URI)
                    .setRequestTimeout(60000)
                    .setHeader("Referer", SELECT_OPTIONS_ROOT_URL_STRING + term.getId() + "/"
                    + registrationNumber + "/add")
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
                    .setBody(form)
                    .build();
    GetClient.getClient()
            .executeRequest(request)
            .toCompletableFuture()
            .handleAsync(((resp, throwable) -> { return null; }));
    // @ToDo: Handle this later
    return null;
  }


}
