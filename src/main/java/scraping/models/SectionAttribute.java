package scraping.models;

import nyu.SectionStatus;

import javax.validation.constraints.NotNull;

// Duplicate variables can be removed later on

/**
 * SectionAttribute to holds data for scraping catalogs' sections
 */
public class SectionAttribute {
  private String sectionName;
  private int registrationNumber; // dup
  private SectionStatus status;   // dup
  private String campus;
  private String description;
  private String instructionMode;
  private String[] instructors; // dup
  private Float minUnits;
  private Float maxUnits;
  private String grading;
  private String location;
  private String notes;
  private String prerequisites;

  public SectionAttribute(String sectionName, int registrationNumber,
                          SectionStatus status, String campus,
                          String description, String instructorMode,
                          String[] instructors, Float minUnits, Float maxUnits,
                          String grading, String notes, String prerequisites,
                          String location) {
    this.sectionName = sectionName;
    this.registrationNumber = registrationNumber;
    this.status = status;
    this.campus = campus;
    this.description = description;
    this.instructionMode = instructorMode;
    this.instructors = instructors;
    this.minUnits = minUnits;
    this.maxUnits = maxUnits;
    this.grading = grading;
    this.notes = notes;
    this.prerequisites = prerequisites;
    this.location = location;
  }

  public String getSectionName() { return sectionName; }

  public @NotNull int getRegistrationNumber() { return registrationNumber; }

  public @NotNull SectionStatus getStatus() { return status; }

  public @NotNull String getCampus() { return campus; }

  public @NotNull String getDescription() { return description; }

  public String getInstructionMode() { return instructionMode; }

  public String[] getInstructors() { return instructors; }

  public Float getMinUnits() { return minUnits; }

  public Float getMaxUnits() { return maxUnits; }

  public String getGrading() { return grading; }

  public String getNotes() { return notes; }

  public String getPrerequisites() { return prerequisites; }

  public String getLocation() { return location; }
}
