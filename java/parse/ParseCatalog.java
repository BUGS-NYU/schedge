package parse;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import kotlin.text.StringsKt;
import models.*;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseCatalog {

  private static Logger logger = LoggerFactory.getLogger("parse.catalog");
  private static Logger meetingsLogger =
      LoggerFactory.getLogger("parse.catalog.meetings");
  private static DateTimeFormatter timeParser =
      DateTimeFormat.forPattern("MM/dd/yyyy h:mma");
  // 09/03/2019 3:15P

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
      throw new IOException("models.Course data is empty!");
    } else if (!elementList.get(0).tagName().equals("div")) {
      logger.error(
          "CSS query `div.primary-head ~ *` returned a list whose first element was not a 'div'.");
      throw new IOException("NYU sent back data we weren't expecting.");
    }

    ArrayList<CatalogEntry> output = new ArrayList<>();
    CourseMetadata current = parseCourseNode(elementList.get(0));
    if (current == null) { // There were no classes
      return output;
    }
    ArrayList<CatalogSectionEntry> sections = new ArrayList<>();
    CatalogSectionEntry lectureEntry = null;

    for (int i = 1; i < elementList.size(); i++) {
      Element element = elementList.get(i);
      if (element.tagName().equals("div")) {
        output.add(current.getCatalogEntry(sections));
        current = parseCourseNode(element);
        sections = new ArrayList<>();
        lectureEntry = null;
      } else {
        CatalogSectionEntry entry;
        try {
          entry = parseSectionNode(element, lectureEntry);
        } catch (Exception e) {
          logger.error("parseSectionNode threw with course={}", current);
          throw e;
        }
        if (entry.getType() == SectionType.LEC) {
          lectureEntry = entry;
        }
        sections.add(entry);
      }
    }
    return output;
  }

  private static CourseMetadata parseCourseNode(Element divTag)
      throws IOException {
    String text = divTag.text(); // MATH-UA 9 - Algebra and Calculus
    if (text.equals("No classes found matching your criteria."))
      return null;

    int textIndex1 = text.indexOf(' '), textIndex2 = text.indexOf(" - ");
    if (textIndex1 < 0) {
      logger.error("Couldn't parse course header '" + text + "'");
      throw new IOException("Couldn't find character ' ' in divTag.text");
    }
    if (textIndex2 < 0) {
      logger.error("Couldn't parse course header '" + text + "'");
      throw new IOException("Couldn't find substring \" - \" in divTag.text");
    }

    String subject = text.substring(0, textIndex1);
    Long deptCourseNumber =
        Long.parseLong(text.substring(textIndex1 + 1, textIndex2));
    String courseName = text.substring(textIndex2 + 3);

    // <div class="secondary-head class-title-header" id=MATHUA9129903>
    int idIndex;
    String idString = divTag.attr("id");
    for (idIndex = 0; idIndex < idString.length(); idIndex++)
      if (Character.isDigit(idString.charAt(idIndex)))
        break;
    Long courseId = Long.parseLong(idString.substring(idIndex));

    return new CourseMetadata(courseName, subject, courseId, deptCourseNumber);
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
  private static CatalogSectionEntry
  parseSectionNode(Element anchorTag, CatalogSectionEntry associatedWith)
      throws IOException {
    HashMap<String, String> sectionData = new HashMap<>();

    { // TODO Needs to be parsed using hashtable, not indices
      Elements dataDivChildren =
          anchorTag.select("div.section-content > div.section-body");

      for (Element child : dataDivChildren) {
        addSectionFieldString(sectionData, child.text());
      }
      logger.trace("Section field strings are: {}", sectionData);
    }

    int registrationNumber, sectionNumber;
    SectionType type;

    try {
      String header = sectionData.get("Section");
      int headerDashIdx = header.indexOf('-');
      registrationNumber = Integer.parseInt(
          header.substring(header.indexOf('(') + 1, header.length() - 1));
      sectionNumber = Integer.parseInt(header.substring(0, headerDashIdx));
      type = SectionType.valueOf(header.substring(
          headerDashIdx + 1, header.indexOf(' ', headerDashIdx)));
    } catch (Exception e) {
      logger.error("parseSectionNode throwing with section data: {}",
                   sectionData);
      throw e;
    }

    List<Meeting> meetings = parseSectionTimesData(
        sectionData.get("Days/Times"), sectionData.get("Dates"));

    return new CatalogSectionEntry(
        registrationNumber, sectionNumber, type, sectionData.get("Instructor"),
        type == SectionType.LEC ? null : associatedWith,
        SectionStatus.parseStatus(sectionData.get("Status")), meetings);
  }

  private static void
  addSectionFieldString(HashMap<String, String> sectionFieldData,
                        String field) {
    int splitIndex = field.indexOf(": ");
    if (splitIndex < 0) {
      logger.debug("Failed to parse '" + field + "' as a section field.");
      return;
    }
    sectionFieldData.put(field.substring(0, splitIndex),
                         field.substring(splitIndex + 2));
  }

  private static List<Meeting> parseSectionTimesData(String times, String dates)
      throws IOException {
    meetingsLogger.debug("Parsing section times data...");
    if (times.equals("TBA"))
      return new ArrayList<>();
    // MoWe 9:30am - 10:45am Fr
    // 2:00pm - 4:00pm Fr 2:00pm - 4:00pm

    // 09/03/2019 - 12/13/2019 10/11/2019
    // - 10/11/2019 11/08/2019 - 11/08/2019
    // TODO Optimize this
    List<String> timeTokens = new ArrayList<>(), dateTokens = new ArrayList<>();
    StringsKt.split(times, new char[] {' '}, false, 0)
        .stream()
        .filter(s -> !s.equals("-"))
        .forEach(s -> timeTokens.add(s));

    StringsKt.split(dates, new char[] {' '}, false, 0)
        .stream()
        .filter(s -> !s.equals("-"))
        .forEach(s -> dateTokens.add(s));

    if (dateTokens.size() / 2 * 3 != timeTokens.size()) {
      meetingsLogger.error(
          "Time/date was in unexpected format: time='{}', date='{}'", times,
          dates);
      throw new IOException("Time/date was in unexpected format");
    }

    ArrayList<Meeting> meetings = new ArrayList<>();

    for (int idx = 0; idx < timeTokens.size() / 3; idx++) {
      meetingsLogger.debug("Tokens are: time='{}' date='{}'", timeTokens,
                           dateTokens);
      int timesIdx = idx * 3, datesIdx = idx * 2;

      DateTime beginDate =
          timeParser.parseDateTime(dateTokens.get(datesIdx) + ' ' +
                                   timeTokens.get(timesIdx + 1).toUpperCase());

      DateTime endTime =
          timeParser.parseDateTime(dateTokens.get(datesIdx) + ' ' +
                                   timeTokens.get(timesIdx + 2).toUpperCase());

      long durationMillis = endTime.getMillis() - beginDate.getMillis();
      meetingsLogger.debug("Duration of meeting is {}", durationMillis);
      Duration duration = Duration.millis(durationMillis);

      DateTime endDate =
          timeParser.parseDateTime(dateTokens.get(datesIdx + 1) + " 11:59PM");

      long activeDurationMillis = endDate.getMillis() - beginDate.getMillis();
      if (activeDurationMillis < 0)
        throw new AssertionError("Active duration should be positive!");
      meetingsLogger.debug("Active duration of meeting is {}",
                           activeDurationMillis);
      Duration activeDuration = Duration.millis(activeDurationMillis);

      List<DayOfWeek> daysList =
          (new Days(timeTokens.get(timesIdx))).toDayOfWeekList();
      meetingsLogger.debug("{}", daysList);

      for (int day = 0; day < 7;
           day++, beginDate = beginDate.plusDays(
                      1)) { // TODO fix this code to do the right thing
        if (daysList.contains(DayOfWeek.of(beginDate.getDayOfWeek()))) {
          Duration dayAdjustedActiveDuration =
              activeDuration.minus(Duration.standardDays(day));
          meetingsLogger.debug("Day adjusted duration of meeting is {}",
                               dayAdjustedActiveDuration);
          meetings.add(
              new Meeting(beginDate, duration, dayAdjustedActiveDuration));
        }
      }
    }

    return meetings;
  }

  private static class CourseMetadata {
    private String courseName;
    private String subject;
    private Long courseId;
    private Long deptCourseNumber;

    CourseMetadata(String courseName, String subject, Long courseId,
                   Long deptCourseNumber) {
      this.courseName = courseName;
      this.subject = subject;
      this.courseId = courseId;
      this.deptCourseNumber = deptCourseNumber;
    }

    private CatalogEntry
    getCatalogEntry(ArrayList<CatalogSectionEntry> sections) {
      return new CatalogEntry(this.courseName, this.subject, this.courseId,
                              this.deptCourseNumber, sections);
    }

    @Override
    public String toString() {
      return "Course(courseName=" + courseName + ", subject=" + subject +
          ", courseId=" + courseId + ", deptCourseNumber=" + deptCourseNumber +
          ")";
    }
  }
}
