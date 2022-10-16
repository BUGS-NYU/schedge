package scraping;

import static utils.Nyu.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import org.asynchttpclient.*;
import org.asynchttpclient.uri.Uri;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import utils.Utils;

public final class PeopleSoftClassSearch {
  private static DateTimeFormatter timeParser =
      DateTimeFormatter.ofPattern("MM/dd/yyyy h.mma", Locale.ENGLISH);

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

  public ArrayList<Element> scrapeSchoolElements(Term term)
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
    return group.children();
  }

  public ArrayList<School> scrapeSchools(Term term)
      throws ExecutionException, InterruptedException {
    var children = scrapeSchoolElements(term);

    var schools = new ArrayList<School>();
    for (var child : children) {
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

  public static Object scrapeSubject(AsyncHttpClient client, Term term,
                                     String subject)
      throws ExecutionException, InterruptedException {
    var self = new PeopleSoftClassSearch(client);
    return self.scrapeSubject(term, subject);
  }

  public Object scrapeSubject(Term term, String subjectCode)
      throws ExecutionException, InterruptedException {
    var group = scrapeSchoolElements(term);

    Subject subject = null;
    String actionString = null;
    for (var child : group) {
      var schoolTags = child.select("div.ps_box-link");
      for (var schoolTag : schoolTags) {
        var schoolTitle = schoolTag.text();
        var parts = schoolTitle.split("\\(");

        var titlePart = parts[0].trim();
        var codePart = parts[1];
        codePart = codePart.substring(0, codePart.length() - 1);

        if (codePart.contentEquals(subjectCode)) {
          subject = new Subject(codePart, titlePart);
          actionString = schoolTag.id().substring(7);
        }
      }
    }

    if (subject == null)
      throw new RuntimeException("Subject not found: " + subjectCode);

    {
      incrementStateNum();
      formMap.put("ICAction", actionString);

      var fut = client.executeRequest(post(MAIN_URI, formMap));
      var resp = fut.get();
      var responseBody = resp.getResponseBody();

      return parseSubject(responseBody, subjectCode);
    }
  }

  static Object parseSubject(String html, String subjectCode) {
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

    return coursesContainer;
  }

  static Course parseCourse(Element courseHtml, String subjectCode) {
    var course = new Course();

    // This happens to work; nothing else really works as well.
    var sections = courseHtml.select(".ps-htmlarea");

    {
      var section = sections.get(0);
      var titleText = section.expectFirst("b").text().trim();
      var titleSections = titleText.split(" ", 3);

      var descrElements = section.select("p");
      var descrP = descrElements.get(descrElements.size() - 2);

      course.name = titleSections[2];
      course.subjectCode = titleSections[0];
      course.deptCourseId = titleSections[1];
      course.sections = new ArrayList<>();

      var textNodes = descrP.textNodes();
      if (!textNodes.isEmpty()) {
        course.description = textNodes.get(0).text();
      }
    }

    // System.err.println("" + course);
    // System.err.println("  descr: " + course.description + "\n");

    if (!course.subjectCode.contentEquals(subjectCode)) {
      // This isn't an error for something like `SCA-UA`/`SCA-UA_1`, but
      // could be different than expected for other schools,
      // so for now we just log it.
      //                  - Albert Liu, Oct 16, 2022 Sun 13:43
      System.err.println("course.subjectCode=" + course.subjectCode +
                         ", but subject=" + subjectCode);
    }

    var first = true;
    for (var sectionHtml : sections) {
      if (first) {
        first = false;
        continue;
      }

      var section = parseSection(sectionHtml);
      course.sections.add(section);
    }

    // System.err.println("  sections: " + course.sections + "\n");

    return course;
  }

  static Section parseSection(Element sectionHtml) {
    var wrapper = sectionHtml.expectFirst("td");
    var data = wrapper.children();

    var section = new Section();
    section.recitations = new ArrayList<>();
    section.meetings = new ArrayList<>();

    {
      var fields = new HashMap<String, String>();

      for (var attrLineDiv : data.get(1).select("div")) {
        var attrLine = attrLineDiv.text();
        var parts = attrLine.trim().split(":", 2);

        if (parts.length == 1) {
          fields.put(parts[0].trim(), "");
          continue;
        }

        fields.put(parts[0].trim(), parts[1].trim());
      }

      // System.err.println("  " + fields + "\n");

      var s = section;
      s.registrationNumber = Integer.parseInt(fields.get("Class#"));
      s.code = fields.get("Section");
      s.type = SectionType.LEC; // fields.get("Component");
      s.instructionMode = fields.get("Instruction Mode");
      s.campus = fields.get("Course Location");
      s.grading = fields.get("Grading");
      s.status = SectionStatus.Open; // fields.get("Class Status");
    }

    // for (int i = 2; i < data.size(); i++) {
    //   var element = data.get(i);
    //   if (element.tagName().contentEquals("br")) {
    //     continue;
    //   }

    //   System.err.println("  " + element + "\n");
    // }

    // 01/25/2021 - 05/14/2021 Thu 6.15 PM - 7.30 PM at SPR 2503 with
    // Morrison, George Arthur; Margarint, Vlad-Dumitru
    String metaText = null;

    var notes = new StringBuilder();
    for (var node : wrapper.textNodes()) {
      var text = node.text().trim();
      if (text.length() == 0)
        continue;

      // System.err.println("  - " + text + "\n");
      if (metaText == null) {
        metaText = text;
      } else {
        notes.append(text);
      }
    }

    section.notes = notes.toString().trim();
    if (section.notes.length() == 0)
      section.notes = null;

    var parts = metaText.split(" with ", 2);
    if (parts.length == 2) {
      section.instructors = parts[1].split("; ");
    }

    parts = parts[0].split(" at ", 2);
    if (parts.length == 2) {
      section.location = parts[1];
    }

    parts = parts[0].split(" ");
    if (parts.length <= 3) {
      // No meetings
      return section;
    }

    int tokenIdx = 0;
    int duration;
    LocalDateTime beginDateTime, endDateTime;
    DayOfWeek[] days;
    {
      var beginDateStr = parts[tokenIdx];
      var endDateStr = parts[tokenIdx + 2];

      var dayStr = parts[tokenIdx + 3];
      var beginTimeStr = parts[tokenIdx + 4];
      var beginTimeHalfStr = parts[tokenIdx + 5];
      var endTimeStr = parts[tokenIdx + 7];
      var endTimeHalfStr = parts[tokenIdx + 8];

      beginDateTime = LocalDateTime.from(timeParser.parse(
          beginDateStr + ' ' + beginTimeStr + beginTimeHalfStr));
      var stopDateTime = LocalDateTime.from(
          timeParser.parse(beginDateStr + ' ' + endTimeStr + endTimeHalfStr));
      duration = (int)ChronoUnit.MINUTES.between(beginDateTime, stopDateTime);

      endDateTime =
          LocalDateTime.from(timeParser.parse(endDateStr + " 11.59PM"));

      var dayStrings = dayStr.split(",");
      days = new DayOfWeek[dayStrings.length];
      for (int i = 0; i < dayStrings.length; i++) {
        days[i] = Utils.parseDayOfWeek(dayStrings[i]);
      }
    }

    var tz = Utils.timezoneForCampus(section.campus);

    beginDateTime = beginDateTime.atZone(tz)
                        .withZoneSameInstant(ZoneOffset.UTC)
                        .toLocalDateTime();

    endDateTime = endDateTime.atZone(tz)
                      .withZoneSameInstant(ZoneOffset.UTC)
                      .toLocalDateTime();

    for (var day : days) {
      var meeting = new Meeting();
      var adjuster = TemporalAdjusters.nextOrSame(day);
      meeting.beginDate = beginDateTime.with(adjuster);
      meeting.minutesDuration = duration;
      meeting.endDate = endDateTime;

      section.meetings.add(meeting);
    }

    return section;
  }

  static Integer findIndices(ArrayList<School> schools, String subjectCode) {
    var index = 0;
    for (var school : schools) {
      for (var subject : school.subjects) {
        if (subject.code.contentEquals(subjectCode)) {
          return index;
        }

        index += 1;
      }
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
