package scraping;

import static utils.TryCatch.*;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.Collectors;
import org.asynchttpclient.*;
import org.asynchttpclient.cookie.CookieStore;
import org.asynchttpclient.uri.Uri;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.*;
import types.Term;

/* User flow

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

  private static String MAIN_URL =
      "https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL";

  private static Uri MAIN_URI = Uri.create(MAIN_URL);
  private static Uri REDIRECT_URI = Uri.create(MAIN_URL + "?&");

  public static String formEncode(HashMap<String, String> values) {
    return values.entrySet()
        .stream()
        .map(e
             -> e.getKey() + "=" +
                    URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));
  }

  public static
      //  HashMap<String, School>
      Object
      scrapeSchools(AsyncHttpClient client, Term term)
          throws ExecutionException, InterruptedException {
    Future<Response> fut;
    Response resp;

    {
      var mainReq = new RequestBuilder()
                        .setUri(MAIN_URI)
                        .setRequestTimeout(10000)
                        .setMethod("GET")
                        .build();

      fut = client.executeRequest(mainReq);
      resp = fut.get();
    }

    {
      var redirectReq = new RequestBuilder()
                            .setUri(REDIRECT_URI)
                            .setRequestTimeout(10000)
                            .setMethod("GET")
                            .build();

      fut = client.executeRequest(redirectReq);
      resp = fut.get();
    }

    var s = resp.getResponseBody();
    var doc = Jsoup.parse(s, MAIN_URL);
    var body = doc.body();
    var yearHeaders = body.select("div#win0divACAD_YEAR");

    if (yearHeaders.size() != 1) {
      throw new RuntimeException(
          "got unexpected number of matches for the year header");
    }

    Element yearHeader = yearHeaders.get(0);

    // System.out.println(yearHeader);
    return yearHeader;

    // HashMap<String, School> schoolMap = new HashMap<>();

    // return schoolMap;
  }
}
