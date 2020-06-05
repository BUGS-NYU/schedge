package database.models;

import static database.generated.Tables.*;

import database.generated.tables.Courses;
import database.generated.tables.Instructors;
import database.generated.tables.Sections;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jdk.dynalink.NamedOperation;
import nyu.Meeting;
import nyu.SectionStatus;
import nyu.SectionType;
import nyu.SubjectCode;
import org.jooq.Record;

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
        instructionMode = rs.getString("instructionMode");
        grading = rs.getString("grading");
        location = rs.getString("location");
        notes = rs.getString("notes");
        prerequisites = rs.getString("prerequisites");
    }

  public FullRow(Record row, List<Meeting> meetings) {
    courseId = row.get(COURSES.ID);
    name = row.get(COURSES.NAME);
    subject =
        new SubjectCode(row.get(COURSES.SUBJECT), row.get(COURSES.SCHOOL));
    deptCourseId = row.get(COURSES.DEPT_COURSE_ID);

    sectionId = row.get(SECTIONS.ID);
    registrationNumber = row.get(SECTIONS.REGISTRATION_NUMBER);
    sectionCode = row.get(SECTIONS.SECTION_CODE);
    String instructorString = (String)row.get("section_instructors");
    instructors = instructorString.equals("") ? new String[] {"Staff"}
                                              : instructorString.split(";");
    sectionType = SectionType.values()[row.get(SECTIONS.SECTION_TYPE)];
    sectionStatus = SectionStatus.values()[row.get(SECTIONS.SECTION_STATUS)];
    associatedWith = row.get(SECTIONS.ASSOCIATED_WITH);
    this.meetings = meetings;
    waitlistTotal = row.get(SECTIONS.WAITLIST_TOTAL);
    sectionName = row.get(SECTIONS.NAME);
    campus = row.get(SECTIONS.CAMPUS);
    description = row.get(COURSES.DESCRIPTION);
    minUnits = row.get(SECTIONS.MIN_UNITS);
    maxUnits = row.get(SECTIONS.MAX_UNITS);
    instructionMode = row.get(SECTIONS.INSTRUCTION_MODE);
    grading = row.get(SECTIONS.GRADING);
    location = row.get(SECTIONS.LOCATION);
    notes = row.get(SECTIONS.NOTES);
    prerequisites = row.get(SECTIONS.PREREQUISITES);
  }
}
