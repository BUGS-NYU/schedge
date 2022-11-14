package scraping;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.slf4j.*;
import utils.*;

class PSCoursesParser {
  static Logger logger = PeopleSoftClassSearch.logger;

  static DateTimeFormatter timeParser =
      DateTimeFormatter.ofPattern("MM/dd/yyyy h.mma", Locale.ENGLISH);

  static ArrayList<Nyu.School>
  translateSubjects(ArrayList<PeopleSoftClassSearch.SubjectElem> raw) {
    var schools = new ArrayList<Nyu.School>();
    Nyu.School school = null;

    for (var subject : raw) {
      if (school == null || !school.name.equals(subject.schoolName)) {
        school = new Nyu.School(subject.schoolName);
        schools.add(school);
      }

      school.subjects.add(new Nyu.Subject(subject.code, subject.name));
    }

    return schools;
  }

  static ArrayList<Nyu.Course> parseSubject(Try ctx, String html,
                                            String subjectCode) {
    var doc = Jsoup.parse(html);

    {
      var field = doc.expectFirst("#win0divPAGECONTAINER");
      var cdata = (CDataNode)field.textNodes().get(0);
      doc = Jsoup.parse(cdata.text());
    }

    {
      var resultCountElem = doc.expectFirst("#RESULT_COUNTlbl");
      var resultCountElemText = resultCountElem.text();
      ctx.put("countElemText", resultCountElemText);
      var parts = resultCountElemText.split(" \\| ");
      var countText = parts[1].split(": ");
      ctx.put("countText", countText[1]);
      var count = Integer.parseInt(countText[1].trim());
      if (count == 0) {
        // There's no document to parse because there's no courses
        return new ArrayList<>();
      }
    }

    var coursesContainer = doc.expectFirst("div[id=win0divSELECT_COURSE$0]");

    var courses = new ArrayList<Nyu.Course>();
    for (var courseHtml : coursesContainer.children()) {
      var course = parseCourse(ctx, courseHtml, subjectCode);
      courses.add(course);
    }

    return courses;
  }

  static Nyu.Course parseCourse(Try ctx, Element courseHtml,
                                String subjectCode) {
    var course = new Nyu.Course();

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

    var matchingSubject = course.subjectCode.contentEquals(subjectCode);
    if (!matchingSubject) {
      // This isn't an error for something like `SCA-UA`/`SCA-UA_1`, but
      // could be different than expected for other schools,
      // so for now we just log it.
      //                  - Albert Liu, Oct 16, 2022 Sun 13:43
      var isSCA = subjectCode.startsWith("SCA-UA");
      if (!isSCA) {
        logger.warn(course.name + " - course.subjectCode=" +
                    course.subjectCode + ", but subject=" + subjectCode);
      }
    }

    course.subjectCode = subjectCode;

    Nyu.Section lecture = null;
    for (var sectionHtml : sections.subList(1, sections.size())) {
      var section = parseSection(ctx, sectionHtml);
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

  static Nyu.Section parseSection(Try ctx, Element sectionHtml) {
    var wrapper = sectionHtml.expectFirst("td");
    var data = wrapper.children();

    var section = new Nyu.Section();
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
      s.status = Nyu.SectionStatus.parseStatus(status);

      if (s.status == Nyu.SectionStatus.WaitList) {
        var waitlistString = status.replaceAll("[^0-9]", "");
        s.waitlistTotal = Integer.parseInt(waitlistString);
      }
    }

    // 01/25/2021 - 05/14/2021 Thu 6.15 PM - 7.30 PM at SPR 2503 with
    // Morrison, George Arthur; Margarint, Vlad-Dumitru
    String metaText = "";

    var notes = new StringBuilder();
    for (var node : wrapper.textNodes()) {
      var text = node.text().trim();
      if (text.length() == 0)
        continue;

      if (metaText.isEmpty()) {
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

    ctx.put("meetingString", parts[0]);

    parts = parts[0].split(" ");
    if (parts.length <= 3) {
      return section; // No meetings
    }

    int tokenIdx = 0;
    int duration;
    LocalDateTime beginDateTime, endDateTime;
    DayOfWeek[] days;

    // Default pattern:
    //      01/25/2021 - 05/14/2021 Thu 6.15 PM - 7.30 PM
    // Asynchronous classes:
    //      01/25/2021 - 05/14/2021 Thu
    // Some online classes also have:
    //      01/25/2021 - 05/14/2021 6.15 PM - 7.30 PM
    {
      var beginDateStr = parts[tokenIdx];
      tokenIdx += 2;

      var endDateStr = parts[tokenIdx];
      tokenIdx += 1;

      var dayStr = parts[tokenIdx];
      if (dayStr.charAt(0) >= '0' && dayStr.charAt(0) <= '9') {
        dayStr = "Sun,Sat,Mon,Tue,Wed,Thu,Fri";
      } else {
        tokenIdx += 1;
      }

      var dayStrings = dayStr.split(",");
      days = new DayOfWeek[dayStrings.length];
      for (int i = 0; i < dayStrings.length; i++) {
        days[i] = Utils.parseDayOfWeek(dayStrings[i]);
      }

      // @Note: Asynchronous classes have the pattern:
      //
      //            01/25/2021 - 05/14/2021 Thu
      //
      // Since the time doesn't really matter anyways, we just substitute
      // in the time of 11.59PM .
      //
      //                    - Albert Liu, Nov 03, 2022 Thu 18:35
      String beginTimeStr, endTimeStr;
      if (tokenIdx >= parts.length) {
        beginTimeStr = endTimeStr = "11.59PM";
      } else {
        beginTimeStr = parts[tokenIdx] + parts[tokenIdx + 1];
        tokenIdx += 3;
        endTimeStr = parts[tokenIdx] + parts[tokenIdx + 1];
      }

      beginDateTime = LocalDateTime.from(
          timeParser.parse(beginDateStr + ' ' + beginTimeStr));
      var stopDateTime =
          LocalDateTime.from(timeParser.parse(beginDateStr + ' ' + endTimeStr));
      duration = (int)ChronoUnit.MINUTES.between(beginDateTime, stopDateTime);

      endDateTime =
          LocalDateTime.from(timeParser.parse(endDateStr + " 11.59PM"));
    }

    var tz = Nyu.Campus.timezoneForCampus(section.campus);

    beginDateTime = beginDateTime.atZone(tz)
                        .withZoneSameInstant(ZoneOffset.UTC)
                        .toLocalDateTime();

    endDateTime = endDateTime.atZone(tz)
                      .withZoneSameInstant(ZoneOffset.UTC)
                      .toLocalDateTime();

    for (var day : days) {
      var meeting = new Nyu.Meeting();
      var adjuster = TemporalAdjusters.nextOrSame(day);
      meeting.beginDate = beginDateTime.with(adjuster);
      meeting.minutesDuration = duration;
      meeting.endDate = endDateTime;

      section.meetings.add(meeting);
    }

    return section;
  }
}
