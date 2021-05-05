package register;

import static register.Context.getContextAsync;

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

public class AddToCart {
  // We will do undergrad for now
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
  private static final Uri ADD_OPTIONS_URI = Uri.create(
      "https://m.albert.nyu.edu/app/student/enrollmentcart/addOptions");

  public static void addToCart(User user, Term term,
                               List<RegistrationCourse> courses) {
    int size = courses.size();
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
        courses.iterator(), size,
        (course, idx) -> addToCart(user, term, course, contexts[idx]));

    engine.iterator();
  }

  public static Future<String> addToCart(User user, Term term,
                                         RegistrationCourse course,
                                         Context.HttpContext context) {
    // @TODO: Turn this into static if possible
    Context.HttpContext newContext = GetLogin.getLoginSession(user, context);
    String sectionsForm = getSectionsForm(term, course, newContext);

    /**
     * Make the request given session token and shopping cart
     * "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD/1204/7669";
     */
    Request request =
        new RequestBuilder()
            .setUri(Uri.create(SHOPPING_CART_ADD_DATA_URL_STRING +
                               term.getId() + "/" + course.registrationNumber))
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
    //    GetClient.getClient()
    //            .executeRequest(request)
    //            .toCompletableFuture()
    //            .handleAsync(((resp, throwable) -> {
    //              //if status code is 200 then there is an error
    //              return null;
    //            }));

    if (sectionsForm != null) {
      System.out.println(sectionsForm);
      Request sectionRequest =
          new RequestBuilder()
              .setUri(Uri.create(ADD_RELATED_DATA_URL_STRING))
              .setRequestTimeout(60000)
              .setHeader("Referer", ADD_RELATED_ROOT_URL_STRING + term.getId() +
                                        "/" + course.registrationNumber)
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
              .setBody(sectionsForm)
              .build();

      GetClient.getClient()
          .executeRequest(sectionRequest)
          .toCompletableFuture()
          .handleAsync(((resp, throwable) -> { return null; }));
    }

    GetClient.getClient()
        .executeRequest(request)
        .toCompletableFuture()
        .handleAsync(((resp, throwable) -> {
          // if status code is 200 then there is an error
          return null;
        }));
    return null;
  }

  public static Future<Void> addRelated(Term term, String form,
                                        Integer registrationNumber,
                                        Context.HttpContext context) {

    /**
     * Make the request given session token and shopping cart
     * "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD/1204/7669";
     */
    System.out.println(ADD_RELATED_ROOT_URL_STRING + term.getId() + "/" +
                       registrationNumber);
    Request sectionRequest =
        new RequestBuilder()
            .setUri(Uri.create(ADD_RELATED_DATA_URL_STRING))
            .setRequestTimeout(60000)
            .setHeader("Referer", ADD_RELATED_ROOT_URL_STRING + term.getId() +
                                      "/" + registrationNumber)
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
        .executeRequest(sectionRequest)
        .toCompletableFuture()
        .handleAsync(((resp, throwable) -> { return null; }));
    return null;
  }

  /**
   * Currently only work for Fall 2020. Fix later
   */
  public static Future<String> addOptions(Term term, String form,
                                          Integer registrationNumber,
                                          Context.HttpContext context) {

    /**
     * Make the request given session token and shopping cart
     * "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD/1204/7669";
     */
    Request optionRequest =
        new RequestBuilder()
            .setUri(ADD_OPTIONS_URI)
            .setRequestTimeout(60000)
            .setHeader("Referer", SELECT_OPTIONS_ROOT_URL_STRING +
                                      term.getId() + "/" + registrationNumber +
                                      "/add")
            .setHeader("Host", "m.albert.nyu.edu")
            .setHeader("Accept-Language", "en-US,en;q=0.9,vi;q=0.8")
            .setHeader("Accept-Encoding", "gzip, deflate, br")
            .setHeader("Content-Type", "application/x-www-form-urlencoded")
            .setHeader("cache-Control", "no-cache")
            .setHeader("Sec-Fetch-Dest", "document")
            .setHeader("Sec-Fetch-Mode", "navigate")
            .setHeader("Sec-Fetch-Size", "same-origin")
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
        .executeRequest(optionRequest)
        .toCompletableFuture()
        .handleAsync(((resp, throwable) -> { return null; }));
    return null;
  }

  private static String getSectionsForm(Term term, RegistrationCourse course,
                                        Context.HttpContext context) {
    if (course.sectionsRelated.size() == 0) {
      return null;
    }
    String form = String.format(
        "institution=NYUNV&acad_career=UGRD&strm=%s&class_nbr=%s&CSRFToken=%s",
        term.getId(), course.registrationNumber, context.csrfToken);
    for (int i = 0; i < course.sectionsRelated.size(); i++) {
      form += "&component" + (i + 1) + "=" +
              course.sectionsRelated.get(i).intValue();
    }
    return form;
  }

  //@ToDo: Change the start date based on term. Handle different start dates and
  // different acad_career
  private static String getOptionForm(Term term, RegistrationCourse course,
                                      Context.HttpContext context) {
    if (course.units == Math.round(course.units)) {
      return String.format(
          "CSRFToken=%s&wait_list_ok=%s&permission_nbr=%s&units=%s&"
              + "start_date=09/02/2020&rqmnt_designtn_opt=N&instructor="
              +
              "&institution=NYUNV&acad_career=UGRD&strm=%s&class_nbr=%s&options_submit=",
          context.csrfToken, course.waitList, course.permissionNo,
          Math.round(course.units), term.getId(), course.registrationNumber);
    }
    return String.format(
        "CSRFToken=%s&wait_list_ok=%s&permission_nbr=%s&units=%s&"
            + "start_date=09/02/2020&rqmnt_designtn_opt=N&instructor="
            +
            "&institution=NYUNV&acad_career=UGRD&strm=%s&class_nbr=%s&options_submit=",
        context.csrfToken, course.waitList, course.permissionNo, course.units,
        term.getId(), course.registrationNumber);
  }
}
