package nyu;

public class Meeting {
  public final String beginDate;    // contains date and time of first event.
  public final int minutesDuration; // Duration of meeting
  public final String endDate;      // When the meeting stops repeating

  public Meeting(String beginDate, String minutesDuration, String endDate) {
    this.beginDate = beginDate;
    this.minutesDuration = Integer.parseInt(minutesDuration);
    this.endDate = endDate;
  }
}
