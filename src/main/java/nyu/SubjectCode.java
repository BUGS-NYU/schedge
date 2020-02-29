package nyu;

import java.util.List;

public final class SubjectCode {
  public final String subject;
  public final String school;

  public SubjectCode(String subject, String school) {
    this.subject = subject;
    this.school = school;
  }

  public static List<SubjectCode> allSubjects() { return null; }

  public static List<SubjectCode> allSubjectsForSchool(String school) {
    return null;
  }

  public String getAbbrev() { return subject + '-' + school; }
}
