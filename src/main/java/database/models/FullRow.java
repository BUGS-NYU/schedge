package database.models;

import java.sql.*;
import java.util.List;
import nyu.*;

public class FullRow {
  public final int courseId;
  public final String name;
  public final SubjectCode subject;
  public final String deptCourseId;

  public final int sectionId;
  public final int registrationNumber;
  public final String sectionCode;
  public final String[] instructors;
  public final SectionType sectionType;
  public final SectionStatus sectionStatus;
  public final Integer associatedWith;
  public final Integer waitlistTotal;
  public final List<Meeting> meetings;

  public final String sectionName;
  public final String campus;
  public final String description;
  public final String instructionMode;
  public final Double minUnits;
  public final Double maxUnits;
  public final String grading;
  public final String location;
  public final String notes;
  public final String prerequisites;

  public FullRow(ResultSet rs, List<Meeting> meetings) throws SQLException {
    courseId = rs.getInt("id");
    name = rs.getString("name");
    subject = new SubjectCode(rs.getString("subject"), rs.getString("school"));
    deptCourseId = rs.getString("dept_course_id");
    sectionId = rs.getInt("section_id");
    registrationNumber = rs.getInt("registration_number");
    sectionCode = rs.getString("section_code");
    String instructorString = rs.getString("section_instructors");
    instructors = instructorString.equals("") ? new String[] {"Staff"}
                                              : instructorString.split(";");

    sectionType = SectionType.values()[rs.getInt("section_type")];
    sectionStatus = SectionStatus.values()[rs.getInt("section_status")];
    int associatedWith = rs.getInt("associated_with");
    this.associatedWith = rs.wasNull() ? null : associatedWith;
    this.meetings = meetings;
    int waitListTotal = rs.getInt("waitlist_total");
    this.waitlistTotal = rs.wasNull() ? null : waitListTotal;
    sectionName = rs.getString("section_name");
    campus = rs.getString("campus");
    description = rs.getString("description");
    minUnits = rs.getDouble("min_units");
    maxUnits = rs.getDouble("max_units");
    instructionMode = rs.getString("instruction_mode");
    grading = rs.getString("grading");
    location = rs.getString("location");
    notes = rs.getString("notes");
    prerequisites = rs.getString("prerequisites");
  }
}
