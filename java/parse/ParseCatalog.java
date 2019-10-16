package parse;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import kotlin.text.StringsKt;
import models.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseCatalog {

  static Logger logger = LoggerFactory.getLogger("parse.catalog");
  static DateTimeFormatter timeParser =
      DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mma", Locale.US);

  /**
   * Get formatted course data from a catalog query result.
   */
  public static List<CatalogEntry> parse(Document data) throws IOException {
    Elements elementList = data.select("div.primary-head ~ *");
    if (elementList == null) {
      logger.error("CSS query `div.primary-head ~ *` returned a null value.");
      throw new IOException("xml.select returned null");
    } else if (elementList.size() == 0) {
      logger.error("CSS query `div.primary-head ~ *` returned an empty list.");
      throw new IOException("Course data is empty!");
    } else if (!elementList.get(0).tagName().equals("div")) {
      logger.error(
          "CSS query `div.primary-head ~ *` returned a list whose first element was not a 'div'.");
      throw new IOException("NYU sent back data we weren't expecting.");
    }

    ArrayList<CatalogEntry> output = new ArrayList<>();
    Course current = parseCourseNode(elementList.get(0));
    ArrayList<CatalogSectionEntry> sections = new ArrayList<>();

    for (int i = 1; i < elementList.size(); i++) {
      Element e = elementList.get(i);
      if (e.tagName().equals("div")) {
        output.add(current.getCatalogEntry(sections));
        current = parseCourseNode(e);
        sections = new ArrayList<>();
      } else {
        sections.add(parseSectionNode(e));
      }
    }
    return output;
  }

  public static Course parseCourseNode(Element divTag) throws IOException {
    String text = divTag.text(); // MATH-UA 9 - Algebra and Calculus
    int textIndex1 = text.indexOf(' '), textIndex2 = text.indexOf(" - ");
    if (textIndex1 < 0)
      throw new IOException("Couldn't find character ' ' in divTag.text");
    if (textIndex2 < 0)
      throw new IOException("Couldn't find substring \" - \" in divTag.text");

    String subject = text.substring(0, textIndex1);
    Integer deptCourseNumber =
        Integer.parseInt(text.substring(textIndex1, textIndex2));
    String courseName = text.substring(textIndex2 + 3);

    // <div class="secondary-head class-title-header" id=MATHUA9129903>
    int idIndex;
    String idString = divTag.attr("id");
    for (idIndex = 0; idIndex < idString.length(); idIndex++)
      if (idString.charAt(idIndex) <= '9' && idString.charAt(idIndex) >= '0')
        break;
    Integer courseId =
        Integer.parseInt(idString.substring(idIndex, idString.length()));

    return new Course(courseName, subject, courseId, deptCourseNumber);
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
  public static CatalogSectionEntry parseSectionNode(Element anchorTag)
      throws IOException {
    String header, times, dates, instructor, status;

    {
      Elements dataDivChildren =
          anchorTag.select("div.section-content > div.section-body");

      // <div class="strong section-body">Section: 001-LEC (7953)</div>
      header = getSectionFieldString(dataDivChildren.get(0).text(), "header");

      // <div class="section-body"  >Days/Times:  MoWe 9:30am - 10:45am Fr
      // 2:00pm - 4:00pm Fr 2:00pm - 4:00pm</div>
      times = getSectionFieldString(dataDivChildren.get(2).text(), "times");

      // <div class="section-body"  >Dates:  09/03/2019 - 12/13/2019 10/11/2019
      // - 10/11/2019 11/08/2019 - 11/08/2019</div>
      dates = getSectionFieldString(dataDivChildren.get(3).text(), "dates");

      // <div class="section-body"  >Instructor: David H A Fitch, Stephen J
      // Small</div>
      instructor =
          getSectionFieldString(dataDivChildren.get(4).text(), "instructor");

      // <div class="section-body"  >Status: Open</div>
      status = getSectionFieldString(dataDivChildren.get(5).text(), "status");
    }

    Integer registrationNumber, sectionNumber;
    SectionType type;

    {
      int headerDashIdx = header.indexOf('-');
      registrationNumber = Integer.parseInt(
          header.substring(header.indexOf('('), header.length() - 1));
      sectionNumber = Integer.parseInt(header.substring(0, headerDashIdx));
      type = SectionType.valueOf(
          header.substring(headerDashIdx, header.indexOf(' ', headerDashIdx)));
    }

    List<Meeting> meetings = parseSectionTimesData(times, dates);

    return new CatalogSectionEntry(registrationNumber, sectionNumber, type,
                                   instructor, meetings);
  }

  public static String getSectionFieldString(String field, String fieldName)
      throws IOException {
    int splitIndex = field.indexOf(": ");
    if (splitIndex < 0) {
      logger.error("Failed to parse `" + fieldName +
                   "` field: couldn't find substring ': '");
      IOException except =
          new IOException("Got a bad string parsing " + fieldName + " field.");
      except.setStackTrace(Arrays.copyOfRange(except.getStackTrace(), 1,
                                              except.getStackTrace().length));
      throw except;
    }

    return field.substring(splitIndex);
  }

  public static List<Meeting> parseSectionTimesData(String times, String dates)
      throws IOException {
    // MoWe 9:30am - 10:45am Fr
    // 2:00pm - 4:00pm Fr 2:00pm - 4:00pm

    // 09/03/2019 - 12/13/2019 10/11/2019
    // - 10/11/2019 11/08/2019 - 11/08/2019
    // TODO Optimize this
    List<String> timeStringTokens =
        StringsKt.split(times, new char[] {' '}, false, 0);
    timeStringTokens.removeIf((str) -> str.equals("-"));
    List<String> dateStringTokens =
        StringsKt.split(dates, new char[] {' '}, false, 0);
    dateStringTokens.removeIf((str) -> str.equals("-"));

    if (dateStringTokens.size() / 2 * 3 != timeStringTokens.size()) {
      logger.error("Time/date was in unexpected format: time: '{}', date: '{}'",
                   times, dates);
      throw new IOException("Time/date was in unexpected format");
    }

    ArrayList<Meeting> meetings = new ArrayList<>();

    for (int i = 0; i < timeStringTokens.size() / 3; i++) {
      int timesIdx = i * 3, datesIdx = i * 2;
      Days days = new Days(timeStringTokens.get(timesIdx));
      LocalDateTime beginDate = LocalDateTime.from(
          timeParser.parse(dateStringTokens.get(datesIdx) + ' ' +
                           timeStringTokens.get(timesIdx + 1).toUpperCase()));
      LocalDateTime endTime = LocalDateTime.from(
          timeParser.parse(dateStringTokens.get(datesIdx) + ' ' +
                           timeStringTokens.get(timesIdx + 2).toUpperCase()));
      Duration duration = Duration.ofMinutes(
          ChronoUnit.MINUTES.between(beginDate.toInstant(ZoneOffset.UTC),
                                     endTime.toInstant(ZoneOffset.UTC)));
      LocalDateTime endDate = LocalDateTime.from(
          timeParser.parse(dateStringTokens.get(datesIdx + 1) + " 11:59PM"));

      { // Make sure beginDate is actually on the first day
        List<DayOfWeek> daysList = days.toDayOfWeekList();
        while (!daysList.contains(beginDate.getDayOfWeek()))
          beginDate.plusDays(1);
      }

      Duration activeDuration = Duration.ofMinutes(
          ChronoUnit.MINUTES.between(beginDate.toInstant(ZoneOffset.UTC),
                                     endDate.toInstant(ZoneOffset.UTC)));
      meetings.add(new Meeting(beginDate, duration, activeDuration, days));
    }

    return meetings;
  }

  private static class Course {
    private String courseName;
    private String subject;
    private Integer courseId;
    private Integer deptCourseNumber;

    Course(String courseName, String subject, Integer courseId,
           Integer deptCourseNumber) {
      this.courseName = courseName;
      this.subject = subject;
      this.courseId = courseId;
      this.deptCourseNumber = deptCourseNumber;
    }

    public CatalogEntry
    getCatalogEntry(ArrayList<CatalogSectionEntry> sections) {
      return new CatalogEntry(this.courseName, this.subject, this.courseId,
                              this.deptCourseNumber, sections);
    }
  }
}
