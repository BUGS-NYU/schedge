package utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import database.models.FullRow;
import database.models.Row;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class Nyu {
  // @Note: order is important here. Order MUST be: ja, sp, su, fa
  //                            - Albert Liu, Feb 05, 2022 Sat 02:38 EST
  public enum Semester {
    ja(2),
    sp(4),
    su(6),
    fa(8);

    public final int nyuCode;

    Semester(int nyuCode) { this.nyuCode = nyuCode; }
  }

  public static final class Subject {
    public final String code;
    public final String name;

    public Subject(String code, String name) {
      this.code = code;
      this.name = name;
    }

    private static final ArrayList<String> allSubjects;

    static {
      var subjects = new ArrayList<String>();
      for (String line : Utils.asResourceLines("/subjects.txt")) {
        String[] s = line.split(",", 3);
        String subject = s[0], school = s[1], name = s[2];

        subjects.add(subject);
      }

      allSubjects = subjects;
    }

    public static ArrayList<String> allSubjects() {
      return new ArrayList<>(allSubjects);
    }
  }

  public static final class School {
    public final String name;
    public final ArrayList<Subject> subjects;

    public School(String name) {
      this.name = name;
      this.subjects = new ArrayList<>();
    }
  }

  public static final class Course {
    public String name;
    public String deptCourseId;
    public String description;
    public List<Section> sections;

    // @NOTE: This is de-normalized into the Course object because that makes it
    // easier to work with when inserting into the database. However, we don't
    // want to include it in the actual API, because it doesn't seem necessary
    // right now.
    //
    //                                  - Albert Liu, Oct 11, 2022 Tue 01:01
    @JsonIgnore() public String subjectCode;

    public String toString() {
      return "Course(name=" + name + ",deptCourseId=" + deptCourseId + ")";
    }
  }

  public static final class Meeting {
    private static DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LocalDateTime beginDate; // contains date and time of first event.
    public int minutesDuration;     // Duration of meeting
    public LocalDateTime endDate;   // When the meeting stops repeating

    // we eventually want to switch to using the `fromCode(String code)` version
    // for the JSON creator. It will happen when we fully move away from
    // scraping from Schedge V1.
    //                                  - Albert Liu, Feb 03, 2022 Thu 01:02 EST
    @JsonCreator
    public static Meeting
    fromJson(@JsonProperty("beginDate") String beginDate,
             @JsonProperty("minutesDuration") int minutesDuration,
             @JsonProperty("endDate") String endDate) {
      Meeting meeting = new Meeting();
      meeting.beginDate = LocalDateTime.parse(beginDate, formatter);
      meeting.minutesDuration = minutesDuration;
      meeting.endDate = LocalDateTime.parse(endDate, formatter);

      return meeting;
    }

    public String getBeginDate() {
      ZonedDateTime zoned = beginDate.atZone(ZoneOffset.UTC);

      return DateTimeFormatter.ISO_INSTANT.format(zoned);
    }

    public String getEndDate() {
      ZonedDateTime zoned = endDate.atZone(ZoneOffset.UTC);

      return DateTimeFormatter.ISO_INSTANT.format(zoned);
    }
  }

  public static final class Section {
    public int registrationNumber;
    public String code;
    public String[] instructors;
    public SectionType type;
    public SectionStatus status;
    public List<Meeting> meetings;
    public List<Section> recitations;

    @JsonInclude(JsonInclude.Include.NON_NULL) public Integer waitlistTotal;
    @JsonInclude(JsonInclude.Include.NON_NULL) public String instructionMode;

    // Values that need to be updated
    @JsonInclude(JsonInclude.Include.NON_NULL) public String name;
    @JsonInclude(JsonInclude.Include.NON_NULL) public String campus;
    @JsonInclude(JsonInclude.Include.NON_NULL) public Double minUnits;
    @JsonInclude(JsonInclude.Include.NON_NULL) public Double maxUnits;
    @JsonInclude(JsonInclude.Include.NON_NULL) public String grading;
    @JsonInclude(JsonInclude.Include.NON_NULL) public String location;
    @JsonInclude(JsonInclude.Include.NON_NULL) public String notes;

    // @TODO: delete this
    @JsonInclude(JsonInclude.Include.NON_NULL) public String prerequisites;

    // @TODO: delete this
    @JsonInclude(JsonInclude.Include.NON_NULL) public String description;

    public static Section fromRow(Row row) {
      Section s = new Section();
      s.waitlistTotal = row.waitlistTotal;
      s.name = row.sectionName;
      s.registrationNumber = row.registrationNumber;
      s.code = row.sectionCode;
      s.instructors = row.instructors;
      s.type = row.sectionType;
      s.status = row.sectionStatus;
      s.meetings = row.meetings;
      s.minUnits = row.minUnits;
      s.maxUnits = row.maxUnits;
      s.instructionMode = row.instructionMode;
      s.location = row.location;

      return s;
    }

    public static Section fromFullRow(FullRow row) {
      Section s = new Section();
      s.waitlistTotal = row.waitlistTotal;
      s.name = row.sectionName;
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
      s.prerequisites = row.prerequisites;

      return s;
    }

    public String toString() { return JsonMapper.toJson(this); }
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
      if (status.equals("Wait List"))
        return WaitList;
      else
        return valueOf(status);
    }

    public boolean isOpen() { return this == Open; }
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

  public static final class Term {

    public final Nyu.Semester semester;
    public final int year;

    public Term(String sem, int year) { this(semesterFromString(sem), year); }

    public Term(Nyu.Semester semester, int year) {
      if (year < 1900)
        throw new IllegalArgumentException("Year was invalid: " + year);

      this.semester = semester;
      this.year = year;
    }

    public static Term fromId(int id) {
      // @Note: This requires order of enum variants to be correct
      Nyu.Semester semester = Nyu.Semester.values()[(id % 10) / 2 - 1];

      return new Term(semester, id / 10 + 1900);
    }

    // @TODO Make this more accurate
    public static Nyu.Semester getSemester(LocalDateTime time) {
      switch (time.getMonth()) {
      case JANUARY:
        return Nyu.Semester.ja;
      case FEBRUARY:
      case MARCH:
      case APRIL:
      case MAY:
        return Nyu.Semester.sp;
      case JUNE:
      case JULY:
      case AUGUST:
        return Nyu.Semester.su;
      case SEPTEMBER:
      case OCTOBER:
      case NOVEMBER:
      case DECEMBER:
        return Nyu.Semester.fa;

      default:
        throw new RuntimeException("Did they add another month? month=" +
                                   time.getMonth());
      }
    }

    public static Term getCurrentTerm() {
      LocalDateTime now = LocalDateTime.now();
      int year = now.getYear();

      return new Term(getSemester(now), year);
    }

    public static Nyu.Semester semesterFromStringNullable(String sem) {
      switch (sem.toLowerCase()) {
      case "ja":
      case "january":
        return Nyu.Semester.ja;
      case "sp":
      case "spring":
        return Nyu.Semester.sp;
      case "su":
      case "summer":
        return Nyu.Semester.su;
      case "fa":
      case "fall":
        return Nyu.Semester.fa;

      default:
        return null;
      }
    }

    public static Nyu.Semester semesterFromString(String sem) {
      Nyu.Semester semCode = semesterFromStringNullable(sem);
      if (semCode == null)
        throw new IllegalArgumentException("Invalid semester string: " + sem);

      return semCode;
    }

    public Term prevTerm() {
      switch (semester) {
      case ja:
        return new Term(Nyu.Semester.fa, year - 1);
      case sp:
        return new Term(Nyu.Semester.ja, year);
      case su:
        return new Term(Semester.sp, year);
      case fa:
        return new Term(Semester.su, year);

      default:
        return null;
      }
    }

    public Term nextTerm() {
      switch (semester) {
      case ja:
        return new Term(Semester.sp, year);
      case sp:
        return new Term(Semester.su, year);
      case su:
        return new Term(Semester.fa, year);
      case fa:
        return new Term(Semester.ja, year + 1);

      default:
        return null;
      }
    }

    public int getId() { return (year - 1900) * 10 + semester.nyuCode; }

    public String toString() {
      return "Term(" + semester + ' ' + year + ",id=" + getId() + ")";
    }

    @JsonValue
    public String json() {
      return "" + semester + year;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      Term term = (Term)o;
      return semester == term.semester && year == term.year;
    }

    @Override
    public int hashCode() {
      return Objects.hash(semester, year);
    }
  }
}
