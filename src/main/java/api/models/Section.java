package api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import javax.validation.constraints.NotNull;
import models.SectionStatus;
import models.SectionType;

public class Section {
  private String sectionName;
  private int registrationNumber;
  private String sectionCode;
  private String instructor;
  private SectionType type;
  private SectionStatus status;
  private List<Meeting> meetings;
  private List<Section> recitations;
  private Integer waitlistTotal;

  // values that need to be updated
  private String campus;

  private String description;

  private double minUnits;

  private double maxUnits;

  private String instructionMode;

  private String grading;

  private String roomNumber;

  private String prerequisites;

  public Section(int registrationNumber, String sectionCode, String instructor,
                 SectionType type, SectionStatus status, List<Meeting> meetings,
                 List<Section> recitations, Integer waitlistTotal) {

    if (type != SectionType.LEC && recitations != null) {
      throw new IllegalArgumentException(
          "If the section type isn't a lecture, it can't have recitations!");
    }

    this.registrationNumber = registrationNumber;
    this.sectionCode = sectionCode;
    this.instructor = instructor;
    this.type = type;
    this.status = status;
    this.meetings = meetings;
    this.recitations = recitations;
    this.waitlistTotal = waitlistTotal;
  }

  public Section(int registrationNumber, String sectionCode, String instructor,
                 SectionType type, SectionStatus status, List<Meeting> meetings,
                 List<Section> recitations, String sectionName,
                 Integer waitlistTotal, String campus, String description,
                 double minUnits, double maxUnits, String instructionMode,
                 String grading, String roomNumber, String prerequisites) {
    if (type != SectionType.LEC && recitations != null) {
      throw new IllegalArgumentException(
          "If the section type isn't a lecture, it can't have recitations!");
    }

    this.waitlistTotal = waitlistTotal;
    this.sectionName = sectionName;
    this.registrationNumber = registrationNumber;
    this.sectionCode = sectionCode;
    this.instructor = instructor;
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
    this.roomNumber = roomNumber;
    this.prerequisites = prerequisites;
  }

  public @NotNull int getRegistrationNumber() { return registrationNumber; }

  public @NotNull String getSectionCode() { return sectionCode; }

  public @NotNull String getInstructor() { return instructor; }

  public @NotNull SectionType getType() { return type; }

  public @NotNull SectionStatus getStatus() { return status; }

  public @NotNull List<Meeting> getMeetings() { return meetings; }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public List<Section> getRecitations() {
    return recitations;
  }

  public String getCampus() { return campus; }

  public void setCampus(String campus) { this.campus = campus; }

  public String getSectionName() { return sectionName; }

  public void setSectionName(String sectionName) {
    this.sectionName = sectionName;
  }

  public Integer getWaitlistTotal() { return waitlistTotal; }

  public void setWaitlistTotal(Integer waitlistTotal) {
    this.waitlistTotal = waitlistTotal;
  }

  public String getDescription() { return description; }

  public double getMinUnits() { return minUnits; }

  public double getMaxUnits() { return maxUnits; }

  public void setMaxUnits(double maxUnits) { this.maxUnits = maxUnits; }

  public String getInstructionMode() { return instructionMode; }

  public String getGrading() { return grading; }

  public void setGrading(String grading) { this.grading = grading; }

  public String getRoomNumber() { return roomNumber; }

  public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

  public String getPrerequisites() { return prerequisites; }

  public void setPrerequisites(String prerequisites) {
    this.prerequisites = prerequisites;
  }
}
