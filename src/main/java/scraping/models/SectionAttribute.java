package scraping.models;

import javax.validation.constraints.NotNull;
import nyu.SectionStatus;

// Duplicate variables can be removed later on

/**
 * SectionAttribute to holds data for scraping catalogs' sections
 */
public class SectionAttribute {
  private String courseName;
  private int registrationNumber; // dup
  private SectionStatus status;   // dup
  private String campus;
  private String description;
  private String instructionMode;
  private String instructor; // dup
  private Double minUnits;
  private Double maxUnits;
  private String grading;
  private String room;
  private String prerequisites;
  // private List<Meeting> meetings; //dup. Ignore for now

  public SectionAttribute(String courseName, int registrationNumber,
                          SectionStatus status, String campus,
                          String description, String instructorMode,
                          String instructor, Double minUnits, Double maxUnits,
                          String grading, String prerequisites, String room) {
    this.courseName = courseName;
    this.registrationNumber = registrationNumber;
    this.status = status;
    this.campus = campus;
    this.description = description;
    this.instructionMode = instructorMode;
    this.instructor = instructor;
    this.minUnits = minUnits;
    this.maxUnits = maxUnits;
    this.grading = grading;
    this.prerequisites = prerequisites;
    this.room = room;
  }

  public String getCourseName() { return courseName; }

  public @NotNull int getRegistrationNumber() { return registrationNumber; }

  public @NotNull SectionStatus getStatus() { return status; }

  public @NotNull String getCampus() { return campus; }

  public @NotNull String getDescription() { return description; }

  public String getInstructionMode() { return instructionMode; }

  public @NotNull String getInstructor() { return instructor; }

  public Double getMinUnits() { return minUnits; }

  public Double getMaxUnits() { return maxUnits; }

  public String getGrading() { return grading; }

  public String getPrerequisites() { return prerequisites; }

  public String getRoom() { return room; }
}
