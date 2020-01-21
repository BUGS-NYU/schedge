package scraping.models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class SubjectCode {
  private static String schoolsFile = "src/main/resources/schools.txt";
  private static String subjectsFile = "src/main/resources/subjects.txt";
  //    private String abbrev;
  private String subject;
  private String school;

  public SubjectCode(String school, String subject) throws IOException {
    validate(school, subject);
    this.subject = subject;
    this.school = school;
  }

  public static List<SubjectCode> allSubjects() throws IOException {
    Map<String, Set<String>> availableSubjects = getAllSubjects();
    List<SubjectCode> listOfSubjects = new ArrayList<>();
    availableSubjects.forEach(
        (school, subjects) -> subjects.forEach(subject -> {
          try {
            listOfSubjects.add(new SubjectCode(school, subject));
          } catch (IOException e) {
            e.printStackTrace();
          }
        }));
    return listOfSubjects;
  }

  public static List<SubjectCode> allSubjects(String forSchool)
      throws IOException {
    if (forSchool == null) {
      return allSubjects();
    }
    Map<String, Set<String>> availableSubjects = getAllSubjects();
    List<SubjectCode> listOfSubjects = new ArrayList<>();
    Set<String> subjects = availableSubjects.get(forSchool);
    subjects.forEach(subject -> {
      try {
        listOfSubjects.add(new SubjectCode(forSchool, subject));
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    return listOfSubjects;
  }

  public static Map<String, String> getAllSchools() throws IOException {
    Map<String, String> map =
        Files.lines(Paths.get(schoolsFile).toAbsolutePath())
            .collect(Collectors.toMap(schoolCode
                                      -> schoolCode.split(",")[0],
                                      schoolName -> schoolName.split(",")[1]));
    return map;
  }

  @NotNull
  public static Map<String, Set<String>> getAllSubjects() throws IOException {
    Map<String, Set<String>> availSubjects = new HashMap<>();
    List<Pair> list =
        Files.lines(Paths.get(subjectsFile).toAbsolutePath())
            .map(value -> new Pair(value.split("-")[0], value.split("-")[1]))
            .collect(Collectors.toList());
    list.forEach(
        (Pair) -> availSubjects.putIfAbsent(Pair.getSchool(), new HashSet<>()));
    list.forEach(
        (Pair) -> availSubjects.get(Pair.getSchool()).add(Pair.getSubject()));
    return availSubjects;
  }

  public static SubjectCode getUnchecked(String subjectString)
      throws IOException {
    String[] values = subjectString.split("-");
    String subject = values[0];
    String school = values[1];
    return new SubjectCode(school, subject);
  }

  public void validate(String school, String subject) throws IOException {
    Map<String, Set<String>> availableSubjects = getAllSubjects();
    if (availableSubjects.get(school) == null) {
      throw new IllegalArgumentException("School must be valid");
    } else if (!(availableSubjects.get(school).contains(subject))) {
      throw new IllegalArgumentException("Subject must be valid");
    }
  }

  public String getSubject() { return subject; }

  public String getSchool() { return school; }

  public String toString() { return subject + "-" + school; }

  private static class Pair {
    private String school;
    private String subject;

    public Pair(String subject, String school) {
      this.school = school;
      this.subject = subject;
    }

    public String getSchool() { return school; }

    public String getSubject() { return subject; }
  }
}
