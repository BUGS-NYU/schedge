package types;

import static utils.TryCatch.*;

import com.fasterxml.jackson.annotation.*;
import java.util.*;
import utils.*;

public final class Subject {

  // TODO It seems time zone stuff in Java SQL is a bit borked. Let's just set
  // all DB timestamps to UTC, and then manually convert to whatever on the
  // fly. Seems easier than the alternative.
  //
  // https://stackoverflow.com/questions/14070572/is-java-sql-timestamp-timezone-specific/14070771
  // https://stackoverflow.com/questions/42280454/changing-localdatetime-based-on-time-difference-in-current-time-zone-vs-eastern
  //
  //                            - Albert Liu, Feb 03, 2022 Thu 01:12 EST
  public static final class School {
    public String name;
    public TimeZone timezone;
    public ArrayList<Subject> subjects;

    public String getTimezone() { return timezone.getID(); }
  }

  @JsonIgnore public volatile String schoolCode;
  @JsonValue public volatile String code;
  @JsonIgnore public volatile String name;
  @JsonIgnore public final int ordinal;

  private static ArrayList<Subject> subjects = new ArrayList<>();
  private static Map<String, Subject> subjectsByCode = new HashMap<>();
  private static Map<String, School> schools = new HashMap<>();

  // Init stuffs
  static {
    for (String line : Utils.asResourceLines("/subjects.txt")) {
      String[] s = line.split(",", 3);
      String subject = s[0], school = s[1], name = s[2];

      addSubject(school, subject, name);
    }

    for (String line : Utils.asResourceLines("/schools.txt")) {
      String[] s = line.split(",", 3);
      String abbreviation = s[0], timezone = s[1], name = s[2];

      School school = new School();
      school.name = name;
      school.timezone = TimeZone.getTimeZone(timezone);
      school.subjects = new ArrayList<>();

      schools.put(abbreviation, school);
    }

    for (Subject code : subjects) {
      String abbreviation = code.code.split("-")[1];
      School school = schools.get(abbreviation);

      if (school == null) {
        DEFAULT_LOGGER.warn(
            "Missing school for a subject, using default: school={},subject={}",
            abbreviation, code);

        school = new School();
        school.name = "";
        school.timezone = TimeZone.getTimeZone("America/New_York");
        school.subjects = new ArrayList<>();

        schools.put(abbreviation, school);
      }

      school.subjects.add(code);
    }
  }

  private Subject(String schoolCode, String code, String name, int ordinal) {
    this.schoolCode = schoolCode;
    this.code = code;
    this.name = name;
    this.ordinal = ordinal;
  }

  public static Subject addSubject(String schoolCode, String code,
                                   String name) {
    synchronized (subjects) {
      Subject subject = new Subject(schoolCode, code, name, subjects.size());

      subjects.add(subject);
      subjectsByCode.put(code, subject);

      return subject;
    }
  }

  public static Subject fromCode(String code) {
    synchronized (subjects) { return subjectsByCode.get(code.toUpperCase()); }
  }

  // we eventually want to switch to using the `fromCode(String code)` version
  // for the JSON creator. It will happen when we fully move away from scraping
  // from Schedge V1.
  //                                  - Albert Liu, Feb 03, 2022 Thu 01:02 EST
  @JsonCreator
  public static Subject fromCode(@JsonProperty("code") String code,
                                 @JsonProperty("school") String school) {
    synchronized (subjects) { return subjectsByCode.get(code + "-" + school); }
  }

  public static Subject fromOrdinal(int ordinal) {
    synchronized (subjects) { return subjects.get(ordinal); }
  }

  public static synchronized Map<String, School> allSchools() {
    Map<String, School> schoolsCopy = new HashMap<>();

    synchronized (schools) {
      for (Map.Entry<String, School> entry : schools.entrySet()) {
        School value = entry.getValue();

        School school = new School();
        school.name = value.name;
        school.timezone = value.timezone;
        school.subjects = value.subjects;

        schoolsCopy.put(entry.getKey(), school);
      }
    }

    return schoolsCopy;
  }

  public static synchronized List<Subject> allSubjects() {
    ArrayList<Subject> localSubjects = new ArrayList<>();

    synchronized (subjects) {
      for (Subject code : subjects) {
        localSubjects.add(code);
      }
    }

    return localSubjects;
  }

  public String toString() { return this.code; }
}
