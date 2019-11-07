package services;

import java.io.IOException;
import java.util.*;
import kotlin.text.StringsKt;
import models.*;
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

/**
 * Parses a catalog string in a stream.
 *
 * @author Albert Liu
 */
public class ParseCatalog implements Iterator<Course> {
  private Logger logger;
  private static DateTimeFormatter timeParser =
      DateTimeFormat.forPattern("MM/dd/yyyy h:mma");
  private Iterator<Element> elements;
  private Element currentElement;

  public static List<Course> parse(Logger logger, String data)
      throws IOException {
    ArrayList<Course> courses = new ArrayList<>();
    new ParseCatalog(logger, Jsoup.parse(data))
        .forEachRemaining(c -> courses.add(c));
    return courses;
  }

  private ParseCatalog(Logger logger, Document data) throws IOException {
    elements = data.select("div.primary-head ~ *").iterator();
    this.logger = LoggerFactory.getLogger(logger.getName());

    if (!elements.hasNext()) {
      logger.error("CSS query `div.primary-head ~ *` returned no values.");
      throw new IOException("models.Course data is empty!");
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

    return new SectionMetadata(
        registrationNumber, sectionNumber, type, sectionData.get("Instructor"),
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
    int deptCourseNumber =
        Integer.parseInt(text.substring(textIndex1 + 1, textIndex2));
    String courseName = text.substring(textIndex2 + 3);

    // <div class="secondary-head class-title-header" id=MATHUA9129903>
    int idIndex;
    String idString = divTag.attr("id");
    for (idIndex = 0; idIndex < idString.length(); idIndex++)
      if (Character.isDigit(idString.charAt(idIndex)))
        break;
    Long courseId = Long.parseLong(idString.substring(idIndex));

    return new CourseMetadata(courseId, courseName, deptCourseNumber, subject);
  }

  List<Meeting> parseSectionTimesData(String times, String dates)
      throws IOException {
    logger.debug("Parsing section times data...");
    // MoWe 9:30am - 10:45am Fr
    // 2:00pm - 4:00pm Fr 2:00pm - 4:00pm

    // 09/03/2019 - 12/13/2019 10/11/2019
    // - 10/11/2019 11/08/2019 - 11/08/2019
    Iterator<String> timeTokens =
        StringsKt.split(times, new char[] {' '}, false, 0)
            .stream()
            .filter(s -> !s.equals("-"))
            .iterator();

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
      Duration duration;
      {
        String beginDateString = dateTokens.next();

        beginDateTime = timeParser.parseDateTime(
            beginDateString + ' ' + timeTokens.next().toUpperCase());
        long durationMillis =
            timeParser
                .parseDateTime(beginDateString + ' ' +
                               timeTokens.next().toUpperCase())
                .getMillis() -
            beginDateTime.getMillis();
        logger.trace("Duration of meeting is {}", durationMillis);
        duration = new Duration(durationMillis / 6000);
      }

      Duration activeDuration;
      {
        DateTime endDate =
            timeParser.parseDateTime(dateTokens.next() + " 11:59PM");
        long activeDurationMillis =
            endDate.getMillis() - beginDateTime.getMillis();
        if (activeDurationMillis < 0)
          throw new AssertionError("Active duration should be positive!");
        logger.trace("Active duration of meeting is {}", activeDurationMillis);
        activeDuration = new Duration(activeDurationMillis / 6000);
      }

      Boolean[] daysList = (new Days(beginDays)).toDayNumberArray();
      logger.trace("{}", (Object)daysList);

      for (int day = 0; day < 7;
           day++, beginDateTime = beginDateTime.plusDays(
                      1)) { // TODO fix this code to do the right thing
        if (daysList[beginDateTime.getDayOfWeek() - 1]) {
          Duration dayAdjustedActiveDuration =
              activeDuration.minus(Duration.standardDays(day));
          logger.trace("Day adjusted duration of meeting is {}",
                       dayAdjustedActiveDuration);
          meetings.add(
              new Meeting(beginDateTime, duration, dayAdjustedActiveDuration));
        }
      }
    }

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
}

/**
 * Metadata for a single section, without nesting of sections.
 *
 * @author Albert Liu
 */
class SectionMetadata {
  private int registrationNumber;
  private int sectionNumber;
  private SectionType type;
  private String instructor;
  private SectionStatus status;
  private List<Meeting> meetings;

  public SectionMetadata(int registrationNumber, int sectionNumber,
                         SectionType type, String instructor,
                         SectionStatus status, List<Meeting> meetings) {
    this.registrationNumber = registrationNumber;
    this.sectionNumber = sectionNumber;
    this.type = type;
    this.instructor = instructor;
    this.status = status;
    this.meetings = meetings;
  }

  @NotNull
  static ArrayList<Section>
  getSectionsFrom(ArrayList<SectionMetadata> sectionData) {
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
        }
      } else {
        sections.add(current.toSectionWithoutRecitations());
      }
    }

    return sections;
  }

  Section toLectureWithRecitations(ArrayList<Section> recitations) {
    return new Section.Lecture(registrationNumber, sectionNumber, instructor,
                               status, meetings, recitations);
  }

  Section toSectionWithoutRecitations() {
    return Section.getSection(registrationNumber, sectionNumber, instructor,
                              type, status, meetings, null);
  }
}

/**
 * Metadata for a single course, without sections.
 *
 * @author Albert Liu
 */
class CourseMetadata {

  private String courseName;
  private long courseId;
  private int deptCourseNumber;
  private SubjectCode subject;

  CourseMetadata(long courseId, String courseName, int deptCourseNumber,
                 SubjectCode subject) {
    this.courseName = courseName;
    this.courseId = courseId;
    this.deptCourseNumber = deptCourseNumber;
    this.subject = subject;
  }

  @NotNull
  Course getCourse(ArrayList<SectionMetadata> sections) {
    return new Course(courseId, courseName, deptCourseNumber, subject,
                      SectionMetadata.getSectionsFrom(sections));
  }

  @Override
  public String toString() {
    return "CourseData(courseName=" + courseName + ", courseId=" + courseId +
        ", deptCourseNumber=" + deptCourseNumber + ")";
  }
}
