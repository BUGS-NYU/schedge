package scraping;

import static utils.Client.*;
import static utils.TryCatch.*;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import types.*;
import org.asynchttpclient.*;
import org.asynchttpclient.uri.Uri;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.*;
import scraping.models.Course;
import scraping.models.Section;
import utils.*;

public class ScrapeCatalog {
  private static Logger logger =
      LoggerFactory.getLogger("scraping.ScrapeCatalog");

  private static final String ROOT_URL =
      "https://m.albert.nyu.edu/app/catalog/classSearch";
  private static final Uri DATA_URI =
      Uri.create("https://m.albert.nyu.edu/app/catalog/getClassSearch");

  private static DateTimeFormatter timeParser =
      DateTimeFormatter.ofPattern("MM/dd/yyyy h:mma", Locale.ENGLISH);

  public static List<Course>
  scrapeCatalog(Term term, Iterable<SubjectCode> subjects_, int batchSize) {
    ArrayList<SubjectCode> subjects = new ArrayList<>();
    for (SubjectCode subject : subjects_)
      subjects.add(subject);

    ArrayList<Course> courses = new ArrayList<>();
    if (subjects.isEmpty())
      return courses;

    FutureEngine<Query> engine = new FutureEngine<>();

    engine.add(makeQuery());
    batchSize -= 1;

    for (Query query : engine) {
      if (query == null)
        continue;

      SubjectCode subject = query.subject;
      String data = query.data;

      if (!subjects.isEmpty()) {
        query.subject = subjects.remove(subjects.size() - 1);
        query.data = null;

        engine.add(queryCatalog(term, query));
      }

      if (subject == null)
        continue;

      if (data == null) {
        logger.info("Retrying subject={}, csrfToken={}", subject,
                    query.csrfToken);

        subjects.add(subject);

        continue;
      }

      parseCatalog(courses, data, subject);

      if (batchSize > 0) {
        engine.add(makeQuery());
        batchSize -= 1;
      }

      logger.info("Processed subject={}", subject);
    }

    return courses;
  }

  private static class Query extends Ctx {
    SubjectCode subject;
    String data;
  }

  private static Future<Query> makeQuery() {
    Request request = new RequestBuilder()
                          .setUri(Uri.create(ROOT_URL))
                          .setRequestTimeout(20000)
                          .setMethod("GET")
                          .build();

    return send(request, (resp, e) -> {
      if (resp == null) {
        logger.error("Failed to get context: uri={}", ROOT_URL, e);

        return null;
      }

      Map<String, String> cookies = cookiesFrom(resp);
      String csrf = cookies.get("CSRFCookie");
      if (csrf == null) {
        logger.error("Missing cookie with name=CSRFCookie: cookies={}",
                     cookies);

        return null;
      }

      String cookieString = cookies.entrySet()
                                .stream()
                                .map(it -> it.getKey() + '=' + it.getValue())
                                .collect(Collectors.joining("; "));

      Query query = new Query();
      query.csrfToken = csrf;
      query.cookies = cookieString;

      return query;
    });
  }

  private static Future<Query> queryCatalog(Term term, Query query) {
    SubjectCode subject = query.subject;

    logger.debug("querying catalog for term=" + term +
                 " and subject=" + subject + "...");

    String csrf = query.csrfToken;
    int id = term.getId();
    String code = subject.code;
    String school = subject.schoolCode;

    String format = "CSRFToken=%s&term=%d&acad_group=%s&subject=%s";
    String params = String.format(format, csrf, id, school, code);
    logger.debug("Params are {}.", params);

    // @Note not sure why all these headers are necessary, but NYU's API will
    // fast-fail if these aren't present. Potentially some kind of anti-scraping
    // protection measure.
    //                              - Albert Liu, Jan 30, 2022 Sun 17:12 EST
    Request request =
        new RequestBuilder()
            .setUri(DATA_URI)
            .setRequestTimeout(10000)
            .setHeader("Referer", ROOT_URL + "/" + term.getId())
            .setHeader("Host", "m.albert.nyu.edu")
            .setHeader("Accept-Language", "en-US,en;q=0.5")
            .setHeader("Accept-Encoding", "gzip, deflate, br")
            .setHeader("Content-Type",
                       "application/x-www-form-urlencoded; charset=UTF-8")
            .setHeader("X-Requested-With", "XMLHttpRequest")
            .setHeader("Origin", "https://m.albert.nyu.edu")
            .setHeader("DNT", "1")
            .setHeader("Connection", "keep-alive")
            .setHeader("Referer",
                       "https://m.albert.nyu.edu/app/catalog/classSearch")
            .setHeader("Cookie", query.cookies)
            .setMethod("POST")
            .setBody(params)
            .build();

    return send(request, (resp, throwable) -> {
      query.data = null;

      if (resp == null) {
        logger.error("Exception thrown for request: subject={}", subject,
                     throwable);

        return query;
      }

      String body = resp.getResponseBody();
      if (body.contentEquals("No classes found matching your criteria.")) {
        logger.warn("No classes found matching criteria subject={}",
                    subject.code);

        query.subject = null;

        return query;
      }

      query.data = body;

      return query;
    });
  }

  public static void parseCatalog(ArrayList<Course> courses, String data,
                                  SubjectCode subject) {
    logger.trace("parsing raw catalog data...");
    Document doc = Jsoup.parse(data);
    Elements elements = doc.select("div.primary-head ~ *");

    if (elements.isEmpty()) {
      logger.warn(
          "CSS query `div.primary-head ~ *` returned no values (subject={}).",
          subject);

      return;
    }

    String firstText = elements.get(0).text();
    if (firstText.contentEquals("No classes found matching your criteria.")) {
      logger.debug("No classes found for subject=" + subject);

      return;
    }

    Course course = null;
    Section section = null;
    for (Element element : elements) {
      TryCatch tc = tcNew(logger, "element={}, course={}, section={}", element,
                          course, section);

      if (element.tagName().contentEquals("div")) {
        course = tc.pass(() -> parseCourseHeader(element));
        section = null;

        courses.add(course);
        continue;
      }

      Section nextSection = tc.pass(() -> parseSectionNode(subject, element));

      if (section != null && section.type == SectionType.LEC &&
          nextSection.type != SectionType.LEC) {
        section.recitations.add(nextSection);

        continue;
      }

      section = nextSection;
      course.sections.add(section);
    }

    if (course.sections.isEmpty()) {
      throw new AssertionError(
          "Should be at least one section after parsing course header.");
    }
  }

  private static Course parseCourseHeader(Element divTag) {
    String text = divTag.text(); // MATH-UA 9 - Algebra and Calculus

    int textIndex1 = text.indexOf(' '), textIndex2 = text.indexOf(" - ");
    if (textIndex1 < 0) {
      logger.error("Couldn't parse course header '{}'", text);

      throw new RuntimeException("Couldn't find character ' ' in divTag.text");
    }

    if (textIndex2 < 0) {
      logger.error("Couldn't parse course header '{}'", text);

      throw new RuntimeException(
          "Couldn't find substring \" - \" in divTag.text");
    }

    SubjectCode subject = SubjectCode.fromCode(text.substring(0, textIndex1));
    String deptCourseId = text.substring(textIndex1 + 1, textIndex2);
    String courseName = text.substring(textIndex2 + 3);

    return new Course(courseName, deptCourseId, subject, new ArrayList<>());
  }

  /*
    <a href="https://m.albert.nyu.edu/app/catalog/classsection/NYUNV/1198/8699">
        <i class="ico-arrow-right pull-right right-icon"></i>
        <div class="strong section-body">Section: 001-LEC (8699)</div> <div
    class="section-body">Session: Regular Academic Session</div> <div
    class="section-body">Days/Times: MoWe 9:30am
      - 10:45am</div> <div class="section-body">Dates: 09/03/2019 -
    12/13/2019</div> <div class="section-body">Instructor: Shizhu Liu</div> <div
    class="section-body">Status: Open</div>
    </div> </a>
  */
  private static Section parseSectionNode(SubjectCode subject, Element aTag) {
    Elements children = aTag.select("div.section-content > div.section-body");

    HashMap<String, String> sectionData = new HashMap<>();
    for (Element child : children) {
      String field = child.text();
      int splitIndex = field.indexOf(": ");
      if (splitIndex < 0) {
        logger.debug("Failed to parse '{}' as a section field.", field);
      } else {
        String key = field.substring(0, splitIndex);
        String value = field.substring(splitIndex + 2);

        sectionData.put(key, value);
      }
    }

    logger.trace("Section field strings are: {}", sectionData);

    int registrationNumber;
    String sectionCode;
    SectionType type;
    List<Meeting> meetings;

    try {
      String header = sectionData.get("Section");
      int headerDashIdx = header.indexOf('-');
      registrationNumber = Integer.parseInt(
          header.substring(header.indexOf('(') + 1, header.length() - 1));
      sectionCode = header.substring(0, headerDashIdx);
      type = SectionType.valueOf(header.substring(
          headerDashIdx + 1, header.indexOf(' ', headerDashIdx)));
      meetings = parseSectionTimesData(sectionData.get("Days/Times"),
                                       sectionData.get("Dates"));
    } catch (RuntimeException e) {
      logger.error("parseSectionNode throwing with section data: {}",
                   sectionData);

      throw e;
    }
    return new Section(
        subject, registrationNumber, sectionCode, type,
        SectionStatus.parseStatus(sectionData.get("Status")), meetings,
        new ArrayList<>(),
        sectionData.containsKey("Wait List Total")
            ? Integer.parseInt(sectionData.get("Wait List Total"))
            : null);
  }

  private static List<Meeting> parseSectionTimesData(String times,
                                                     String dates) {
    logger.trace("Parsing section times data...");

    // MoWe 9:30am - 10:45am Fr
    // 2:00pm - 4:00pm Fr 2:00pm - 4:00pm
    Iterator<String> timeTokens = Arrays.asList(times.split(" "))
                                      .stream()
                                      .filter(s -> !s.equals("-"))
                                      .iterator();

    // 09/03/2019 - 12/13/2019 10/11/2019
    // - 10/11/2019 11/08/2019 - 11/08/2019
    Iterator<String> dateTokens = Arrays.asList(dates.split(" "))
                                      .stream()
                                      .filter(s -> !s.equals("-"))
                                      .iterator();

    ArrayList<Meeting> meetings = new ArrayList<>();
    while (timeTokens.hasNext()) {
      if (!dateTokens.hasNext()) {
        logger.error("Time/date was in unexpected format: time='{}', date='{}'",
                     times, dates);

        throw new RuntimeException("Time/date was in unexpected format");
      }

      String beginDays = timeTokens.next();

      if (beginDays.equals("TBA")) {
        if (dateTokens.hasNext())
          dateTokens.next();
        if (dateTokens.hasNext())
          dateTokens.next();

        continue;
      }

      LocalDateTime beginDateTime;
      long duration;
      {
        String beginDateString = dateTokens.next();

        beginDateTime = LocalDateTime.from(timeParser.parse(
            beginDateString + ' ' + timeTokens.next().toUpperCase()));
        LocalDateTime stopDateTime = LocalDateTime.from(timeParser.parse(
            beginDateString + ' ' + timeTokens.next().toUpperCase()));
        duration = ChronoUnit.MINUTES.between(beginDateTime, stopDateTime);
      }

      LocalDateTime endDate =
          LocalDateTime.from(timeParser.parse(dateTokens.next() + " 11:59PM"));

      boolean[] daysList = new boolean[7];
      Arrays.fill(daysList, Boolean.FALSE);
      for (int i = 0; i < beginDays.length() - 1; i += 2) {
        String dayString = beginDays.substring(i, i + 2);
        int dayValue = Utils.parseDayOfWeek(dayString).getValue();

        daysList[dayValue % 7] = true;
      }

      // @TODO Add support for time zone of the campus
      //                          - Albert Liu, Jan 25, 2022 Tue 17:22 EST
      ZoneOffset tz = ZoneOffset.UTC;
      int dayOfWeek = beginDateTime.getDayOfWeek().getValue();
      for (int i = 0; i < 7; i++, dayOfWeek++) {
        if (!daysList[dayOfWeek % 7])
          continue;

        Meeting meeting = new Meeting();
        meeting.beginDate = Timestamp.from(beginDateTime.toInstant(tz));
        meeting.minutesDuration = duration;
        meeting.endDate = Timestamp.from(beginDateTime.toInstant(tz));

        meetings.add(meeting);
      }
    }

    return meetings;
  }
}
