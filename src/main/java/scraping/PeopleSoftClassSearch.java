package scraping;

import static utils.ArrayJS.*;
import static utils.Nyu.*;

import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import me.tongfei.progressbar.ProgressBar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.slf4j.*;
import utils.Try;

public final class PeopleSoftClassSearch {
  static Logger logger =
      LoggerFactory.getLogger("scraping.PeopleSoftClassSearch");

  public static final class SubjectElem {
    public final String schoolName;
    public final String code;
    public final String name;
    public final String action;

    SubjectElem(String school, String code, String name, String action) {
      this.schoolName = school;
      this.code = code;
      this.name = name;
      this.action = action;
    }

    @Override
    public String toString() {
      return "SubjectElem(schoolName=" + schoolName + ",code=" + code +
          ",name=" + name + ",action=" + action + ")";
    }
  }

  public static final class CoursesForTerm {
    public final ArrayList<School> schools = new ArrayList<>();
    public final ArrayList<Course> courses = new ArrayList<>();
  }

  public static final class FormEntry {
    public final String key;
    public final String value;

    public FormEntry(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }

  static String MAIN_URL =
      "https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL";

  static final FormEntry[] FORM_DEFAULTS = new FormEntry[] {
      new FormEntry("ICAJAX", "1"),
      new FormEntry("ICNAVTYPEDROPDOWN", "0"),
      new FormEntry(
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

  static URI MAIN_URI = URI.create(MAIN_URL);
  static URI REDIRECT_URI = URI.create(MAIN_URL + "?&");

  HashMap<String, String> formMap;
  final HttpClient client;
  final Try ctx = Try.Ctx(logger);

  public PeopleSoftClassSearch() {
    var cookieHandler = new CookieManager();
    var builder = HttpClient.newBuilder();

    this.client = builder.connectTimeout(Duration.of(30, ChronoUnit.SECONDS))
                      .cookieHandler(cookieHandler)
                      .build();
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

  public ArrayList<School> scrapeSchools(Term term) {
    ctx.put("term", term);

    return ctx.log(() -> {
      var subjects = scrapeSubjectList(term);
      return PSCoursesParser.translateSubjects(subjects);
    });
  }

  public ArrayList<Course> scrapeSubject(Term term, String subjectCode) {
    ctx.put("term", term);
    ctx.put("subject", subjectCode);

    return ctx.log(() -> {
      var subjects = scrapeSubjectList(term);

      var subject = find(subjects, s -> s.code.equals(subjectCode));
      if (subject == null)
        throw new RuntimeException("Subject not found: " + subjectCode);

      {
        incrementStateNum();
        formMap.put("ICAction", subject.action);

        var resp = client.send(post(MAIN_URI, formMap),
                               HttpResponse.BodyHandlers.ofString());
        var responseBody = resp.body();

        return PSCoursesParser.parseSubject(ctx, responseBody, subjectCode);
      }
    });
  }

  // @TODO: There's a more good way to do this, where we just imitate a user
  // navigating the page normally; but I am tired and don't want to do it
  // right now.
  //
  //                          - Albert Liu, Nov 03, 2022 Thu 16:08
  /**
   * @param term The term to scrape
   * @param bar Nullable progress bar to output progress to
   */
  public CoursesForTerm scrapeTerm(Term term, ProgressBar bar) {
    return ctx.log(() -> { return scrapeTermInternal(term, bar); });
  }

  CoursesForTerm scrapeTermInternal(Term term, ProgressBar bar)
      throws ExecutionException, IOException, InterruptedException {
    var out = new CoursesForTerm();
    if (bar != null) {
      bar.setExtraMessage("fetching subject list...");
      bar.maxHint(-1);
    }

    ctx.put("term", term);

    var rawSubjects = ctx.log(() -> {
      var subjects = scrapeSubjectList(term);

      out.schools.addAll(PSCoursesParser.translateSubjects(subjects));
      ctx.put("schools", out.schools);

      return subjects;
    });

    if (bar != null) {
      bar.maxHint(rawSubjects.size() + 1);
    }

    var handler = HttpResponse.BodyHandlers.ofString();

    for (var subject : rawSubjects) {
      if (bar != null) {
        bar.setExtraMessage("fetching " + subject.code);
        bar.step();
      }

      ctx.put("subject", subject);

      Thread.sleep(5_000);
      // client.getConfig().getCookieStore().clear();

      incrementStateNum();
      formMap.put("ICAction", subject.action);

      var resp = client.send(post(MAIN_URI, formMap), handler);
      var responseBody = resp.body();

      var courses =
          PSCoursesParser.parseSubject(ctx, responseBody, subject.code);
      out.courses.addAll(courses);

      incrementStateNum();
      formMap.put("ICAction", "NYU_CLS_DERIVED_BACK");

      resp = client.send(post(MAIN_URI, formMap), handler);
      responseBody = resp.body();
    }

    return out;
  }

  HttpResponse<String> navigateToTerm(Term term)
      throws IOException, ExecutionException, InterruptedException {
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
      ctx.put("formMap", formMap);
    }

    { // Get the correct state on the page
      client.send(post(MAIN_URI, formMap), handler);
    }

    {
      incrementStateNum();
      formMap.put("ICAction", semesterId);
      formMap.put(semesterId, "Y");

      var resp = client.send(post(MAIN_URI, formMap), handler);

      return resp;
    }
  }

  ArrayList<SubjectElem> scrapeSubjectList(Term term)
      throws ExecutionException, IOException, InterruptedException {
    var resp = navigateToTerm(term);
    var responseBody = resp.body();
    var doc = Jsoup.parse(responseBody, MAIN_URL);

    var field = doc.expectFirst("#win0divNYU_CLASS_SEARCH");
    var cdata = (CDataNode)field.textNodes().get(0);

    doc = Jsoup.parse(cdata.text(), MAIN_URL);
    var results = doc.expectFirst("#win0divRESULTS");
    var group = results.expectFirst("div[id=win0divGROUP$0]");

    var out = new ArrayList<SubjectElem>();
    for (var child : group.children()) {
      var school = child.expectFirst("h2").text();

      var schoolTags = child.select("div.ps_box-link");
      for (var schoolTag : schoolTags) {
        var schoolTitle = schoolTag.text();
        var parts = schoolTitle.split("\\(");

        var titlePart = parts[0].trim();
        var codePart = parts[1];
        codePart = codePart.substring(0, codePart.length() - 1);

        var action = schoolTag.id().substring(7);

        out.add(new SubjectElem(school, codePart, titlePart, action));
      }
    }

    return out;
  }

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
