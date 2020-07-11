package database.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import nyu.SectionStatus;
import nyu.SectionType;
import nyu.SubjectCode;

// A meeting plus section information
public class AugmentedMeeting {
  public final String sectionName;
  public final SubjectCode subject;
  public final String deptCourseId;

  public final String sectionCode;
  public final int registrationNumber;
  public final SectionType sectionType;
  public final SectionStatus sectionStatus;
  public final String instructionMode;
  public final String location;
  public final int dayOfWeek;
  public final int minuteBegin;
  public final long minutesDuration;

  public AugmentedMeeting(ResultSet rs) throws SQLException {
    subject = new SubjectCode(rs.getString("subject"), rs.getString("school"));
    deptCourseId = rs.getString("dept_course_id");
    registrationNumber = rs.getInt("registration_number");
    sectionCode = rs.getString("section_code");
    sectionType = SectionType.values()[rs.getInt("section_type")];
    sectionStatus = SectionStatus.values()[rs.getInt("section_status")];
    sectionName = rs.getString("section_name");
    location = rs.getString("location");
    instructionMode = rs.getString("instruction_mode");
    LocalTime beginDate = rs.getTime("begin_date").toLocalTime();
    dayOfWeek = beginDate.get(ChronoField.DAY_OF_WEEK);
    minuteBegin = beginDate.get(ChronoField.MINUTE_OF_DAY);
    minutesDuration = rs.getLong("duration");
  }
}
