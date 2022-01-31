package types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.*;
import java.util.stream.Collectors;
import utils.Utils;

public final class SubjectCode {

  @JsonIgnore public String schoolCode;
  @JsonValue public String code;
  @JsonIgnore public String name;
  @JsonIgnore public final int ordinal;

  private static ArrayList<SubjectCode> subjects = new ArrayList<>();
  private static Map<String, SubjectCode> subjectsByCode = new HashMap<>();
  private static Map<String, SchoolMetadata> schools;

  private SubjectCode(String schoolCode, String code, String name) {
    this.schoolCode = schoolCode;
    this.code = code;
    this.name = name;

    synchronized (subjects) {
      this.ordinal = subjects.size();

      subjects.add(this);
      subjectsByCode.put(this.code, this);
    }
  }

  public static SubjectCode addSubject(String schoolCode, String code,
                                       String name) {
    return new SubjectCode(schoolCode, code, name);
  }

  // Init stuffs
  static {
    for (String line : Utils.asResourceLines("/subjects.txt")) {
      String[] s = line.split(",", 3);
      String subject = s[0], school = s[1], name = s[2];

      addSubject(school, subject, name);
    }
  }

  public static SubjectCode fromCode(String code) {
    synchronized (subjects) { return subjectsByCode.get(code); }
  }

  @JsonCreator
  public static SubjectCode fromCode(@JsonProperty("code") String code,
                                     @JsonProperty("school") String school) {
    synchronized (subjects) { return subjectsByCode.get(code + "-" + school); }
  }

  public static SubjectCode fromOrdinal(int ordinal) {
    synchronized (subjects) { return subjects.get(ordinal); }
  }

  public static synchronized Map<String, SchoolMetadata> allSchools() {
    if (schools == null) {
      schools = Utils.asResourceLines("/schools.txt")
                    .stream()
                    .map(str -> str.split(",", 2))
                    .collect(Collectors.toMap(s -> s[0], s -> {
                      SchoolMetadata school = new SchoolMetadata();
                      school.name = s[1];
                      school.subjects = new ArrayList<>();

                      return school;
                    }));

      synchronized (subjects) {
        for (SubjectCode code : subjects) {
          schools.get(code.schoolCode).subjects.add(code);
        }
      }
    }

    return schools;
  }

  public static synchronized List<SubjectCode> allSubjects() {
    ArrayList<SubjectCode> localSubjects = new ArrayList<>();

    synchronized (subjects) {
      for (SubjectCode code : subjects) {
        localSubjects.add(code);
      }
    }

    return localSubjects;
  }

  public String toString() { return this.code; }

  public static final class SchoolMetadata {
    public String name;
    public ArrayList<SubjectCode> subjects;
  }
}
