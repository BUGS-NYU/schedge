package database.models;

import com.fasterxml.jackson.annotation.*;
import io.javalin.openapi.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import utils.Nyu;

// A meeting plus section information
@JsonIgnoreProperties(
    value = {"minutesInDay", "endDateLocal", "beginDateLocal"},
    allowGetters = true)
public class AugmentedMeeting {
  public final String subject;
  public final String deptCourseId;

  public final String sectionCode;
  public final int registrationNumber;
  public final String sectionType;
  public final Nyu.SectionStatus sectionStatus;
  public final String instructionMode;
  public final String location;
  public final ZonedDateTime beginDate;
  public final int minutesDuration;
  public final String campus;

  @JsonIgnore
  public final ZoneId tz;
  public final ZonedDateTime endDate;

  public static DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss", Locale.US);

  public AugmentedMeeting(ResultSet rs) throws SQLException {
    subject = rs.getString("subject_code");
    deptCourseId = rs.getString("dept_course_id");
    registrationNumber = rs.getInt("registration_number");
    sectionCode = rs.getString("section_code");
    sectionType = rs.getString("section_type");
    sectionStatus = Nyu.SectionStatus.valueOf(rs.getString("section_status"));
    campus = rs.getString("campus");
    location = rs.getString("location");
    instructionMode = rs.getString("instruction_mode");
    beginDate =
        rs.getTimestamp("begin_date").toLocalDateTime().atZone(ZoneOffset.UTC);
    endDate =
        rs.getTimestamp("end_date").toLocalDateTime().atZone(ZoneOffset.UTC);
    minutesDuration = rs.getInt("duration");

    tz = Nyu.Campus.timezoneForCampus(campus);
  }

  @JsonCreator()
  public AugmentedMeeting(
      @JsonProperty("subject") String subject,
      @JsonProperty("deptCourseId") String deptCourseId,
      @JsonProperty("campus") String campus,
      @JsonProperty("sectionCode") String sectionCode,
      @JsonProperty("registrationNumber") int registrationNumber,
      @JsonProperty("sectionType") String sectionType,
      @JsonProperty("sectionStatus") Nyu.SectionStatus sectionStatus,
      @JsonProperty("instructionMode") String instructionMode,
      @JsonProperty("location") String location,
      @JsonProperty("beginDate") String beginDate,
      @JsonProperty("minutesDuration") int minutesDuration,
      @JsonProperty("endDate") String endDate) {
    this.subject = subject;
    this.campus = campus;
    this.deptCourseId = deptCourseId;
    this.sectionCode = sectionCode;
    this.registrationNumber = registrationNumber;
    this.sectionType = sectionType;
    this.sectionStatus = sectionStatus;
    this.instructionMode = instructionMode;
    this.location = location;

    this.beginDate = Nyu.Meeting.parseTime(beginDate);
    this.minutesDuration = minutesDuration;
    this.endDate = Nyu.Meeting.parseTime(endDate);

    tz = Nyu.Campus.timezoneForCampus(campus);
  }

  public String getSubject() { return subject; }
  public String getDeptCourseId() { return deptCourseId; }
  public String getCampus() { return campus; }

  public String getSectionCode() { return sectionCode; }
  public int getRegistrationNumber() { return registrationNumber; }
  public String getSectionType() { return sectionType; }

  @OpenApiPropertyType(definedBy = String.class)
  public Nyu.SectionStatus getSectionStatus() {
    return sectionStatus;
  }
  public String getInstructionMode() { return instructionMode; }
  public String getLocation() { return location; }

  @NotNull
  public String getBeginDate() {
    return DateTimeFormatter.ISO_INSTANT.format(beginDate);
  }

  @NotNull
  public String getEndDate() {
    return DateTimeFormatter.ISO_INSTANT.format(endDate);
  }

  @NotNull
  public String getBeginDateLocal() {
    return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
        beginDate.withZoneSameInstant(tz));
  }

  @NotNull
  public String getEndDateLocal() {
    return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
        endDate.withZoneSameInstant(tz));
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
