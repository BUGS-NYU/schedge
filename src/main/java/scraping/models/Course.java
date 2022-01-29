package scraping.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import nyu.SubjectCode;

public final class Course {

  public final String name;
  public final String deptCourseId;
  public final SubjectCode subjectCode;
  public final List<Section> sections;

  public Course(String name, String deptCourseId, SubjectCode subjectCode,
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
