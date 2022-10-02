package database.models;

import java.sql.*;
import java.util.List;
import types.*;

public class Row {
  public final int courseId;
  public final String name;
  public final String subject;
  public final String deptCourseId;

  public final int sectionId;
  public final int registrationNumber;
  public final String sectionCode;
  public final String[] instructors;
  public final Nyu.SectionType sectionType;
  public final Nyu.SectionStatus sectionStatus;
  public final Integer associatedWith;
  public final Integer waitlistTotal;
  public final List<Nyu.Meeting> meetings;

  public final String sectionName;
  public final Double minUnits;
  public final Double maxUnits;
  public final String location;
  public final String instructionMode;

  public Row(ResultSet rs, List<Nyu.Meeting> meetings) throws SQLException {
    this.courseId = rs.getInt("id");
    this.name = rs.getString("name");
    this.subject = rs.getString("subject_code");
    this.deptCourseId = rs.getString("dept_course_id");
    this.sectionId = rs.getInt("section_id");
    this.registrationNumber = rs.getInt("registration_number");
    this.sectionCode = rs.getString("section_code");

    Array instructorArray = rs.getArray("instructors");
    String[] instructors = null;
    if (instructorArray != null) {
      instructors = (String[])instructorArray.getArray();
    }

    if (instructors == null || instructors.length == 0) {
      instructors = new String[] {"Staff"};
    }

    this.instructors = instructors;

    this.sectionType = Nyu.SectionType.valueOf(rs.getString("section_type"));
    this.sectionStatus = Nyu.SectionStatus.valueOf(rs.getString("section_status"));

    int associatedWith = rs.getInt("associated_with");
    this.associatedWith = rs.wasNull() ? null : associatedWith;

    this.meetings = meetings;

    int waitListTotal = rs.getInt("waitlist_total");
    this.waitlistTotal = rs.wasNull() ? null : waitListTotal;

    this.sectionName = rs.getString("section_name");
    this.minUnits = rs.getDouble("min_units");
    this.maxUnits = rs.getDouble("max_units");
    this.location = rs.getString("location");
    this.instructionMode = rs.getString("instruction_mode");
  }
}
