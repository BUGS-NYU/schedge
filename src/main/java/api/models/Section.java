package api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import nyu.SectionStatus;
import nyu.SectionType;

public class Section {
  private String sectionName;
  private int registrationNumber;
  private String sectionCode;
  private String[] instructors;
  private SectionType type;
  private SectionStatus status;
  private List<Meeting> meetings;
  private List<Section> recitations;
  private Integer waitlistTotal;

  // values that need to be updated
  private String campus;
  private String description;
  private Float minUnits;
  private Float maxUnits;
  private String instructionMode;
  private String grading;
  private String location;
  private String notes;
  private String prerequisites;

  public Section(int registrationNumber, String sectionCode, String[] instructors,
                 SectionType type, SectionStatus status, List<Meeting> meetings,
                 List<Section> recitations, Integer waitlistTotal) {

    if (type != SectionType.LEC && recitations != null) {
      throw new IllegalArgumentException(
          "If the section type isn't a lecture, it can't have recitations!");
    }

    this.registrationNumber = registrationNumber;
    this.sectionCode = sectionCode;
    this.instructors = instructors;
    this.type = type;
    this.status = status;
    this.meetings = meetings;
    this.recitations = recitations;
    this.waitlistTotal = waitlistTotal;
  }

  public Section(int registrationNumber, String sectionCode, String[] instructors,
                 SectionType type, SectionStatus status, List<Meeting> meetings,
                 List<Section> recitations, String sectionName,
                 Integer waitlistTotal, String campus, String description,
                 Float minUnits, Float maxUnits, String instructionMode,
                 String grading, String location, String notes,
                 String prerequisites) {
    if (type != SectionType.LEC && recitations != null) {
      throw new IllegalArgumentException(
          "If the section type isn't a lecture, it can't have recitations!");
    }

    this.waitlistTotal = waitlistTotal;
    this.sectionName = sectionName;
    this.registrationNumber = registrationNumber;
    this.sectionCode = sectionCode;
    this.instructors = instructors;
    this.type = type;
    this.status = status;
    this.meetings = meetings;
    this.recitations = recitations;
    this.campus = campus;
    this.description = description;
    this.minUnits = minUnits;
    this.maxUnits = maxUnits;
    this.instructionMode = instructionMode;
    this.grading = grading;
    this.notes = notes;
    this.location = location;
    this.prerequisites = prerequisites;
  }

  public @NotNull int getRegistrationNumber() { return registrationNumber; }

  public @NotNull String getSectionCode() { return sectionCode; }

  public @NotNull String[] getInstructors() { return instructors; }

  public @NotNull SectionType getType() { return type; }

  public @NotNull SectionStatus getStatus() { return status; }

  public @NotNull List<Meeting> getMeetings() { return meetings; }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public List<Section> getRecitations() {
    return recitations;
  }
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public Integer getWaitlistTotal() {
    return waitlistTotal;
  }
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getCampus() {
    return campus;
  }
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getSectionName() {
    return sectionName;
  }
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getDescription() {
    return description;
  }
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public Float getMinUnits() {
    return minUnits;
  }
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public Float getMaxUnits() {
    return maxUnits;
  }
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getInstructionMode() {
    return instructionMode;
  }
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getGrading() {
    return grading;
  }
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getLocation() {
    return location;
  }
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getNotes() {
    return notes;
  }
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getPrerequisites() {
    return prerequisites;
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
        Objects.equals(sectionName, section.sectionName) &&
        sectionCode.equals(section.sectionCode) &&
        instructors.equals(section.instructors) && type == section.type &&
        status == section.status &&
        Objects.equals(waitlistTotal, section.waitlistTotal) &&
        Objects.equals(campus, section.campus) &&
        Objects.equals(description, section.description) &&
        Objects.equals(minUnits, section.minUnits) &&
        Objects.equals(maxUnits, section.maxUnits) &&
        Objects.equals(instructionMode, section.instructionMode) &&
        Objects.equals(grading, section.grading) &&
        Objects.equals(location, section.location) &&
        Objects.equals(prerequisites, section.prerequisites);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sectionName, registrationNumber, sectionCode,
                        instructors, type, status, waitlistTotal, campus,
                        description, minUnits, maxUnits, instructionMode,
                        grading, location, prerequisites);
  }
}
