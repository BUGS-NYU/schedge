package database.models;

import types.Subject;

public class SectionID {
  public final Subject subjectCode;
  public final int id;
  public final int registrationNumber;

  public SectionID(Subject subject, int i, int r) {
    this.subjectCode = subject;
    id = i;
    registrationNumber = r;
  }
}