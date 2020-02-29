package nyu;

import static utils.UtilsKt.asResourceLines;

import java.util.*;
import java.util.stream.Collectors;

public final class SubjectCode {

  private static HashMap<String, ArrayList<SubjectCode>> availableSubjects;
  private static List<SchoolMetadata> schools;
  private static List<SubjectCode> allSubjects;

  public final String subject;
  public final String school;

  public SubjectCode(String subjectCode) {
    String[] code = subjectCode.split("-", 2);
    this.subject = code[0];
    this.school = code[1];
  }

  public SubjectCode(String subject, String school) {
    this.subject = subject;
    this.school = school;
  }

  public static List<SchoolMetadata> allSchools() {
    if (schools == null) {
      schools = asResourceLines("/schools.txt")
                    .stream()
                    .map(str -> {
                      String[] data = str.split(",", 2);
                      return new SchoolMetadata(data[0], data[1]);
                    })
                    .collect(Collectors.toList());
    }
    return schools;
  }

  public static HashMap<String, ArrayList<SubjectCode>> getAvailableSubjects() {
    if (availableSubjects == null) {
      List<String> lines = asResourceLines("/subjects.txt");
      availableSubjects = new HashMap<>();
      lines.stream().map(str -> str.split("-")).forEach(strings -> {
        if (availableSubjects.containsKey(strings[1])) {
          availableSubjects.get(strings[1])
              .add(new SubjectCode(strings[0], strings[1]));
        } else {
          ArrayList<SubjectCode> subjects = new ArrayList<>();
          subjects.add(new SubjectCode(strings[0], strings[1]));
          availableSubjects.put(strings[1], subjects);
        }
      });
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

  public static List<SubjectCode> allSubjectsForSchool(String school) {
    return getAvailableSubjects().get(school);
  }

  public String getAbbrev() { return subject + '-' + school; }

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
