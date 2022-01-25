package nyu;

import java.sql.*;

public class Meeting {
  public Timestamp beginDate;  // contains date and time of first event.
  public long minutesDuration; // Duration of meeting
  public Timestamp endDate;    // When the meeting stops repeating
}
