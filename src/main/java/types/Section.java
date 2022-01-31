package types;

import com.fasterxml.jackson.annotation.JsonInclude;
import database.models.FullRow;
import database.models.Row;
import java.util.ArrayList;
import java.util.List;

import utils.JsonMapper;

public class Section {
  public int registrationNumber;
  public String code;
  public String[] instructors;
  public SectionType type;
  public SectionStatus status;
  public List<Meeting> meetings;
  public List<Section> recitations;

  @JsonInclude(JsonInclude.Include.NON_NULL) public Integer waitlistTotal;
  @JsonInclude(JsonInclude.Include.NON_NULL) public String instructionMode;

  // Values that need to be updated
  @JsonInclude(JsonInclude.Include.NON_NULL) public String name;
  @JsonInclude(JsonInclude.Include.NON_NULL) public String campus;
  @JsonInclude(JsonInclude.Include.NON_NULL) public Double minUnits;
  @JsonInclude(JsonInclude.Include.NON_NULL) public Double maxUnits;
  @JsonInclude(JsonInclude.Include.NON_NULL) public String grading;
  @JsonInclude(JsonInclude.Include.NON_NULL) public String location;
  @JsonInclude(JsonInclude.Include.NON_NULL) public String notes;
  @JsonInclude(JsonInclude.Include.NON_NULL) public String prerequisites;

  // Values that are sometimes necessary
  @JsonInclude(JsonInclude.Include.NON_NULL) public String description;

  public void addRecitation(Section s) {
    if (recitations == null) {
      recitations = new ArrayList<>();
    }

    recitations.add(s);
  }

  public static Section fromRow(Row row) {
    Section s = new Section();
    s.waitlistTotal = row.waitlistTotal;
    s.name = row.sectionName;
    s.registrationNumber = row.registrationNumber;
    s.code = row.sectionCode;
    s.instructors = row.instructors;
    s.type = row.sectionType;
    s.status = row.sectionStatus;
    s.meetings = row.meetings;
    s.minUnits = row.minUnits;
    s.maxUnits = row.maxUnits;
    s.instructionMode = row.instructionMode;
    s.location = row.location;

    return s;
  }

  public static Section fromFullRow(FullRow row) {
    Section s = new Section();
    s.waitlistTotal = row.waitlistTotal;
    s.name = row.sectionName;
    s.registrationNumber = row.registrationNumber;
    s.code = row.sectionCode;
    s.instructors = row.instructors;
    s.type = row.sectionType;
    s.status = row.sectionStatus;
    s.meetings = row.meetings;
    s.campus = row.campus;
    s.minUnits = row.minUnits;
    s.maxUnits = row.maxUnits;
    s.instructionMode = row.instructionMode;
    s.grading = row.grading;
    s.notes = row.notes;
    s.location = row.location;
    s.prerequisites = row.prerequisites;

    return s;
  }

  public String toString() { return JsonMapper.toJson(this); }
}
