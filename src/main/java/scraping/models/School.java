package scraping.models;

import java.util.List;
import javax.validation.constraints.NotNull;

public class School {
  private String code;
  private String name;
  private List<Subject> subjects;

  public School(String code, String name, List<Subject> subjects) {
    this.code = code;
    this.name = name;
    this.subjects = subjects;
  }

  public @NotNull String getCode() { return code; }

  public @NotNull String getName() { return name; }

  public @NotNull List<Subject> getSubjects() { return subjects; }
}
