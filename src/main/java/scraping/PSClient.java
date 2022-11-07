package scraping;

import static utils.ArrayJS.*;
import static utils.Nyu.*;
import static utils.Try.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.slf4j.*;
import utils.*;

public class PSClient {
  static Logger logger = PeopleSoftClassSearch.logger;

  HttpClient client;
  HashMap<String, String> formMap;
  CompletableFuture<HttpResponse<String>> inProgress;

  public PSClient() {
    var cookieHandler = new CookieManager();
    var builder = HttpClient.newBuilder();

    this.client = builder.connectTimeout(Duration.of(30, ChronoUnit.SECONDS))
                      .cookieHandler(cookieHandler)
                      .build();
  }

  Future<HttpResponse<String>> navigateToTerm(Nyu.Term term)
      throws IOException, InterruptedException {
    String yearText = yearText(term);
    String semesterId = semesterId(term);

    var handler = HttpResponse.BodyHandlers.ofString();
    { // ignore the response here because we just want the cookies
      client.send(get(MAIN_URI), handler);
    }

    {
      var resp = client.send(get(REDIRECT_URI), handler);
      var responseBody = resp.body();
      var doc = Jsoup.parse(responseBody, MAIN_URL);
      var body = doc.body();

      var yearHeader = body.expectFirst("div#win0divACAD_YEAR");
      var links = yearHeader.select("a.ps-link");

      var link = find(links, l -> l.text().equals(yearText));
      if (link == null)
        throw new RuntimeException("yearText not found");

      var id = link.id();

      formMap = parseFormFields(body);
      formMap.put("ICAction", id);
    }

    { // Get the correct state on the page
      client.send(post(MAIN_URI, formMap), handler);
    }

    {
      incrementStateNum();
      formMap.put("ICAction", semesterId);
      formMap.put(semesterId, "Y");

      var resp = client.sendAsync(post(MAIN_URI, formMap), handler);

      return resp;
    }
  }

  Future<HttpResponse<String>>
  fetchSubject(PeopleSoftClassSearch.SubjectElem subject) {
    var fut = this.inProgress;
    if (fut == null) {
      fut = CompletableFuture.completedFuture(null);
    }

    var handler = HttpResponse.BodyHandlers.ofString();

    var out = fut.handle((resp_, err_) -> {
      incrementStateNum();
      formMap.put("ICAction", subject.action);

      return tcPass(() -> client.send(post(MAIN_URI, formMap), handler));
    });

    this.inProgress = out.handle((resp_, err) -> {
      if (err != null) {
        // @TODO
      }

      incrementStateNum();
      formMap.put("ICAction", "NYU_CLS_DERIVED_BACK");

      var resp = tcPass(() -> client.send(post(MAIN_URI, formMap), handler));

      tcPass(() -> Thread.sleep(5000));

      return resp;
    });

    return out;
  }

  static String MAIN_URL =
      "https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL";
  static URI MAIN_URI = URI.create(MAIN_URL);
  static URI REDIRECT_URI = URI.create(MAIN_URL + "?&");

  static final PeopleSoftClassSearch.FormEntry[] FORM_DEFAULTS =
      new PeopleSoftClassSearch.FormEntry[] {
          new PeopleSoftClassSearch.FormEntry("ICAJAX", "1"),
          new PeopleSoftClassSearch.FormEntry("ICNAVTYPEDROPDOWN", "0"),
          new PeopleSoftClassSearch.FormEntry(
              "ICBcDomData",
              "C~UnknownValue~EMPLOYEE~SA~NYU_SR.NYU_CLS_SRCH.GBL~"
                  + "NYU_CLS_SRCH~Course Search~UnknownValue~UnknownValue"
                  + "~https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR."
                  + "NYU_CLS_SRCH.GBL?~UnknownValue*C~UnknownValue~EMPLOYEE"
                  + "~SA~NYU_SR.NYU_CLS_SRCH.GBL~NYU_CLS_SRCH~Course Search~"
                  + "UnknownValue~UnknownValue~https://sis.nyu.edu/psc/csprod"
                  + "/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL?~UnknownValue*C"
                  + "~UnknownValue~EMPLOYEE~SA~NYU_SR.NYU_CLS_SRCH.GBL~"
                  + "NYU_CLS_SRCH~Course Search~UnknownValue~UnknownValue"
                  + "~https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR"
                  + ".NYU_CLS_SRCH.GBL?~UnknownValue*C~UnknownValue~"
                  + "EMPLOYEE~SA~NYU_SR.NYU_CLS_SRCH.GBL~NYU_CLS_SRCH"
                  + "~Course Search~UnknownValue~UnknownValue~https"
                  + "://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR."
                  + "NYU_CLS_SRCH.GBL?~UnknownValue"),
      };

  void incrementStateNum() {
    int action = Integer.parseInt(formMap.get("ICStateNum"));
    action += 1;
    formMap.put("ICStateNum", "" + action);
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

  static String yearText(Term term) {
    switch (term.semester) {
    case ja:
    case sp:
    case su:
      return (term.year - 1) + "-" + term.year;

    case fa:
      return term.year + "-" + (term.year + 1);

    default:
      throw new RuntimeException("whatever");
    }
  }

  static String semesterId(Term term) {
    switch (term.semester) {
    case fa:
      return "NYU_CLS_WRK_NYU_FALL$36$";
    case ja:
      return "NYU_CLS_WRK_NYU_WINTER$37$";
    case sp:
      return "NYU_CLS_WRK_NYU_SPRING$38$";
    case su:
      return "NYU_CLS_WRK_NYU_SUMMER$39$";

    default:
      throw new RuntimeException("whatever");
    }
  }

  // I think I get like silently rate-limited during testing without this
  // header.
  static String USER_AGENT =
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:105.0) Gecko/20100101 Firefox/105.0";

  static String formEncode(HashMap<String, String> values) {
    return values.entrySet()
        .stream()
        .map(e -> {
          return e.getKey() + "=" +
              URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8);
        })
        .collect(Collectors.joining("&"));
  }

  static HttpRequest get(URI uri) {
    return HttpRequest.newBuilder()
        .uri(uri)
        .timeout(Duration.of(30, ChronoUnit.SECONDS))
        .setHeader("User-Agent", USER_AGENT)
        .setHeader("Content-Type", "application/x-www-form-urlencoded")
        .GET()
        .build();
  }

  static HttpRequest post(URI uri, HashMap<String, String> body) {
    String s = formEncode(body);

    return HttpRequest.newBuilder()
        .uri(uri)
        .timeout(Duration.of(30, ChronoUnit.SECONDS))
        .setHeader("User-Agent", USER_AGENT)
        .setHeader("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(s))
        .build();
  }
}
