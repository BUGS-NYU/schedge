package nyu;

import com.fasterxml.jackson.annotation.JsonValue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.validation.constraints.NotNull;
import utils.JsonSerializable;

public class Meeting implements JsonSerializable {
  private LocalDateTime
      beginDate; // Begin date; contains date and time of first event.
  private Long minutesDuration;  // Duration of meeting
  private LocalDateTime endDate; // When the meeting stops repeating

  public static final DateTimeFormatter beginFormatter =
      DateTimeFormatter.ofPattern("MM/dd/yyyy h:mma", Locale.ENGLISH);
  public static final DateTimeFormatter endFormatter =
      DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH);

  public Meeting(LocalDateTime beginDate, Long minutesDuration,
                 LocalDateTime endDate) {
    this.beginDate = beginDate;
    this.minutesDuration = minutesDuration;
    this.endDate = endDate;
  }

  public @NotNull LocalDateTime getBeginDate() { return beginDate; }

  public @NotNull Long getMinutesDuration() { return minutesDuration; }

  public @NotNull LocalDateTime getEndDate() { return endDate; }

  @JsonValue
  public @NotNull MeetingJson toJson() {
    return new MeetingJson(beginDate.toString(), minutesDuration,
                           endDate.toString());
  }

  @Override
  public void toJson(StringBuilder s) {
    s.append(String.format(
        "{\"beginDate\":\"%s\",\"duration\":\"%s\",\"endDate\":\"%s\"}",
        beginDate, minutesDuration, endDate));
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
