package types;

import com.fasterxml.jackson.annotation.JsonValue;
import java.time.LocalDateTime;
import java.util.Objects;

public final class Term {

  // @Note: order is important here. Order MUST be: ja, sp, su, fa
  //                            - Albert Liu, Feb 05, 2022 Sat 02:38 EST
  public enum Semester {
    ja(2),
    sp(4),
    su(6),
    fa(8);

    public int nyuCode;

    Semester(int nyuCode) { this.nyuCode = nyuCode; }
  }

  public final Semester semester;
  public final int year;

  public Term(String sem, int year) { this(semesterFromString(sem), year); }

  public Term(Semester semester, int year) {
    if (year < 1900)
      throw new IllegalArgumentException("Year was invalid: " + year);

    this.semester = semester;
    this.year = year;
  }

  public static Term fromId(int id) {
    // @Note: This requires order of enum variants to be correct
    Semester semester = Semester.values()[(id % 10) / 2 - 1];

    return new Term(semester, id / 10 + 1900);
  }

  // @TODO Make this more accurate
  public static Semester getSemester(LocalDateTime time) {
    switch (time.getMonth()) {
    case JANUARY:
      return Semester.ja;
    case FEBRUARY:
    case MARCH:
    case APRIL:
    case MAY:
      return Semester.sp;
    case JUNE:
    case JULY:
    case AUGUST:
      return Semester.su;
    case SEPTEMBER:
    case OCTOBER:
    case NOVEMBER:
    case DECEMBER:
      return Semester.fa;

    default:
      throw new RuntimeException("Did they add another month? month=" +
                                 time.getMonth());
    }
  }

  public static Term getCurrentTerm() {
    LocalDateTime now = LocalDateTime.now();
    int year = now.getYear();

    return new Term(getSemester(now), year);
  }

  public static Semester semesterFromStringNullable(String sem) {
    switch (sem.toLowerCase()) {
    case "ja":
    case "january":
      return Semester.ja;
    case "sp":
    case "spring":
      return Semester.sp;
    case "su":
    case "summer":
      return Semester.su;
    case "fa":
    case "fall":
      return Semester.fa;

    default:
      return null;
    }
  }

  public static Semester semesterFromString(String sem) {
    Semester semCode = semesterFromStringNullable(sem);
    if (semCode == null)
      throw new IllegalArgumentException("Invalid semester string: " + sem);

    return semCode;
  }

  public String semString() { return this.semester.toString(); }

  public Term prevTerm() {
    switch (semester) {
    case ja:
      return new Term(Semester.fa, year - 1);
    case sp:
      return new Term(Semester.ja, year);
    case su:
      return new Term(Semester.sp, year);
    case fa:
      return new Term(Semester.su, year);

    default:
      return null;
    }
  }

  public Term nextTerm() {
    switch (semester) {
    case ja:
      return new Term(Semester.sp, year);
    case sp:
      return new Term(Semester.su, year);
    case su:
      return new Term(Semester.fa, year);
    case fa:
      return new Term(Semester.ja, year + 1);

    default:
      return null;
    }
  }

  public int getId() { return (year - 1900) * 10 + semester.nyuCode; }

  public String toString() {
    return "Term(" + semester + ' ' + year + ",id=" + getId() + ")";
  }

  @JsonValue
  public String json() {
    return "" + semester + year;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Term term = (Term)o;
    return semester == term.semester && year == term.year;
  }

  @Override
  public int hashCode() {
    return Objects.hash(semester, year);
  }
}
