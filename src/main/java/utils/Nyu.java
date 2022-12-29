package utils;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.*;
import database.models.*;
import io.javalin.openapi.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import org.jetbrains.annotations.*;

public final class Nyu {
  // @Note: order is important here. Order MUST be: ja, sp, su, fa
  //                            - Albert Liu, Feb 05, 2022 Sat 02:38 EST
  public enum Semester {
    ja(2),
    sp(4),
    su(6),
    fa(8);

    public final int nyuCode;

    Semester(int nyuCode) {
      this.nyuCode = nyuCode;
    }
  }

  public record Subject(String code, String name) {
    @JsonCreator
    public Subject(@JsonProperty("code") String code,
                   @JsonProperty("name") String name) {
      this.code = code;
      this.name = name;
    }
  }

  public static final class School {
    public final String name;
    public final String code;
    public final ArrayList<Subject> subjects;

    @JsonCreator
    public School(@JsonProperty("name") String name,
                  @JsonProperty("code") String code,
                  @JsonProperty("subjects") ArrayList<Subject> subjects) {
      this.name = name;
      this.code = code;
      this.subjects = subjects;
    }

    public School(String name, String code) {
      this.name = name;
      this.code = code;
      this.subjects = new ArrayList<>();
    }

    @OpenApiExample(value = "College of Arts and Sciences")
    public String getName() {
      return name;
    }

    @OpenApiExample(value = "UA")
    public String getCode() {
      return code;
    }

    public final ArrayList<Subject> getSubjects() {
      return subjects;
    }

    public String toString() {
      return "School(" + name + ")";
    }
  }

  public static final class Course {
    public String name;
    public String deptCourseId;
    public String description;
    public List<Section> sections;
    public String subjectCode;

    public String getName() {
      return name;
    }

    // @Note: these methods are required for the Javalin OpenAPI integration
    // to pick up fields on the output data
    public String getDeptCourseId() {
      return deptCourseId;
    }
    public String getDescription() {
      return description;
    }
    public List<Section> getSections() {
      return sections;
    }
    @OpenApiExample(value = "CSCI-UA")
    public String getSubjectCode() {
      return subjectCode;
    }

    public String toString() {
      return "Course(name=" + name + ",deptCourseId=" + deptCourseId + ")";
    }
  }

  @JsonIgnoreProperties(value = {"endDateLocal", "beginDateLocal"},
                        allowGetters = true)
  public static final class Meeting {
    private static DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ZonedDateTime beginDate; // contains date and time of first event.
    public int minutesDuration;     // Duration of meeting
    public ZonedDateTime endDate;   // When the meeting stops repeating

    // we eventually want to switch to using the `fromCode(String code)` version
    // for the JSON creator. It will happen when we fully move away from
    // scraping from Schedge V1.
    //                                  - Albert Liu, Feb 03, 2022 Thu 01:02 EST
    @JsonCreator
    public static Meeting
    fromJson(@JsonProperty("beginDate") String beginDate,
             @JsonProperty("minutesDuration") int minutesDuration,
             @JsonProperty("endDate") String endDate) {
      var meeting = new Meeting();
      try {
        meeting.beginDate =
            LocalDateTime.parse(beginDate, formatter).atZone(ZoneOffset.UTC);
        meeting.minutesDuration = minutesDuration;
        meeting.endDate =
            LocalDateTime.parse(endDate, formatter).atZone(ZoneOffset.UTC);
      } catch (java.time.format.DateTimeParseException e) {
        meeting.beginDate = parseTime(beginDate);
        meeting.minutesDuration = minutesDuration;
        meeting.endDate = parseTime(endDate);
      }

      return meeting;
    }

    public static ZonedDateTime parseTime(String timeString) {
      var parsed = DateTimeFormatter.ISO_INSTANT.parse(timeString);
      var instant = Instant.from(parsed);
      return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public int getMinutesDuration() {
      return minutesDuration;
    }

    @NotNull
    public String getBeginDate() {
      return DateTimeFormatter.ISO_INSTANT.format(beginDate);
    }

    @NotNull
    public String getEndDate() {
      return DateTimeFormatter.ISO_INSTANT.format(endDate);
    }
  }

  public static class ZonedMeeting {
    public ZonedDateTime beginDate;
    public int minutesDuration;
    public ZonedDateTime endDate;
    private ZoneId tz;

    ZonedMeeting(Meeting meeting, ZoneId tz) {
      this.beginDate = meeting.beginDate;
      this.minutesDuration = meeting.minutesDuration;
      this.endDate = meeting.endDate;
      this.tz = tz;
    }

    public int getMinutesDuration() {
      return minutesDuration;
    }

    @NotNull
    public String getBeginDate() {
      return DateTimeFormatter.ISO_INSTANT.format(beginDate);
    }

    @NotNull
    public String getBeginDateLocal() {
      var beginDateLocal = beginDate.withZoneSameInstant(tz);
      return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(beginDateLocal);
    }

    @NotNull
    public String getEndDate() {
      return DateTimeFormatter.ISO_INSTANT.format(endDate);
    }

    @NotNull
    public String getEndDateLocal() {
      var endDateLocal = endDate.withZoneSameInstant(tz);
      return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(endDateLocal);
    }
  }

  public static final class Section {
    public int registrationNumber;
    public String code;
    public String name;
    public String[] instructors;
    public String type;
    public SectionStatus status;
    public List<Meeting> meetings;
    public List<Section> recitations;
    public Integer waitlistTotal;
    public String instructionMode;
    public String campus;
    public Double minUnits;
    public Double maxUnits;
    public String grading;
    public String location;
    public String notes;

    public static Section fromRow(Row row) {
      Section s = new Section();
      s.name = row.sectionName;
      s.waitlistTotal = row.waitlistTotal;
      s.registrationNumber = row.registrationNumber;
      s.code = row.sectionCode;
      s.instructors = row.instructors;
      s.type = row.sectionType;
      s.status = row.sectionStatus;
      s.meetings = row.meetings;
      s.campus = row.campus;
      s.minUnits = row.minUnits;
      s.maxUnits = row.maxUnits;
      s.instructionMode = row.instructionMode;
      s.grading = row.grading;
      s.notes = row.notes;
      s.location = row.location;

      return s;
    }

    public int getRegistrationNumber() {
      return registrationNumber;
    }

    @JsonInclude(NON_NULL)
    @OpenApiExample(value = "Topic: Natual Language Processing")
    public String getName() {
      return name;
    }

    @NotNull
    public String getCode() {
      return code;
    }

    public String[] getInstructors() {
      return instructors;
    }

    @NotNull
    public String getType() {
      return type;
    }

    @OpenApiPropertyType(definedBy = String.class)
    public SectionStatus getStatus() {
      return status;
    }

    public String getCampus() {
      return campus;
    }

    public List<ZonedMeeting> getMeetings() {
      var output = new ArrayList<ZonedMeeting>();
      var tz = Campus.timezoneForCampus(campus);

      for (var meeting : meetings) {
        var out = new ZonedMeeting(meeting, tz);
        output.add(out);
      }

      return output;
    }

    public String getInstructionMode() {
      return instructionMode;
    }
    public Double getMinUnits() {
      return minUnits;
    }
    public Double getMaxUnits() {
      return maxUnits;
    }
    public String getGrading() {
      return grading;
    }
    public String getLocation() {
      return location;
    }
    public String getNotes() {
      return notes;
    }

    @JsonInclude(NON_NULL)
    public List<Section> getRecitations() {
      return recitations;
    }
    @JsonInclude(NON_NULL)
    public Integer getWaitlistTotal() {
      return waitlistTotal;
    }

    public String toString() {
      return JsonMapper.toJson(this);
    }
  }

  /**
   * Enum class for status of the section based on the availability
   */
  public enum SectionStatus {
    Open,      // Open
    Closed,    // Closed
    WaitList,  // Waitlist
    Cancelled; // Cancelled

    public static SectionStatus parseStatus(String status) {
      if (status.startsWith("Wait List"))
        return WaitList;
      else
        return valueOf(status);
    }

    public boolean isOpen() {
      return this == Open;
    }
  }

  public enum SectionType {
    LEC, // Lecture
    RCT, // Recitation
    LAB, // Lab
    SEM, // Seminar
    PCT, // Practicum
    INT, // Internship
    RSC, // Research Tandon's Code
    FLD, // Field Instruction
    SIM, // Simulation
    LLB, // Lecture for Lab (Tandon)
    DLX, // Distance Learning Hybrid
    CLI, // Clinic
    STU, // Studio
    STI, // Independent Instruction
    STG, // Group Instruction
    CLQ, // Colloquium
    WKS, // Workshop
    IND, // independent study
    PRO, // Project (Tandon)
    GUI, // Guided Study (Tandon)
    NCR, // Non-Credit (Tandon)
    PRP, // Preparatory
    MAM, // Maintaining Marticulation
    DLG,
    NCH,
    NCL, // Non-Credit Lab
    EQV;

    @JsonValue
    public String getName() {
      switch (this) {
      case LEC:
        return "Lecture";
      case RCT:
        return "Recitation";
      case LAB:
        return "Lab";
      case SEM:
        return "Seminar";
      case IND:
        return "Independent Study";
      case SIM:
        return "Simulation";
      case CLI:
        return "Clinic";
      case FLD:
        return "Field Instruction";
      case WKS:
        return "Workshop";
      case STI:
        return "Independent Instruction";
      case STU:
        return "Studio";
      case STG:
        return "Group Instruction";
      case INT:
        return "Internship";
      case RSC:
        return "Research (Tandon)";
      case CLQ:
        return "Colloquium";
      case PRO:
        return "Project (Tandon)";
      case GUI:
        return "Guided Studies (Tandon)";
      case NCR:
        return "Non-Credit (Tandon)";
      case PRP:
        return "Preparatory";
      case MAM:
        return "Maintaining Marticulation";
      case DLX:
        return "Distance Learning Hybrid";
      case PCT:
        return "Practicum";
      case LLB:
        return "Lecture for Lab";
      case NCL:
        return "Non-Credit Lab";
      default:
        return this.toString();
      }
    }
  }

  public record Term(Semester semester, int year) {
    public Term(String sem, int year) {
      this(semesterFromString(sem), year);
    }

    public Term {
      if (year < 1900)
        throw new IllegalArgumentException("Year was invalid: " + year);
    }

    public static Term fromId(int id) {
      // @Note: This requires order of enum variants to be correct
      var semester = Semester.values()[(id % 10) / 2 - 1];

      return new Term(semester, id / 10 + 1900);
    }

    public static Term fromString(String termString) {
      if (termString.contentEquals("current")) {
        return getCurrentTerm();
      }

      if (termString.contentEquals("next")) {
        return getCurrentTerm().nextTerm();
      }

      int year = Integer.parseInt(termString.substring(2));
      return new Term(termString.substring(0, 2), year);
    }

    // @TODO Make this more accurate
    public static Semester getSemester(LocalDateTime time) {
      return switch (time.getMonth()) {
      case JANUARY -> Semester.ja;
      case FEBRUARY, MARCH, APRIL, MAY -> Semester.sp;
      case JUNE, JULY, AUGUST -> Semester.su;
      case SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER -> Semester.fa;
      };
    }

    public static Term getCurrentTerm() {
      LocalDateTime now = LocalDateTime.now();
      int year = now.getYear();

      return new Term(getSemester(now), year);
    }

    public static Semester semesterFromStringNullable(String sem) {
      switch (sem.toLowerCase()) {
      case "ja", "january":
        return Semester.ja;
      case "sp", "spring":
        return Semester.sp;
      case "su", "summer":
        return Semester.su;
      case "fa", "fall":
        return Semester.fa;

      default:
        return null;
      }
    }

    public static Semester semesterFromString(String sem) {
      var semCode = semesterFromStringNullable(sem);
      if (semCode == null)
        throw new IllegalArgumentException("Invalid semester string: " + sem);

      return semCode;
    }

    public Term prevTerm() {
      return switch (semester) {
      case ja -> new Term(Semester.fa, year - 1);
      case sp -> new Term(Semester.ja, year);
      case su -> new Term(Semester.sp, year);
      case fa -> new Term(Semester.su, year);
      };
    }

    public Term nextTerm() {
      return switch (semester) {
      case ja -> new Term(Semester.sp, year);
      case sp -> new Term(Semester.su, year);
      case su -> new Term(Semester.fa, year);
      case fa -> new Term(Semester.ja, year + 1);
      };
    }

    public int getId() {
      return (year - 1900) * 10 + semester.nyuCode;
    }

    @JsonValue
    public String json() {
      return "" + semester + year;
    }
  }

  public static final class Campus {
    public final String name;
    public final String timezoneId;
    public final String timezoneName;

    @JsonIgnore() public final ZoneId timezone;

    public Campus(String name, ZoneId zone) {
      this.name = name;
      this.timezone = zone;
      this.timezoneId = zone.getId();
      this.timezoneName =
          zone.getDisplayName(TextStyle.FULL_STANDALONE, Locale.US);
    }

    public static final HashMap<String, Campus> campuses;

    static {
      var map = new HashMap<String, Campus>();

      var nyc = ZoneId.of("America/New_York");
      var buenosAires = ZoneId.of("America/Argentina/Buenos_Aires");
      var berlin = ZoneId.of("Europe/Berlin");
      var losAngeles = ZoneId.of("America/Los_Angeles");
      var dubai = ZoneId.of("Asia/Dubai");

      var campusList = new Campus[] {
          new Campus("Dublin", ZoneId.of("Europe/Dublin")),
          new Campus("NYU London (Global)", ZoneId.of("Europe/London")),
          new Campus("London", ZoneId.of("Europe/London")),
          new Campus("Ireland", ZoneId.of("Europe/Dublin")),
          new Campus("NYU Paris (Global)", ZoneId.of("Europe/Paris")),
          new Campus("NYU Florence (Global)", ZoneId.of("Europe/Rome")),
          new Campus("NYU Berlin (Global)", berlin),
          new Campus("Berlin", berlin),
          new Campus("Germany", berlin),
          new Campus("NYU Madrid (Global)", ZoneId.of("Europe/Madrid")),
          new Campus("Madrid", ZoneId.of("Europe/Madrid")),
          new Campus("NYU Prague (Global)", ZoneId.of("Europe/Prague")),
          new Campus("Prague", ZoneId.of("Europe/Prague")),
          new Campus("Athens", ZoneId.of("Europe/Athens")),
          new Campus("Greece", ZoneId.of("Europe/Athens")),
          new Campus("Sweden", ZoneId.of("Europe/Stockholm")),

          new Campus("NYU Abu Dhabi (Global)", dubai),
          new Campus("Abu Dhabi", dubai),
          new Campus("Abu Dhabi - Other", dubai),
          new Campus("NYU Tel Aviv (Global)", ZoneId.of("Asia/Tel_Aviv")),
          new Campus("NYU Shanghai (Global)", ZoneId.of("Asia/Shanghai")),
          new Campus("Shanghai", ZoneId.of("Asia/Shanghai")),

          new Campus("NYU Sydney (Global)", ZoneId.of("Australia/Sydney")),

          new Campus("NYU Accra (Global)", ZoneId.of("Africa/Accra")),
          new Campus("Zambia", ZoneId.of("Africa/Lusaka")),

          new Campus("NYU Buenos Aires (Global)", buenosAires),
          new Campus("Buenos Aires", buenosAires),
          new Campus("Dominican Republic", ZoneId.of("America/Santo_Domingo")),
          new Campus("NYU Los Angeles (Global)", losAngeles),
          new Campus("Colombia", ZoneId.of("America/Bogota")),
          new Campus("Cuba", ZoneId.of("America/Havana")),

          // @Note: Brazil has 3 time zones; I have no idea which one is the
          // right one, but the primary time zone of Brazil is Brazil/East so
          // this is a best-effort choice.
          //
          //                              - Albert Liu, Nov 10, 2022 Thu 22:09
          new Campus("Brazil", ZoneId.of("Brazil/East")),

          new Campus("Off Campus", nyc),
          new Campus("Online", nyc),
          new Campus("Distance Learning/Asynchronous", nyc),
          new Campus("Distance Learning / Blended", nyc),
          new Campus("Distance Learning/Synchronous", nyc),
          new Campus("Distance Ed (Learning Space)", nyc),
          new Campus("Distance Education", nyc),

          new Campus("St. Thomas Aquinas College", nyc),
          new Campus("Sarah Lawrence", nyc),
          new Campus("ePoly", nyc),
          new Campus("Medical Center Long Island", nyc),
          new Campus("NYU New York (Global)", nyc),
          new Campus("Wallkill Correctional Facility", nyc),
          new Campus("Mount Sinai Hospital", nyc),

          new Campus("Woolworth Bldg.-15 Barclay St", nyc),
          new Campus("Grad Stern at Purchase", nyc),
          new Campus("Dental Center", nyc),
          new Campus("Midtown Center", nyc),
          new Campus("Hosp. for Joint Diseases", nyc),
          new Campus("2 Broadway, Manhattan", nyc),
          new Campus("Inst. of Fine Arts", nyc),
          new Campus("Medical Center", nyc),
          new Campus("NYU Washington DC (Global)", nyc),
          new Campus("Washington, DC", nyc),
          new Campus("Brooklyn Campus", nyc),
          new Campus("Washington Square", nyc),
      };

      for (var campus : campusList) {
          map.put(campus.name, campus);
        }

        campuses = map;
      }

      public static ZoneId timezoneForCampus(String name) {
        var campus = campuses.get(name);
        if (campus == null) {
          // throw new IllegalArgumentException("Bad campus: " + campus);
          System.err.println("\nBad campus: " + name);
          return ZoneId.of("America/New_York");
        }

        return campus.timezone;
      }

      @NotNull
      @OpenApiExample(value = "NYU Accra (Global)")
      public String getName() {
        return name;
      }

      @NotNull
      @OpenApiExample(value = "Africa/Accra")
      public String getTimezoneId() {
        return timezoneId;
      }

      @NotNull
      @OpenApiExample(value = "Ghana Mean Time")
      public String getTimezoneName() {
        return timezoneName;
      }
    }
  }
