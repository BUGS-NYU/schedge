package scraping;

import static utils.ArrayJS.*;
import static utils.Http.*;
import static utils.Nyu.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.*;
import org.asynchttpclient.*;
import org.asynchttpclient.uri.Uri;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.slf4j.*;
import utils.Try;
import utils.Utils;

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

  static Uri MAIN_URI = Uri.create(MAIN_URL);
  static Uri REDIRECT_URI = Uri.create(MAIN_URL + "?&");

  HashMap<String, String> formMap;
  final AsyncHttpClient client;
  final Try ctx = Try.Ctx(logger);

  public PeopleSoftClassSearch(AsyncHttpClient client) { this.client = client; }

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

  public ArrayList<School> scrapeSchools(Term term)
      throws ExecutionException, InterruptedException {
    ctx.put("term", term);

    return ctx.log(() -> {
      var subjects = scrapeSubjectList(term);
      return Parser.translateSubjects(subjects);
    });
  }

  public ArrayList<Course> scrapeSubject(Term term, String subjectCode)
      throws ExecutionException, InterruptedException {
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

        var fut = client.executeRequest(post(MAIN_URI, formMap));
        var resp = fut.get();
        var responseBody = resp.getResponseBody();

        return Parser.parseSubject(responseBody, subjectCode);
      }
    });
  }

  // @TODO: There's a more good way to do this, where we just imitate a user
  // navigating the page normally; but I am tired and don't want to do it
  // right now.
  //
  //                          - Albert Liu, Nov 03, 2022 Thu 16:08
  public CoursesForTerm scrapeTerm(Term term)
      throws ExecutionException, InterruptedException {
    var out = new CoursesForTerm();

    ctx.put("term", term);

    out.schools.addAll(scrapeSchools(term));

    System.out.println(out.schools);

    for (var school : out.schools) {
      for (var subject : school.subjects) {
        client.getConfig().getCookieStore().clear();

        System.out.println("SUBJECT: " + subject);

        var subjectCourses = scrapeSubject(term, subject.code);
        out.courses.addAll(subjectCourses);

        Thread.sleep(20_000);
      }
    }

    return out;
  }

  Future<Response> navigateToTerm(Term term)
      throws ExecutionException, InterruptedException {
    String yearText = yearText(term);
    String semesterId = semesterId(term);

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

      var link = find(links, l -> l.text().equals(yearText));
      if (link == null)
        throw new RuntimeException("yearText not found");

      var id = link.id();

      formMap = parseFormFields(body);
      formMap.put("ICAction", id);
      ctx.put("formMap", formMap);
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

  ArrayList<SubjectElem> scrapeSubjectList(Term term)
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
}

class Parser {
  static Logger logger = PeopleSoftClassSearch.logger;

  static DateTimeFormatter timeParser =
      DateTimeFormatter.ofPattern("MM/dd/yyyy h.mma", Locale.ENGLISH);

  static ArrayList<School>
  translateSubjects(ArrayList<PeopleSoftClassSearch.SubjectElem> raw) {
    var schools = new ArrayList<School>();
    School school = null;

    for (var subject : raw) {
      if (school == null || !school.name.equals(subject.schoolName)) {
        school = new School(subject.schoolName);
        schools.add(school);
      }

      school.subjects.add(new Subject(subject.code, subject.name));
    }

    return schools;
  }

  static ArrayList<Course> parseSubject(String html, String subjectCode) {
    var doc = Jsoup.parse(html);

    {
      var field = doc.expectFirst("#win0divPAGECONTAINER");
      var cdata = (CDataNode)field.textNodes().get(0);
      doc = Jsoup.parse(cdata.text());
    }

    var coursesContainer = doc.expectFirst("div[id=win0divSELECT_COURSE$0]");

    var courses = new ArrayList<Course>();
    for (var courseHtml : coursesContainer.children()) {
      var course = parseCourse(courseHtml, subjectCode);
      courses.add(course);
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

      var descrElements = section.select("p");
      var descrP = descrElements.get(descrElements.size() - 2);

      course.name = titleSections[2];
      course.subjectCode = titleSections[0];
      course.deptCourseId = titleSections[1];
      course.sections = new ArrayList<>();

      var textNodes = descrP.textNodes();
      if (!textNodes.isEmpty()) {
        course.description = textNodes.get(0).text();
      } else {
        course.description = "";
      }
    }

    if (!course.subjectCode.contentEquals(subjectCode)) {
      // This isn't an error for something like `SCA-UA`/`SCA-UA_1`, but
      // could be different than expected for other schools,
      // so for now we just log it.
      //                  - Albert Liu, Oct 16, 2022 Sun 13:43
      logger.warn("course.subjectCode=" + course.subjectCode +
                  ", but subject=" + subjectCode);
    }

    Section lecture = null;
    for (var sectionHtml : sections.subList(1, sections.size())) {
      var section = parseSection(sectionHtml);
      if (section.type.equals("Lecture")) {
        course.sections.add(section);
        section.recitations = new ArrayList<>();
        lecture = section;

        continue;
      }

      if (lecture == null) {
        course.sections.add(section);
      } else {
        lecture.recitations.add(section);
      }
    }

    return course;
  }

  static Section parseSection(Element sectionHtml) {
    var wrapper = sectionHtml.expectFirst("td");
    var data = wrapper.children();

    var section = new Section();
    section.meetings = new ArrayList<>();

    {
      var title = data.get(0).text();
      var parts = title.split(" \\| ");

      var unitString = parts.length < 2 ? "0 units" : parts[1];
      parts = unitString.split(" ");

      section.minUnits = Double.parseDouble(parts[0]);
      if (parts.length > 2) {
        section.maxUnits = Double.parseDouble(parts[2]);
      } else {
        section.maxUnits = section.minUnits;
      }
    }

    {
      var fields = new HashMap<String, String>();

      for (var attrLineDiv : data.get(1).select("div")) {
        var attrLine = attrLineDiv.text();
        var parts = attrLine.trim().split(":", 2);
        var key = parts[0].trim();

        if (parts.length == 1) {
          fields.put(key, "");
          continue;
        }

        fields.put(key, parts[1].trim());
      }

      var s = section;
      s.registrationNumber = Integer.parseInt(fields.get("Class#"));
      s.code = fields.get("Section");
      s.type = fields.get("Component");
      s.instructionMode = fields.get("Instruction Mode");
      s.campus = fields.get("Course Location");
      s.grading = fields.get("Grading");

      var status = fields.get("Class Status");
      s.status = SectionStatus.parseStatus(status);

      if (s.status == SectionStatus.WaitList) {
        var waitlistString = status.replaceAll("[^0-9]", "");
        s.waitlistTotal = Integer.parseInt(waitlistString);
      }
    }

    // 01/25/2021 - 05/14/2021 Thu 6.15 PM - 7.30 PM at SPR 2503 with
    // Morrison, George Arthur; Margarint, Vlad-Dumitru
    String metaText = null;

    var notes = new StringBuilder();
    for (var node : wrapper.textNodes()) {
      var text = node.text().trim();
      if (text.length() == 0)
        continue;

      if (metaText == null) {
        metaText = text;
      } else {
        notes.append(text);
      }
    }

    section.notes = notes.toString().trim();

    var parts = metaText.split(" with ", 2);
    if (parts.length == 2) {
      section.instructors = parts[1].split("; ");
    } else {
      section.instructors = new String[0];
    }

    parts = parts[0].split(" at ", 2);
    if (parts.length == 2) {
      section.location = parts[1];
    }

    parts = parts[0].split(" ");
    if (parts.length <= 3) {
      return section; // No meetings
    }

    int tokenIdx = 0;
    int duration;
    LocalDateTime beginDateTime, endDateTime;
    DayOfWeek[] days;

    {
      for (var part : parts) {
        System.out.println("part=" + part);
      }
      System.out.println("\n\n");

      var beginDateStr = parts[tokenIdx];
      var endDateStr = parts[tokenIdx + 2];

      var dayStr = parts[tokenIdx + 3];

      // @Note: Asynchronous classes will have the pattern:
      //
      //            01/25/2021 - 05/14/2021 Thu
      //
      // Since the time doesn't really matter anyways, we just substitute
      // in the time of 11.59PM .
      //
      //                    - Albert Liu, Nov 03, 2022 Thu 18:35
      String beginTimeStr, endTimeStr;
      if (parts.length > 4) {
        beginTimeStr = parts[tokenIdx + 4] + parts[tokenIdx + 5];
        endTimeStr = parts[tokenIdx + 7] + parts[tokenIdx + 8];
      } else {
        beginTimeStr = endTimeStr = "11.59PM";
      }

      beginDateTime = LocalDateTime.from(
          timeParser.parse(beginDateStr + ' ' + beginTimeStr));
      var stopDateTime =
          LocalDateTime.from(timeParser.parse(beginDateStr + ' ' + endTimeStr));
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
}
