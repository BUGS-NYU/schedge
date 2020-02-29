package scraping.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
import nyu.SectionStatus;
import nyu.SectionType;
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
  private String campus = null;
  private String description = null;
  private Double minUnits = null;
  private Double maxUnits = null;
  private String instructionMode = null;
  private String grading = null;
  private String roomNumber = null;
  private String prerequisites = null;

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

  public void update(String rawData) {
    Map<String, String> map = ParseSection.update(rawData);
    this.sectionName = map.get("sectionName");
    this.campus = map.getOrDefault("Location", "");
    this.description = map.getOrDefault("Description", "");
    if (map.get("minUnits") != null &&
        !((map.get("minUnits")).trim().equals(""))) {
      this.minUnits = Double.parseDouble(map.getOrDefault("minUnits", "0"));
    }
    if (map.get("maxUnits") != null &&
        !((map.get("maxUnits")).trim().equals(""))) {
      this.maxUnits = Double.parseDouble(map.getOrDefault("maxUnits", "0"));
    }
    this.instructionMode = map.getOrDefault("Instruction Mode", "In-Person");
    this.grading = map.getOrDefault("Grading", "");
    this.roomNumber = map.getOrDefault("Room", "");
    this.prerequisites =
        map.getOrDefault("Notes", "See Description. None otherwise");
  }

  public String getSectionName() { return sectionName; }
  public int getRegistrationNumber() { return registrationNumber; }
  public Double getMinUnits() { return minUnits; }
  public Double getMaxUnits() { return maxUnits; }
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