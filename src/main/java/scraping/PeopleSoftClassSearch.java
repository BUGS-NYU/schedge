package scraping;

import static utils.Nyu.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import org.asynchttpclient.*;
import org.asynchttpclient.uri.Uri;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

public final class PeopleSoftClassSearch {
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

  static Uri MAIN_URI = Uri.create(MAIN_URL);
  static Uri REDIRECT_URI = Uri.create(MAIN_URL + "?&");

  HashMap<String, String> formMap;
  final AsyncHttpClient client;

  PeopleSoftClassSearch(AsyncHttpClient client) { this.client = client; }

  public static String formEncode(HashMap<String, String> values) {
    return values.entrySet()
        .stream()
        .map(e -> {
          return e.getKey() + "=" +
              URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8);
        })
        .collect(Collectors.joining("&"));
  }

  Future<Response> navigateToTerm(Term term)
      throws ExecutionException, InterruptedException {
    String yearText;
    switch (term.semester) {
    case ja:
    case sp:
    case su:
      yearText = (term.year - 1) + "-" + term.year;
      break;

    case fa:
      yearText = term.year + "-" + (term.year + 1);
      break;

    default:
      throw new RuntimeException("whatever");
    }

    String semesterId;
    {
      switch (term.semester) {
      case fa:
        semesterId = "NYU_CLS_WRK_NYU_FALL$36$";
        break;
      case ja:
        semesterId = "NYU_CLS_WRK_NYU_WINTER$37$";
        break;
      case sp:
        semesterId = "NYU_CLS_WRK_NYU_SPRING$38$";
        break;
      case su:
        semesterId = "NYU_CLS_WRK_NYU_SUMMER$39$";
        break;

      default:
        throw new RuntimeException("whatever");
      }
    }

    { // ignore the response here because we just want the cookies
      var fut = client.executeRequest(get(MAIN_URI));
      fut.get();
    }

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
      formMap.put("ICAction", id);
    }

    { // Get the correct state on the page
      var fut = client.executeRequest(post(MAIN_URI, formMap));
      fut.get();
    }

    {
      incrementStateNum();
      formMap.put("ICAction", semesterId);
      formMap.put(semesterId, "Y");

      var fut = client.executeRequest(post(MAIN_URI, formMap));

      return fut;
    }
  }

  public static ArrayList<School> scrapeSchools(AsyncHttpClient client,
                                                Term term)
      throws ExecutionException, InterruptedException {
    var self = new PeopleSoftClassSearch(client);
    return self.scrapeSchools(term);
  }

  public ArrayList<School> scrapeSchools(Term term)
      throws ExecutionException, InterruptedException {
    var fut = navigateToTerm(term);
    var resp = fut.get();
    var responseBody = resp.getResponseBody();
    var doc = Jsoup.parse(responseBody, MAIN_URL);

    var field = doc.expectFirst("#win0divNYU_CLASS_SEARCH");
    var cdata = (CDataNode)field.textNodes().get(0);

    doc = Jsoup.parse(cdata.text(), MAIN_URL);
    var results = doc.expectFirst("#win0divRESULTS");
    var group = results.expectFirst("div[id=win0divGROUP$0]");

    var schools = new ArrayList<School>();
    for (var child : group.children()) {
      var header = child.expectFirst("h2");
      var school = new School(header.text());
      schools.add(school);

      var schoolTags = child.select("div.ps_box-link");
      for (var schoolTag : schoolTags) {
        var schoolTitle = schoolTag.text();
        var parts = schoolTitle.split("\\(");

        var titlePart = parts[0].trim();
        var codePart = parts[1];
        codePart = codePart.substring(0, codePart.length() - 1);

        school.subjects.add(new Subject(codePart, titlePart));
      }
    }

    return schools;
  }

  public static Object scrapeCourses(AsyncHttpClient client, Term term,
                                     String subject)
      throws ExecutionException, InterruptedException {
    var self = new PeopleSoftClassSearch(client);
    return self.scrapeCourses(term, subject);
  }

  public Object scrapeCourses(Term term, String subjectCode)
      throws ExecutionException, InterruptedException {
    var schools = scrapeSchools(term);

    var indices = findIndices(schools, subjectCode);
    if (indices == null)
      throw new RuntimeException("Subject not found: " + subjectCode);

    {
      incrementStateNum();
      formMap.put("ICAction",
                  "LINK" + (indices.school + 1) + "$" + (indices.subject + 1));

      var fut = client.executeRequest(post(MAIN_URI, formMap));
      var resp = fut.get();
      var responseBody = resp.getResponseBody();

      return parseSubject(responseBody, subjectCode);
    }
  }

  static ArrayList<Course> parseSubject(String html, String subjectCode) {
    var doc = Jsoup.parse(html, MAIN_URL);

    {
      var field = doc.expectFirst("#win0divPAGECONTAINER");
      var cdata = (CDataNode)field.textNodes().get(0);
      doc = Jsoup.parse(cdata.text(), MAIN_URL);
    }

    var coursesContainer = doc.expectFirst("div[id=win0divSELECT_COURSE$0]");

    var courses = new ArrayList<Course>();
    for (var courseHtml : coursesContainer.children()) {
      courses.add(parseCourse(courseHtml, subjectCode));
    }

    return courses;
  }

  static Course parseCourse(Element courseHtml, String subjectCode) {
    var course = new Course();

    // This happens to work; nothing else really works as well.
    var sections = courseHtml.select(".ps-htmlarea");

    {
      var section = sections.get(0);
      var titleText = section.expectFirst("b").text().trim();
      var titleSections = titleText.split(" ", 3);

      var descriptionElements = section.select("p");
      var descriptionP =
          descriptionElements.get(descriptionElements.size() - 1);

      course.name = titleSections[2];
      course.subjectCode = titleSections[0];
      course.deptCourseId = titleSections[1];
      course.description = descriptionP.text();
      course.sections = new ArrayList<>();
    }

    if (!course.subjectCode.contentEquals(subjectCode)) {
      throw new RuntimeException("course.subjectCode=" + course.subjectCode +
                                 ", but subjectCode=" + subjectCode);
    }

    var first = true;
    for (var section : sections) {
      if (first) {
        first = false;
        continue;
      }

      System.err.println("Hello" + course);
    }

    return course;
  }

  static final class SubjectIndices {
    int school = 0;
    int subject = 0;
  }
  static SubjectIndices findIndices(ArrayList<School> schools,
                                    String subjectCode) {
    var indices = new SubjectIndices();
    for (var school : schools) {
      indices.subject = 0;

      for (var subject : school.subjects) {
        if (subject.code.contentEquals(subjectCode)) {
          return indices;
        }

        indices.subject += 1;
      }

      indices.school += 1;
    }

    return null;
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

  static Request get(Uri uri) {
    return new RequestBuilder()
        .setUri(uri)
        .setRequestTimeout(10_000)
        .setMethod("GET")
        .setHeader("User-Agent", USER_AGENT)
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
