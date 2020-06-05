package scraping.models;

import java.time.LocalDateTime;

public class Meeting {
  public final LocalDateTime
      beginDate;                      // contains date and time of first event.
  public final Long duration;         // Duration of meeting in minutes
  public final LocalDateTime endDate; // When the meeting stops repeating

  public Meeting(LocalDateTime beginDate, Long minutesDuration,
                 LocalDateTime endDate) {
    this.beginDate = beginDate;
    this.duration = minutesDuration;
    this.endDate = endDate;
  }
}
