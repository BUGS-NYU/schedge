package database.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Locale;
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
  public final LocalDateTime beginDate;
  public final int minutesDuration;
  public final LocalDateTime endDate;

  public static DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss", Locale.US);

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
    beginDate = rs.getTimestamp("begin_date").toLocalDateTime();
    endDate = rs.getTimestamp("end_date").toLocalDateTime();
    minutesDuration = rs.getInt("duration");
  }

  public String getSectionName() { return sectionName; }

  public SubjectCode getSubject() { return subject; }

  public String getDeptCourseId() { return deptCourseId; }

  public String getSectionCode() { return sectionCode; }

  public int getRegistrationNumber() { return registrationNumber; }

  public SectionType getSectionType() { return sectionType; }

  public SectionStatus getSectionStatus() { return sectionStatus; }

  public String getInstructionMode() { return instructionMode; }

  public String getLocation() { return location; }
  public String getBeginDate() { return beginDate.format(formatter); }

  public int getMinutesDuration() { return minutesDuration; }

  public int getMinutesInDay() { return beginDate.get(ChronoField.MINUTE_OF_DAY); }

  public String getEndDate() { return endDate.format(formatter); }

  @Override
  public String toString() {
    return "AugmentedMeeting{"
        + "sectionName='" + sectionName + '\'' + ", subject=" + subject +
        ", deptCourseId='" + deptCourseId + '\'' + ", sectionCode='" +
        sectionCode + '\'' + ", registrationNumber=" + registrationNumber +
        ", sectionType=" + sectionType + ", sectionStatus=" + sectionStatus +
        ", instructionMode='" + instructionMode + '\'' + ", location='" +
        location + '\'' + ", beginDate=" + beginDate +
        ", minutesDuration=" + minutesDuration + ", endDate=" + endDate + '}';
  }
}
