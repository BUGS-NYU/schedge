package scraping.models;

import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonInclude;
import models.SectionStatus;
import models.SectionType;

import java.util.List;

public class Section {
  private int registrationNumber;
  private String sectionCode;
  private String instructor;
  private SectionType type;
  private SectionStatus status;
  private List<Meeting> meetings;
  private List<Section> recitations;
  private String sectionName;
  private Integer waitlistTotal;

  public Section(int registrationNumber, String sectionCode, String instructor,
                 SectionType type, SectionStatus status, List<Meeting> meetings,
                 List<Section> recitations, String sectionName, Integer waitlistTotal) {

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

  public Integer getWaitlistTotal() { return waitlistTotal; }

  public @NotNull String getSectionName() { return sectionName; }

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
}
