package scraping.models;

public class Term {
  private Semester sem;
  private Integer year;
  private Integer id;

  public Term(Semester sem, Integer year) {
    setSem(sem);
    setYear(year);
  }

  public static Term fromId(Integer id) {
    if (id < 0) {
      throw new IllegalArgumentException("Can't create Term with negative ID");
    }
    return new Term(Semester.fromInt(id % 10), (id / 10) + 1900);
  }

  public void setYear(Integer year) {
    if (year < 0) {
      throw new IllegalArgumentException(
          "Can't create a Term with negative year");
    }
    this.year = year;
  }

  public void setSem(Semester sem) { this.sem = sem; }

  public Integer getId() { return (this.year - 1900) * 10 + sem.toInt(); }

  public String toString() {
    return "Term(" + sem.toString() + "," + year + ")";
  }
}
