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

  public String getSectionName() { return sectionName; }
  public int getRegistrationNumber() { return registrationNumber; }
  public String getSectionCode() { return sectionCode; }
  public String getInstructor() { return instructor; }
  public SectionType getType() { return type; }
  public SectionStatus getStatus() { return status; }
  public List<Meeting> getMeetings() { return meetings; }
  public List<Section> getRecitations() { return recitations; }
  public Integer getWaitlistTotal() { return waitlistTotal; }
}