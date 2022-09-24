package scraping;

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
import types.Term;

/* User flow

  1.  Navigate to

        https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL

      and get cookies

  2.  Navigate there again, and get more cookies
  3.  Navigate there again using POST, and set form params



  src/main/java/utils/Client.java
 */

public final class ScrapeSchools {
  public static final class Subject {
    public String code;
    public String name;
  }

  public static final class School {
    public String code;
    public String name;
    public ArrayList<Subject> subjects;
  }

  private static Uri MAIN_URI = Uri.create(
      "https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL");

  private static Uri REDIRECT_URI = Uri.create(
      "https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL?&");

  public static HashMap<String, School> scrapeSchools(AsyncHttpClient client,
                                                      Term term)
      throws ExecutionException, InterruptedException {
    Future<Response> fut;
    Response resp;

    {
      Request mainReq = new RequestBuilder()
                            .setUri(MAIN_URI)
                            .setRequestTimeout(10000)
                            .setMethod("GET")
                            .build();

      fut = client.executeRequest(mainReq);
      resp = fut.get();
    }

    {
      Request redirectReq = new RequestBuilder()
                                .setUri(REDIRECT_URI)
                                .setRequestTimeout(10000)
                                .setMethod("GET")
                                .build();

      fut = client.executeRequest(redirectReq);
      resp = fut.get();

      if (resp == null) {
        System.out.println("resp was null");
      } else {
        System.out.println(resp);
      }
    }

    HashMap<String, School> schoolMap = new HashMap<>();

    return schoolMap;
  }
}
