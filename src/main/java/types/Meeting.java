package types;

import com.fasterxml.jackson.annotation.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;

public class Meeting {
  private static DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public LocalDateTime beginDate; // contains date and time of first event.
  public int minutesDuration;     // Duration of meeting
  public LocalDateTime endDate;   // When the meeting stops repeating

  // we eventually want to switch to using the `fromCode(String code)` version
  // for the JSON creator. It will happen when we fully move away from scraping
  // from Schedge V1.
  //                                  - Albert Liu, Feb 03, 2022 Thu 01:02 EST
  @JsonCreator
  public static Meeting
  fromJson(@JsonProperty("beginDate") String beginDate,
           @JsonProperty("minutesDuration") int minutesDuration,
           @JsonProperty("endDate") String endDate) {
    Meeting meeting = new Meeting();
    meeting.beginDate = LocalDateTime.parse(beginDate, formatter);
    meeting.minutesDuration = minutesDuration;
    meeting.endDate = LocalDateTime.parse(endDate, formatter);

    return meeting;
  }

  public String getBeginDate() {
    ZonedDateTime zoned = beginDate.atZone(ZoneOffset.UTC);

    return DateTimeFormatter.ISO_INSTANT.format(zoned);
  }

  public String getEndDate() {
    ZonedDateTime zoned = endDate.atZone(ZoneOffset.UTC);

    return DateTimeFormatter.ISO_INSTANT.format(zoned);
  }
}
