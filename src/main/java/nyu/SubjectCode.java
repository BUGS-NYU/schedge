package nyu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import utils.Utils;

public final class SubjectCode {

  private static Map<String, List<SubjectCode>> availableSubjects;
  private static Map<String, Map<String, String>> availableSubjectInfo;
  private static List<SubjectMetadata> allSubjectInfo;
  private static Map<String, String> schools;
  private static List<SubjectCode> allSubjects;

  public final String code;
  public final String school;

  public SubjectCode(String subjectCode) {
    String[] code = subjectCode.split("-", 2);
    this.code = code[0].toUpperCase();
    this.school = code[1].toUpperCase();
  }

  public SubjectCode(String code, String school) {
    this.code = code.toUpperCase();
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

  public static Map<String, String> allSchools() {
    if (schools == null) {
      schools = Utils.asResourceLines("/schools.txt")
                    .stream()
                    .map(str -> str.split(",", 2))
                    .collect(Collectors.toMap(s -> s[0], s -> s[1]));
    }
    return schools;
  }

  public static Map<String, Map<String, String>> getAvailableSubjectInfo() {
    if (availableSubjectInfo == null) {
      availableSubjectInfo = new HashMap<>();
      Utils.asResourceLines("/subjects.txt")
          .stream()
          .map(it -> it.split(","))
          .forEach(s -> {
            if (!availableSubjectInfo.containsKey(s[1])) {
              availableSubjectInfo.put(s[1], new HashMap<>());
            }
            availableSubjectInfo.get(s[1]).put(s[0], s[2]);
          });
    }
    return availableSubjectInfo;
  }

  public static Map<String, List<SubjectCode>> getAvailableSubjects() {
    if (availableSubjects == null) {
      BiFunction<String, Set<String>, List<SubjectCode>> f = (school, subjects)
          -> subjects.stream()
                 .map(it -> new SubjectCode(it, school))
                 .collect(Collectors.toList());
      availableSubjects = getAvailableSubjectInfo().entrySet().stream().collect(
          Collectors.toMap(Map.Entry::getKey,
                           e -> f.apply(e.getKey(), e.getValue().keySet())));
    }
    return availableSubjects;
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

  public static Map<String, String> allSubjectInfoForSchool(String school) {
    return getAvailableSubjectInfo().get(school);
  }

  public static List<SubjectCode> allSubjectsForSchool(String school) {
    return getAvailableSubjects().get(school);
  }

  @JsonIgnore
  public String getAbbrev() {
    return code + '-' + school;
  }

  public String toString() { return getAbbrev(); }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SubjectCode that = (SubjectCode)o;
    return this.school.equals(that.school) && this.code.equals(that.code);
  }

  public static final class SubjectMetadata {
    private String code, school, name;
    SubjectMetadata(String csv) {
      String[] values = csv.split(",", 3);
      code = values[0];
      school = values[1];
      name = values[2];
    }
    @JsonIgnore
    SubjectCode getSubjectCode() {
      return new SubjectCode(code, school);
    }
    public String getCode() { return code; }
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
