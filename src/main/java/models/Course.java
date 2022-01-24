package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import javax.validation.constraints.NotNull;
import nyu.SubjectCode;

public class Course {

  public String name;
  public String deptCourseId;
  @JsonInclude(JsonInclude.Include.NON_NULL) public String description;
  public SubjectCode subjectCode;
  public List<Section> sections;

  @JsonIgnore
  public String getSubject() {
    return subjectCode.code;
  }

  @JsonIgnore
  public String getSchool() {
    return subjectCode.school;
  }
}
