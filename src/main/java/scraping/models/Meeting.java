package scraping.models;

import com.fasterxml.jackson.annotation.JsonValue;
import javax.validation.constraints.NotNull;
import org.joda.time.DateTime;

public class Meeting {
  private DateTime
      beginDate; // Begin date; contains date and time of first event.
  private Long minutesDuration; // Duration of meeting
  private DateTime endDate;     // When the meeting stops repeating

  public Meeting(DateTime beginDate, Long minutesDuration, DateTime endDate) {
    this.beginDate = beginDate;
    this.minutesDuration = minutesDuration;
    this.endDate = endDate;
  }

  public @NotNull DateTime getBeginDate() { return beginDate; }

  public @NotNull Long getMinutesDuration() { return minutesDuration; }

  public @NotNull DateTime getEndDate() { return endDate; }

  @JsonValue
  public @NotNull MeetingJson toJson() {
    return new MeetingJson(beginDate.toString("MM/dd/yyyy h:mma"),
                           minutesDuration, endDate.toString("MM/dd/yyyy"));
  }

  class MeetingJson {

    private String beginDate;
    private long duration;
    private String endDate;

    MeetingJson(String beginDate, long duration, String endDate) {
      this.beginDate = beginDate;
      this.duration = duration;
      this.endDate = endDate;
    }

    public @NotNull String getBeginDate() { return beginDate; }

    public long getDuration() { return duration; }

    public @NotNull String getEndDate() { return endDate; }
  }
}
