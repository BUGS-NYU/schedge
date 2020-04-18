package register;

import io.netty.handler.codec.http.cookie.Cookie;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import nyu.SubjectCode;
import nyu.Term;
import nyu.User;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import scraping.models.CatalogQueryData;
import scraping.query.GetClient;
import scraping.query.QueryCatalog;
import utils.SimpleBatchedFutureEngine;

import static register.Context.getContextAsync;

public class AddToCart {
  // We will do undergrad for now/ Remember to add term and registration number
  // at the end
  private static final String SHOPPING_CART_ADD_DATA_URL_STRING =
      "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD/";
  private static final String ROOT_URL_STRING =
      "https://m.albert.nyu.edu/app/student/enrollmentcart/cart";

  public static void
  addToCart(User user, Term term, List<Integer> registrationNumbers,
            Context.HttpContext context) {
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

  public static Future<String> addToCart(User user, Term term, int registrationNumber,
                               Context.HttpContext context) {
    // @TODO: Turn this into static if possible
    List<Cookie> cookies = GetLogin.getLoginSession(user, context);

    /**
     * Make the request given session token and shopping cart
     * "https://m.albert.nyu.edu/app/student/enrollmentcart/addToCart/NYUNV/UGRD/1204/7669";
     */
    Request request2 =
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
            .setHeader("Cookie", cookies.stream()
                                     .map(it -> it.name() + '=' + it.value())
                                     .collect(Collectors.joining("; ")))
            .setMethod("GET")
            .build();
    GetClient.getClient()
        .executeRequest(request2)
        .toCompletableFuture()
        .handleAsync(((response2, throwable) -> { return null; }));
    // @ToDo: Handle this later
    return null;
  }
}
