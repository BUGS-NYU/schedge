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
import org.jsoup.nodes.CDataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.*;
import types.Term;

/* User flow

  3.  Navigate there again using POST, and set form params



  src/main/java/utils/Client.java
 */

public final class PeopleSoftClassSearch {
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

  public static Object scrapeSchools(AsyncHttpClient client, Term term)
      throws ExecutionException, InterruptedException {
    String yearText;
    switch (term.semester) {
    case ja:
    case sp:
    case su:
      yearText = (term.year - 1) + "-" + term.year;
      break;

    case fa:
    default:
      yearText = term.year + "-" + (term.year + 1);
      break;
    }

    String semesterId;
    {
      switch (term.semester) {
      case fa:
        semesterId = "win0divNYU_CLS_WRK_NYU_FALL$36$";
        break;
      case ja:
        semesterId = "win0divNYU_CLS_WRK_NYU_WINTER$37$";
        break;
      case sp:
        semesterId = "win0divNYU_CLS_WRK_NYU_SPRING$38$";
        break;
      case su:
        semesterId = "win0divNYU_CLS_WRK_NYU_SUMMER$39$";
        break;
      default:
        throw new RuntimeException("whatever");
      }
    }

    { // ignore the response here because we just want the cookies
      var fut = client.executeRequest(get(MAIN_URI));
      fut.get();
    }

    HashMap<String, String> formMap;
    {
      var fut = client.executeRequest(get(REDIRECT_URI));
      var resp = fut.get();
      var responseBody = resp.getResponseBody();
      var doc = Jsoup.parse(responseBody, MAIN_URL);
      var body = doc.body();

      var yearHeader = body.expectFirst("div#win0divACAD_YEAR");
      var links = yearHeader.select("a.ps-link");

      String id = null;
      for (Element link : links) {
        var text = link.text();
        if (!text.contentEquals(yearText))
          continue;

        id = link.id();
      }
      if (id == null)
        throw new RuntimeException("yearText not found");

      formMap = parseFormFields(body);
      formMap.put("ICNAVTYPEDROPDOWN", "0");
    }

    { // Get the correct state on the page
      formMap.put("ICAction", id);

      var fut = client.executeRequest(post(MAIN_URI, formMap));
      fut.get();
    }

    {
      int action = Integer.parseInt(formMap.get("ICStateNum"));
      action += 1;
      formMap.put("ICStateNum", "" + action);
      // formMap.put("ICAction", );
    }

    return options;
  }

  static HashMap<String, String> parseFormFields(Element body) {
    var optionsRoot = body.expectFirst("#win0divPSTOOLSHIDDENS");
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

  static Request get(Uri uri) {
    return new RequestBuilder()
        .setUri(uri)
        .setRequestTimeout(10_000)
        .setMethod("GET")
        .build();
  }

  static Request post(Uri uri, HashMap<String, String> body) {
    String s = formEncode(body);

    return new RequestBuilder()
        .setUri(uri)
        .setRequestTimeout(10_000)
        .setMethod("POST")
        .setHeader("Content-Type", "application/x-www-form-urlencoded")
        .setBody(s)
        .build();
  }
}
