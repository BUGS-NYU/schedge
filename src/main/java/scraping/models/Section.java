package scraping.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import javax.validation.constraints.NotNull;
import models.SectionStatus;
import models.SectionType;
import services.ParseCatalog;
import services.ParseSection;

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
  private float minUnits;
  private float maxUnits;
  private String instructionMode;
  private String grading;
  private String roomNumber;
  private String prerequisites;

  public Section(int registrationNumber, String sectionCode, String instructor,
                 SectionType type, SectionStatus status, List<Meeting> meetings,
                 List<Section> recitations, String sectionName,
                 Integer waitlistTotal) {
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
  }

  public Future<Void> update(String rawData) {
    Map<String, String> map = ParseSection.update(rawData);
    this.sectionName = map.get("sectionName");
    this.campus = map.get("Location");
    this.description = map.get("Description");
    if(map.get("minUnits") != null && !((map.get("minUnits")).trim().equals(""))) {
      this.minUnits = Float.parseFloat(map.get("minUnits"));
    }
    if(map.get("maxUnits") != null && !((map.get("maxUnits")).trim().equals(""))) {
      this.maxUnits = Float.parseFloat(map.get("maxUnits"));
    }
    this.instructionMode = map.get("Instruction Mode");
    this.grading = map.get("Grading");
    this.roomNumber = map.get("Location");
    this.prerequisites = map.getOrDefault("Notes", "See Description. None otherwise");
    return new CompletableFuture<>();
  }

  public String getSectionName() { return sectionName; }
  public int getRegistrationNumber() { return registrationNumber; }
  public float getMinUnits() { return minUnits; }
  public float getMaxUnits() { return maxUnits; }
  public String getSectionCode() { return sectionCode; }
  public String getInstructor() { return instructor; }
  public SectionType getType() { return type; }
  public SectionStatus getStatus() { return status; }
  public List<Meeting> getMeetings() { return meetings; }
  public String getCampus() { return campus; }
  public String getDescription() { return description; }
  public String getInstructionMode() { return instructionMode; }
  public String getGrading() { return grading; }
  public String getRoomNumber() { return roomNumber; }
  public String getPrerequisites() { return prerequisites; }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public List<Section> getRecitations() {
    return recitations;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public Integer getWaitlistTotal() {
    return waitlistTotal;
  }
}