package scraping.models;

import java.util.List;

public final class Course {

  public final String name;
  public final String deptCourseId;
  public final String subjectCode;
  public final List<Section> sections;

  public Course(String name, String deptCourseId, String subjectCode,
                List<Section> sections) {
    this.name = name;
    this.deptCourseId = deptCourseId;
    this.subjectCode = subjectCode;
    this.sections = sections;
  }

  public String toString() {
    return "Course(name=" + name + ",deptCourseId=" + deptCourseId +
        ",subjectCode=" + subjectCode + ",sections=" + sections + ")";
  }
}
