package nyu;

import utils.JsonSerializable;

public class Meeting implements JsonSerializable {
  public final String beginDate;     // contains date and time of first event.
  public final long minutesDuration; // Duration of meeting
  public final String endDate;       // When the meeting stops repeating

  public Meeting(String beginDate, String minutesDuration, String endDate) {
    this.beginDate = beginDate;
    this.minutesDuration = Long.parseLong(minutesDuration);
    this.endDate = endDate;
  }

  public String getBeginDate() { return beginDate; }

  public long getMinutesDuration() { return minutesDuration; }

  public String getEndDate() { return endDate; }

  @Override
  public void toJson(StringBuilder s) {
    s.append(String.format(
        "{\"beginDate\":\"%s\",\"duration\":\"%s\",\"endDate\":\"%s\"}",
        beginDate, minutesDuration, endDate));
  }
}
