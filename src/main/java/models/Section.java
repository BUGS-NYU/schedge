package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import database.models.FullRow;
import database.models.Row;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import nyu.Meeting;
import nyu.SectionStatus;
import nyu.SectionType;

public class Section {
  public int registrationNumber;
  public String code;
  public String[] instructors;
  public SectionType type;
  public SectionStatus status;
  public List<Meeting> meetings;
  public List<Section> recitations;
  public Integer waitlistTotal;
  public String instructionMode;

  // values that need to be updated
  @JsonInclude(JsonInclude.Include.NON_NULL) public String name;
  @JsonInclude(JsonInclude.Include.NON_NULL) public String campus;
  @JsonInclude(JsonInclude.Include.NON_NULL) public Double minUnits;
  @JsonInclude(JsonInclude.Include.NON_NULL) public Double maxUnits;
  @JsonInclude(JsonInclude.Include.NON_NULL) public String grading;
  @JsonInclude(JsonInclude.Include.NON_NULL) public String location;
  @JsonInclude(JsonInclude.Include.NON_NULL) public String notes;
  @JsonInclude(JsonInclude.Include.NON_NULL) public String prerequisites;

  public Section(int registrationNumber, String code, String[] instructors,
                 SectionType type, SectionStatus status, List<Meeting> meetings,
                 List<Section> recitations, Integer waitlistTotal,
                 String instructionMode) {

    if (type != SectionType.LEC && recitations != null) {
      throw new IllegalArgumentException(
          "If the section type isn't a lecture, it can't have recitations!");
    }
    this.registrationNumber = registrationNumber;
    this.code = code;
    this.instructors = instructors;
    this.type = type;
    this.status = status;
    this.meetings = meetings;
    this.recitations = recitations;
    this.waitlistTotal = waitlistTotal;
    this.instructionMode = instructionMode;
  }

  public Section(int registrationNumber, String code, String[] instructors,
                 SectionType type, SectionStatus status, List<Meeting> meetings,
                 List<Section> recitations, String name, Integer waitlistTotal,
                 String campus, Double minUnits, Double maxUnits,
                 String instructionMode, String grading, String location,
                 String notes, String prerequisites) {
    if (type != SectionType.LEC && recitations != null) {
      throw new IllegalArgumentException(
          "If the section type isn't a lecture, it can't have recitations!");
    }

    this.waitlistTotal = waitlistTotal;
    this.name = name;
    this.registrationNumber = registrationNumber;
    this.code = code;
    this.instructors = instructors;
    this.type = type;
    this.status = status;
    this.meetings = meetings;
    this.recitations = recitations;
    this.campus = campus;
    this.minUnits = minUnits;
    this.maxUnits = maxUnits;
    this.instructionMode = instructionMode;
    this.grading = grading;
    this.notes = notes;
    this.location = location;
    this.prerequisites = prerequisites;
  }

  public void addRecitation(Section s) {
    if (recitations == null) {
      recitations = new ArrayList<>();
    }

    recitations.add(s);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Section section = (Section)o;
    return registrationNumber == section.registrationNumber &&
        Objects.equals(name, section.name) && code.equals(section.code) &&
        instructors.equals(section.instructors) && type == section.type &&
        status == section.status &&
        Objects.equals(waitlistTotal, section.waitlistTotal) &&
        Objects.equals(campus, section.campus) &&
        Objects.equals(minUnits, section.minUnits) &&
        Objects.equals(maxUnits, section.maxUnits) &&
        Objects.equals(instructionMode, section.instructionMode) &&
        Objects.equals(grading, section.grading) &&
        Objects.equals(location, section.location) &&
        Objects.equals(prerequisites, section.prerequisites);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, registrationNumber, code, instructors, type,
                        status, waitlistTotal, campus, minUnits, maxUnits,
                        instructionMode, grading, location, prerequisites);
  }

  public static Section fromRow(Row row) {
    return new Section(row.registrationNumber, row.sectionCode, row.instructors,
                       row.sectionType, row.sectionStatus, row.meetings, null,
                       row.sectionName, row.waitlistTotal, null, row.minUnits,
                       row.maxUnits, row.instructionMode, null, row.location,
                       null, null);
  }

  public static Section fromFullRow(FullRow row) {
    return new Section(row.registrationNumber, row.sectionCode, row.instructors,
                       row.sectionType, row.sectionStatus, row.meetings, null,
                       row.sectionName, row.waitlistTotal, row.campus,
                       row.minUnits, row.maxUnits, row.instructionMode,
                       row.grading, row.location, row.notes, row.prerequisites);
  }
}
