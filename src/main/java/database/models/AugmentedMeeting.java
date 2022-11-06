package database.models;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Locale;
import utils.Nyu;

// A meeting plus section information
public class AugmentedMeeting {
  public final String subject;
  public final String deptCourseId;

  public final String sectionCode;
  public final int registrationNumber;
  public final String sectionType;
  public final Nyu.SectionStatus sectionStatus;
  public final String instructionMode;
  public final String location;
  public final LocalDateTime beginDate;
  public final int minutesDuration;
  public final LocalDateTime endDate;

  public static DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss", Locale.US);

  public AugmentedMeeting(ResultSet rs) throws SQLException {
    subject = rs.getString("subject_code");
    deptCourseId = rs.getString("dept_course_id");
    registrationNumber = rs.getInt("registration_number");
    sectionCode = rs.getString("section_code");
    sectionType = rs.getString("section_type");
    sectionStatus = Nyu.SectionStatus.valueOf(rs.getString("section_status"));
    location = rs.getString("location");
    instructionMode = rs.getString("instruction_mode");
    beginDate = rs.getTimestamp("begin_date").toLocalDateTime();
    endDate = rs.getTimestamp("end_date").toLocalDateTime();
    minutesDuration = rs.getInt("duration");
  }

  public String getBeginDate() {
    var zoned = beginDate.atZone(ZoneOffset.UTC);
    return DateTimeFormatter.ISO_INSTANT.format(zoned);
  }

  public String getEndDate() {
    var zoned = endDate.atZone(ZoneOffset.UTC);
    return DateTimeFormatter.ISO_INSTANT.format(zoned);
  }

  public int getMinutesDuration() { return minutesDuration; }

  public int getMinutesInDay() {
    return beginDate.get(ChronoField.MINUTE_OF_DAY);
  }

  @Override
  public String toString() {
    return "AugmentedMeeting{"
        + "subject=" + subject + ", deptCourseId='" + deptCourseId + '\'' +
        ", sectionCode='" + sectionCode + '\'' +
        ", registrationNumber=" + registrationNumber +
        ", sectionType=" + sectionType + ", sectionStatus=" + sectionStatus +
        ", instructionMode='" + instructionMode + '\'' + ", location='" +
        location + '\'' + ", beginDate=" + beginDate +
        ", minutesDuration=" + minutesDuration + ", endDate=" + endDate + '}';
  }
}
