package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import javax.validation.constraints.NotNull;
import nyu.SubjectCode;

public class Course {

  private String name;
  private String deptCourseId;
  private String description;
  private SubjectCode subjectCode;
  private List<Section> sections;

  public Course(String name, String deptCourseId, String description,
                SubjectCode subjectCode, List<Section> sections) {
    this.name = name;
    this.deptCourseId = deptCourseId;
    this.description = description;
    this.subjectCode = subjectCode;
    this.sections = sections;
  }

  public @NotNull String getName() { return name; }

  public @NotNull String getDeptCourseId() { return deptCourseId; }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getDescription() {
    return description;
  }

  public @NotNull SubjectCode getSubjectCode() { return subjectCode; }

  public @NotNull List<Section> getSections() { return sections; }

  @JsonIgnore
  public String getSubject() {
    return subjectCode.code;
  }

  @JsonIgnore
  public String getSchool() {
    return subjectCode.school;
  }
}
