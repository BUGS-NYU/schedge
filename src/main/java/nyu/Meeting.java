package nyu;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.validation.constraints.NotNull;
import utils.JsonSerializable;

public class Meeting implements JsonSerializable {
  private String
      beginDate; // Begin date; contains date and time of first event.
  private Long minutesDuration; // Duration of meeting
  private String endDate;       // When the meeting stops repeating

  public static final DateTimeFormatter beginFormatter =
      DateTimeFormatter.ofPattern("MM/dd/yyyy h:mma", Locale.ENGLISH);
  public static final DateTimeFormatter endFormatter =
      DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH);

  public Meeting(String beginDate, String minutesDuration, String endDate) {
    this.beginDate = beginDate;
    this.minutesDuration = Long.parseLong(minutesDuration);
    this.endDate = endDate;
  }

  public @NotNull String getBeginDate() { return beginDate; }

  public @NotNull Long getMinutesDuration() { return minutesDuration; }

  public @NotNull String getEndDate() { return endDate; }

  @Override
  public void toJson(StringBuilder s) {
    s.append(String.format(
        "{\"beginDate\":\"%s\",\"duration\":\"%s\",\"endDate\":\"%s\"}",
        beginDate, minutesDuration, endDate));
  }
}
