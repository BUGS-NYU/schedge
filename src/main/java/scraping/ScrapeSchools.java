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

  public static final class FormEntry {
    public final String key;
    public final String value;

    public FormEntry(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }

  private static String MAIN_URL =
      "https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL";

  private static final FormEntry[] FORM_DEFAULTS = new FormEntry[] {
      new FormEntry("ICAJAX", "1"),
      new FormEntry(
          "ICBcDomData",
          "C~UnknownValue~EMPLOYEE~SA~NYU_SR.NYU_CLS_SRCH.GBL~NYU_CLS_SRCH~Course Search~UnknownValue~UnknownValue~https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL?~UnknownValue*C~UnknownValue~EMPLOYEE~SA~NYU_SR.NYU_CLS_SRCH.GBL~NYU_CLS_SRCH~Course Search~UnknownValue~UnknownValue~https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL?~UnknownValue*C~UnknownValue~EMPLOYEE~SA~NYU_SR.NYU_CLS_SRCH.GBL~NYU_CLS_SRCH~Course Search~UnknownValue~UnknownValue~https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL?~UnknownValue*C~UnknownValue~EMPLOYEE~SA~NYU_SR.NYU_CLS_SRCH.GBL~NYU_CLS_SRCH~Course Search~UnknownValue~UnknownValue~https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL?~UnknownValue"),

  };

  private static Uri MAIN_URI = Uri.create(MAIN_URL);
  private static Uri REDIRECT_URI = Uri.create(MAIN_URL + "?&");

  public static String formEncode(HashMap<String, String> values) {
    return values.entrySet()
        .stream()
        .map(e -> {
          return e.getKey() + "=" +
              URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8);
        })
        .collect(Collectors.joining("&"));
  }

  public static
      //  HashMap<String, School>
      Object
      scrapeSchools(AsyncHttpClient client, Term term)
          throws ExecutionException, InterruptedException {

    {
      var mainReq = new RequestBuilder()
                        .setUri(MAIN_URI)
                        .setRequestTimeout(10000)
                        .setMethod("GET")
                        .build();

      Future<Response> fut = client.executeRequest(mainReq);
      fut.get();

      // ignore the response here because we just want the cookies
    }

    String responseBody;
    {
      var redirectReq = new RequestBuilder()
                            .setUri(REDIRECT_URI)
                            .setRequestTimeout(10000)
                            .setMethod("GET")
                            .build();

      var fut = client.executeRequest(redirectReq);
      var resp = fut.get();
      responseBody = resp.getResponseBody();
    }

    var doc = Jsoup.parse(responseBody, MAIN_URL);
    var body = doc.body();
    var yearHeaders = body.select("div#win0divACAD_YEAR");

    if (yearHeaders.size() != 1) {
      throw new RuntimeException(
          "got unexpected number of matches for the year header");
    }

    var links = yearHeaders.get(0).select("a.ps-link");

    for (Element link : links) {
      var id = link.id();
      var text = link.text();
      System.out.println("ID: " + id);
      System.out.println("Text: " + text);

      var formMap = parseFormFields(body);

      formMap.put("ICAction", id);
      formMap.put("ICNAVTYPEDROPDOWN", "0");

      // Future<Response> fut;
      // Response resp;

      {
        var req = post(MAIN_URI, formMap);
        var fut = client.executeRequest(req);
        var resp = fut.get();

        if (resp != null)
          return resp;
      }

      var yearParts = text.split("-");
      var first = Integer.parseInt(yearParts[0]);
      var second = Integer.parseInt(yearParts[1]);

      // 2019 - 2022
      // fa2019 ja2020 sp2020 su2020
      Term fa = new Term(Term.Semester.fa, first);
      Term ja = new Term(Term.Semester.ja, second);
      Term sp = new Term(Term.Semester.sp, second);
      Term su = new Term(Term.Semester.su, second);

      System.out.println(fa);
      System.out.println(ja);
      System.out.println(sp);
      System.out.println(su);
    }

    // System.out.println(yearHeader);
    return links;

    // HashMap<String, School> schoolMap = new HashMap<>();

    // return schoolMap;
  }

  static HashMap<String, String> parseFormFields(Element body) {
    var optionsRoots = body.select("#win0divPSTOOLSHIDDENS");
    if (optionsRoots.size() != 1) {
      throw new RuntimeException("found wrong number of options roots");
    }

    var optionsRoot = optionsRoots.get(0);
    var inputs = optionsRoot.select("input");
    var map = new HashMap<String, String>();
    for (var input : inputs) {
      var attr = input.attributes();

      map.put(input.id(), attr.get("value"));
    }

    for (var entry : FORM_DEFAULTS) {
      map.computeIfAbsent(entry.key, k -> entry.value);
    }

    return map;
  }

  static Request post(Uri uri, HashMap<String, String> body) {
    String s = formEncode(body);

    return new RequestBuilder()
        .setUri(uri)
        .setRequestTimeout(10000)
        .setMethod("POST")
        .setHeader("Content-Type", "application/x-www-form-urlencoded")
        .setBody(s)
        .build();
  }
}
