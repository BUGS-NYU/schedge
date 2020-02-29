package api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.validation.constraints.NotNull;
import models.SubjectCode;

public class Course {

  private String name;
  private String deptCourseId;
  private SubjectCode subjectCode;
  private List<Section> sections;

  public Course(String name, String deptCourseId, SubjectCode subjectCode,
                List<Section> sections) {
    this.name = name;
    this.deptCourseId = deptCourseId;
    this.subjectCode = subjectCode;
    this.sections = sections;
  }

  public @NotNull String getName() { return name; }

  public @NotNull String getDeptCourseId() { return deptCourseId; }

  public @NotNull SubjectCode getSubjectCode() { return subjectCode; }

  public @NotNull List<Section> getSections() { return sections; }

  @JsonIgnore
  public String getSubject() {
    return subjectCode.getSubject();
  }

  @JsonIgnore
  public String getSchool() {
    return subjectCode.getSchool();
  }
}
