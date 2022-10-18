package database.models;

public class SectionID {
  public final String subjectCode;
  public final int id;
  public final int registrationNumber;

  public SectionID(String subject, int i, int r) {
    this.subjectCode = subject;
    id = i;
    registrationNumber = r;
  }
}