package scraping.models;

import javax.validation.constraints.NotNull;
import nyu.SectionStatus;

// Duplicate variables can be removed later on

/**
 * SectionAttribute to holds data for scraping catalogs' sections
 */
public class SectionAttribute {
  public final String sectionName;
  public final int registrationNumber; // dup
  public final SectionStatus status;   // dup
  public final String campus;
  public final String description;
  public final String instructionMode;
  public final String[] instructors; // dup
  public final Double minUnits;
  public final Double maxUnits;
  public final String grading;
  public final String location;
  public final String notes;
  public final String prerequisites;

  public SectionAttribute(String sectionName, int registrationNumber,
                          SectionStatus status, String campus,
                          String description, String instructorMode,
                          String[] instructors, Double minUnits,
                          Double maxUnits, String grading, String notes,
                          String prerequisites, String location) {
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
}
