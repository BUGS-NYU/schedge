package nyu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import utils.Utils;

public final class SubjectCode {

  private static Map<String, List<SubjectCode>> availableSubjects;
  private static Map<String, ArrayList<SubjectMetadata>> availableSubjectInfo;
  private static List<SubjectMetadata> allSubjectInfo;
  private static List<SchoolMetadata> schools;
  private static List<SubjectCode> allSubjects;

  public final String subject;
  public final String school;

  public SubjectCode(String subjectCode) {
    String[] code = subjectCode.split("-", 2);
    this.subject = code[0].toUpperCase();
    this.school = code[1].toUpperCase();
  }

  public SubjectCode(String subject, String school) {
    this.subject = subject.toUpperCase();
    this.school = school.toUpperCase();
  }

  public void checkValid() {
    if (!getAvailableSubjects().containsKey(school))
      throw new IllegalArgumentException("School code '" + school +
                                         "' in subject '" + this.toString() +
                                         "' is not valid");
    if (!getAvailableSubjects().get(school).contains(this))
      throw new IllegalArgumentException("School '" + school +
                                         "' doesn't contain subject '" +
                                         this.toString() + "'");
  }

  public static List<SchoolMetadata> allSchools() {
    if (schools == null) {
      schools = Utils.asResourceLines("/schools.txt")
                    .stream()
                    .map(str -> {
                      String[] data = str.split(",", 2);
                      return new SchoolMetadata(data[0], data[1]);
                    })
                    .collect(Collectors.toList());
    }
    return schools;
  }

  public static Map<String, ArrayList<SubjectMetadata>>
  getAvailableSubjectInfo() {
    if (availableSubjectInfo == null) {
      availableSubjectInfo = new HashMap<>();
      Utils.asResourceLines("/subjects.txt")
          .stream()
          .map(it -> new SubjectMetadata(it))
          .forEach(s -> {
            if (availableSubjectInfo.containsKey(s.getSchool())) {
              availableSubjectInfo.get(s.getSchool()).add(s);
            } else {
              ArrayList<SubjectMetadata> subjects = new ArrayList<>();
              subjects.add(s);
              availableSubjectInfo.put(s.getSchool(), subjects);
            }
          });
    }
    return availableSubjectInfo;
  }

  public static Map<String, List<SubjectCode>> getAvailableSubjects() {
    if (availableSubjects == null) {
      Function<List<SubjectMetadata>, List<SubjectCode>> f =
          e -> e.stream().map(it -> it.getCode()).collect(Collectors.toList());
      availableSubjects = getAvailableSubjectInfo().entrySet().stream().collect(
          Collectors.toMap(Map.Entry::getKey, e -> f.apply(e.getValue())));
    }
    return availableSubjects;
  }

  public static List<SubjectMetadata> allSubjectInfo() {
    if (allSubjectInfo == null) {
      allSubjectInfo = getAvailableSubjectInfo()
                           .entrySet()
                           .stream()
                           .flatMap(e -> e.getValue().stream())
                           .collect(Collectors.toList());
    }
    return allSubjectInfo;
  }

  public static List<SubjectCode> allSubjects() {
    if (allSubjects == null) {
      allSubjects = getAvailableSubjects()
                        .entrySet()
                        .stream()
                        .flatMap(entry -> entry.getValue().stream())
                        .collect(Collectors.toList());
    }
    return allSubjects;
  }

  public static List<SubjectMetadata> allSubjectInfoForSchool(String school) {
    return getAvailableSubjectInfo().get(school);
  }

  public static List<SubjectCode> allSubjectsForSchool(String school) {
    return getAvailableSubjects().get(school);
  }

  @JsonIgnore
  public String getAbbrev() {
    return subject + '-' + school;
  }

  public String toString() { return getAbbrev(); }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SubjectCode that = (SubjectCode)o;
    return this.school.equals(that.school) && this.subject.equals(that.subject);
  }

  public static final class SubjectMetadata {
    private String subject, school, name;
    SubjectMetadata(String csv) {
      String[] values = csv.split(",", 3);
      if (values.length < 3)
        System.err.println(csv);
      subject = values[0];
      school = values[1];
      name = values[2];
    }
    @JsonIgnore
    SubjectCode getCode() {
      return new SubjectCode(subject, school);
    }
    public String getSubject() { return subject; }
    public String getSchool() { return school; }
    public String getName() { return name; }
  }

  public static final class SchoolMetadata {
    private String code, name;
    SchoolMetadata(String code, String name) {
      this.code = code;
      this.name = name;
    }

    public String getCode() { return code; }
    public String getName() { return name; }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      SchoolMetadata that = (SchoolMetadata)o;
      return code.equals(that.code) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(code, name);
    }
  }
}
