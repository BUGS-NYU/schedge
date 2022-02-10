package types;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.*;
import java.time.*;
import java.time.format.*;

public class Meeting {

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  public LocalDateTime beginDate; // contains date and time of first event.

  public int minutesDuration; // Duration of meeting

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  public LocalDateTime endDate; // When the meeting stops repeating

  public String getBeginDate() {
    ZonedDateTime zoned = beginDate.atZone(ZoneOffset.UTC);

    return DateTimeFormatter.ISO_INSTANT.format(zoned);
  }

  public String getEndDate() {
    ZonedDateTime zoned = endDate.atZone(ZoneOffset.UTC);

    return DateTimeFormatter.ISO_INSTANT.format(zoned);
  }
}
