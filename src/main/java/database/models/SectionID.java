package database.models;

import types.SubjectCode;

public class SectionID {
  public final SubjectCode subjectCode;
  public final int id;
  public final int registrationNumber;

  public SectionID(SubjectCode subjectCode, int i, int r) {
    this.subjectCode = subjectCode;
    id = i;
    registrationNumber = r;
  }
}