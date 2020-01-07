package services;

import java.time.DayOfWeek;
import java.io.IOException;
import java.util.Arrays;
import java.util.*;
import kotlin.text.StringsKt;
import models.*;
import scraping.models.*;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.UtilsKt;

/**
 * Parses a catalog string in a stream.
 *
 * @author Albert Liu
 */
public class ParseCatalog implements Iterator<Course> {
  private Logger logger;
  private static DateTimeFormatter timeParser =
      DateTimeFormat.forPattern("MM/dd/yyyy h:mma").withLocale(Locale.ENGLISH);
  private Iterator<Element> elements;
  private Element currentElement;

  public static List<Course> parse(Logger logger, String data)
      throws IOException {
    logger.debug("parsing raw catalog data...");
    ArrayList<Course> courses = new ArrayList<>();
    new ParseCatalog(logger, Jsoup.parse(data))
        .forEachRemaining(c -> courses.add(c));
    return courses;
  }

  private ParseCatalog(Logger logger, Document data) throws IOException {
    elements = data.select("div.primary-head ~ *").iterator();
    this.logger = LoggerFactory.getLogger(logger.getName());

    if (!elements.hasNext()) {
      logger.warn("CSS query `div.primary-head ~ *` returned no values.");
      currentElement = null;
      // throw new IOException("models.Course data is empty!");
    } else if (!(currentElement = elements.next()).tagName().equals("div")) {
      logger.error("CSS query `div.primary-head ~ *` returned "
                   + "a list whose first element was not a 'div'.");
      throw new IOException("NYU sent back data we weren't expecting.");
    } else if (currentElement.text().equals(
                   "No classes found matching your criteria.")) {
      currentElement = null; // We're done, nothing's here
    }
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
  SectionMetadata parseSectionNode(Element anchorTag) throws IOException {
    HashMap<String, String> sectionData = sectionFieldTable(
        anchorTag.select("div.section-content > div.section-body"));
    logger.debug("Section field strings are: {}", sectionData);

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
    } catch (Exception e) {
      logger.error("parseSectionNode throwing with section data: {}",
                   sectionData);
      throw e;
    }

    return new SectionMetadata(
        registrationNumber, sectionCode, type, sectionData.get("Instructor"),
        SectionStatus.parseStatus(sectionData.get("Status")), meetings);
  }

  private HashMap<String, String> sectionFieldTable(Elements fields) {
    HashMap<String, String> map = new HashMap<>();
    for (Element child : fields) {
      String field = child.text();
      int splitIndex = field.indexOf(": ");
      if (splitIndex < 0) {
        logger.debug("Failed to parse '{}' as a section field.", field);
      } else {
        map.put(field.substring(0, splitIndex),
                field.substring(splitIndex + 2));
      }
    }
    return map;
  }

  CourseMetadata parseCourseHeader(Element divTag) throws IOException {
    String text = divTag.text(); // MATH-UA 9 - Algebra and Calculus

    int textIndex1 = text.indexOf(' '), textIndex2 = text.indexOf(" - ");
    if (textIndex1 < 0) {
      logger.error("Couldn't parse course header '" + text + "'");
      throw new IOException("Couldn't find character ' ' in divTag.text");
    }
    if (textIndex2 < 0) {
      logger.error("Couldn't parse course header '" + text + "'");
      throw new IOException("Couldn't find substring \" - \" in divTag.text");
    }

    SubjectCode subject =
        SubjectCode.getUnchecked(text.substring(0, textIndex1));
    String deptCourseId = text.substring(textIndex1 + 1, textIndex2);
    String courseName = text.substring(textIndex2 + 3);

    // <div class="secondary-head class-title-header" id=MATHUA9129903>
    int idIndex;
    String idString = divTag.attr("id");
    for (idIndex = 0; idIndex < idString.length(); idIndex++)
      if (Character.isDigit(idString.charAt(idIndex)))
        break;
    // Long courseId = Long.parseLong(idString.substring(idIndex));

    return new CourseMetadata(courseName, deptCourseId, subject);
  }

  List<Meeting> parseSectionTimesData(String times, String dates)
      throws IOException {
    logger.debug("Parsing section times data...");
    // MoWe 9:30am - 10:45am Fr
    // 2:00pm - 4:00pm Fr 2:00pm - 4:00pm

    Iterator<String> timeTokens =
        StringsKt.split(times, new char[] {' '}, false, 0)
            .stream()
            .filter(s -> !s.equals("-"))
            .iterator();

    // 09/03/2019 - 12/13/2019 10/11/2019
    // - 10/11/2019 11/08/2019 - 11/08/2019
    Iterator<String> dateTokens =
        StringsKt.split(dates, new char[] {' '}, false, 0)
            .stream()
            .filter(s -> !s.equals("-"))
            .iterator();

    ArrayList<Meeting> meetings = new ArrayList<>();
    while (timeTokens.hasNext()) {
      if (!dateTokens.hasNext()) {
        logger.error("Time/date was in unexpected format: time='{}', date='{}'",
                     times, dates);
        throw new IOException("Time/date was in unexpected format");
      }

      String beginDays = timeTokens.next();

      if (beginDays.equals("TBA")) {
        safeNext(dateTokens);
        safeNext(dateTokens);
        continue;
      }

      DateTime beginDateTime;
      long duration;
      {
        String beginDateString = dateTokens.next();

        beginDateTime = timeParser.parseDateTime(
            beginDateString + ' ' + timeTokens.next().toUpperCase());
        DateTime stopDateTime = timeParser.parseDateTime(
            beginDateString + ' ' + timeTokens.next().toUpperCase());
        logger.trace("Begin date: {}, End date: {}", beginDateTime,
                     stopDateTime);
        long durationMillis =
            stopDateTime.getMillis() - beginDateTime.getMillis();
        duration = durationMillis / 60000;
        logger.trace("Duration of meeting is {} minutes", duration);
      }

      DateTime endDate =
          timeParser.parseDateTime(dateTokens.next() + " 11:59PM");

      boolean[] daysList = new boolean[7];
      Arrays.fill(daysList, Boolean.FALSE);
      for (int i = 0; i < beginDays.length() - 1; i += 2) {
        String dayString = beginDays.substring(i, i + 2);
        int dayValue = UtilsKt.parseDayOfWeek(dayString).getValue();
        logger.trace("day: {} translates to ", dayString, dayValue);
        daysList[dayValue % 7] = true;
      }

      for (int day = 0; day < 7;
           day++, beginDateTime = beginDateTime.plusDays(1)) {
        if (daysList[beginDateTime.getDayOfWeek() % 7]) {
          meetings.add(new Meeting(beginDateTime, duration, endDate));
        }
      }
    }
    logger.trace("Meetings are: {}", meetings);

    return meetings;
  }

  @Override
  public boolean hasNext() {
    return currentElement != null;
  }

  @Override
  @NotNull
  public Course next() {

    if (!hasNext())
      throw new NoSuchElementException("No more elements in the iterator!");

    ArrayList<SectionMetadata> sections = new ArrayList<>();
    CourseMetadata course;

    try {
      course = parseCourseHeader(currentElement);
    } catch (IOException e) {
      logger.error("parseCourseHeader threw with node={}", currentElement);
      throw new RuntimeException(e);
    }

    if (!elements.hasNext())
      throw new AssertionError(
          "Should be at least one section after parsing course header.");

    while (elements.hasNext() &&
           !(currentElement = elements.next()).tagName().equals("div")) {
      try {
        sections.add(parseSectionNode(currentElement));
      } catch (Exception e) {
        logger.error("parseSectionNode threw with course={}", course);
        throw new RuntimeException(e);
      }
    }

    if (!elements.hasNext())
      currentElement = null;

    return course.getCourse(sections);
  }

  private static <T> Optional<T> safeNext(Iterator<T> iterator) {
    if (iterator.hasNext()) {
      return Optional.of(iterator.next());
    } else {
      return Optional.empty();
    }
  }

  /**
   * Metadata for a single section, without nesting of sections.
   *
   * @author Albert Liu
   */
  private static class SectionMetadata {
    private int registrationNumber;
    private String sectionCode;
    private SectionType type;
    private String instructor;
    private SectionStatus status;
    private List<Meeting> meetings;

    public SectionMetadata(int registrationNumber, String sectionCode,
                           SectionType type, String instructor,
                           SectionStatus status, List<Meeting> meetings) {
      this.registrationNumber = registrationNumber;
      this.sectionCode = sectionCode;
      this.type = type;
      this.instructor = instructor;
      this.status = status;
      this.meetings = meetings;
    }

    @NotNull
    static ArrayList<Section>
    getSectionsFrom(ArrayList<SectionMetadata> sectionData, SubjectCode code) {
      ArrayList<Section> sections = new ArrayList<>();

      int size = sectionData.size();
      for (int cursor = 0; cursor < size; cursor++) {
        SectionMetadata current = sectionData.get(cursor);
        if (current.type == SectionType.LEC) {
          ArrayList<Section> recitations = new ArrayList<>();
          for (int nestedCursor = cursor + 1;
               nestedCursor < size &&
               sectionData.get(nestedCursor).type != SectionType.LEC;
               nestedCursor++) {
            recitations.add(
                sectionData.get(nestedCursor).toSectionWithoutRecitations());
          }

          if (recitations.size() > 0) {
            sections.add(current.toLectureWithRecitations(recitations));
            cursor += recitations.size();
          } else {
            sections.add(current.toSectionWithoutRecitations());
          }
        } else {
          sections.add(current.toSectionWithoutRecitations());
        }
      }

      return sections;
    }

    Section toLectureWithRecitations(ArrayList<Section> recitations) {
      return new Section(registrationNumber, sectionCode, instructor,
                         SectionType.LEC, status, meetings, recitations);
    }

    Section toSectionWithoutRecitations() {
      return new Section(registrationNumber, sectionCode, instructor, type,
                         status, meetings, null);
    }
  }

  /**
   * Metadata for a single course, without sections.
   *
   * @author Albert Liu
   */
  private static class CourseMetadata {

    private String courseName;
    private String deptCourseId;
    private SubjectCode subject;

    CourseMetadata(String courseName, String deptCourseId,
                   SubjectCode subject) {
      this.courseName = courseName;
      this.deptCourseId = deptCourseId;
      this.subject = subject;
    }

    @NotNull
    Course getCourse(ArrayList<SectionMetadata> sections) {
      return new Course(courseName, deptCourseId, subject,
                        SectionMetadata.getSectionsFrom(sections, subject));
    }

    @Override
    public String toString() {
      return "CourseData(courseName=" +
          courseName + // ", courseId=" + courseId +
          ", deptCourseId=" + deptCourseId + ")";
    }
  }
}
