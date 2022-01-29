package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import nyu.SubjectCode;

public class Course {
  public String name;
  public String deptCourseId;
  @JsonInclude(JsonInclude.Include.NON_NULL) public String description;
  public SubjectCode subjectCode;
  public List<Section> sections;

  public String toString() {
    return "Course(name=" + name + ",deptCourseId=" + deptCourseId + ")";
  }
}
