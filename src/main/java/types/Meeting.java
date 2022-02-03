package types;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.*;

public class Meeting {
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  public Timestamp beginDate; // contains date and time of first event.

  public int minutesDuration; // Duration of meeting

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  public Timestamp endDate; // When the meeting stops repeating
}
