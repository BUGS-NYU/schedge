package actions;

import com.fasterxml.jackson.annotation.*;
import database.models.AugmentedMeeting;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import org.jetbrains.annotations.*;

public final class ScheduleSections {
  private ScheduleSections() {}

  public static final class Schedule {
    public boolean valid = true;
    public final ArrayList<AugmentedMeeting> mo;
    public final ArrayList<AugmentedMeeting> tu;
    public final ArrayList<AugmentedMeeting> we;
    public final ArrayList<AugmentedMeeting> th;
    public final ArrayList<AugmentedMeeting> fr;
    public final ArrayList<AugmentedMeeting> sa;
    public final ArrayList<AugmentedMeeting> su;

    public AugmentedMeeting conflictA;
    public AugmentedMeeting conflictB;

    public Schedule(
        @JsonProperty("valid") boolean valid,
        @JsonProperty("mo") ArrayList<AugmentedMeeting> mo,
        @JsonProperty("tu") ArrayList<AugmentedMeeting> tu,
        @JsonProperty("we") ArrayList<AugmentedMeeting> we,
        @JsonProperty("th") ArrayList<AugmentedMeeting> th,
        @JsonProperty("fr") ArrayList<AugmentedMeeting> fr,
        @JsonProperty("sa") ArrayList<AugmentedMeeting> sa,
        @JsonProperty("su") ArrayList<AugmentedMeeting> su,
        @JsonProperty("conflictA") AugmentedMeeting conflictA,
        @JsonProperty("conflictB") AugmentedMeeting conflictB) {
      this.valid = valid;
      this.mo = mo;
      this.tu = tu;
      this.we = we;
      this.th = th;
      this.fr = fr;
      this.sa = sa;
      this.su = su;
      this.conflictA = conflictA;
      this.conflictB = conflictB;
    }

    public Schedule() {
      mo = new ArrayList<>();
      tu = new ArrayList<>();
      we = new ArrayList<>();
      th = new ArrayList<>();
      fr = new ArrayList<>();
      sa = new ArrayList<>();
      su = new ArrayList<>();
    }

    public boolean getValid() {
      return valid;
    }

    @JsonIgnore
    public int size() {
      var size = 0;
      size += mo.size();
      size += tu.size();
      size += we.size();
      size += th.size();
      size += fr.size();
      size += sa.size();
      size += su.size();

      return size;
    }

    @NotNull public ArrayList<AugmentedMeeting> getMo() {
      return mo;
    }

    @NotNull public ArrayList<AugmentedMeeting> getTu() {
      return tu;
    }

    @NotNull public ArrayList<AugmentedMeeting> getWe() {
      return we;
    }

    @NotNull public ArrayList<AugmentedMeeting> getTh() {
      return th;
    }

    @NotNull public ArrayList<AugmentedMeeting> getFr() {
      return fr;
    }

    @NotNull public ArrayList<AugmentedMeeting> getSa() {
      return sa;
    }

    @NotNull public ArrayList<AugmentedMeeting> getSu() {
      return su;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public AugmentedMeeting getConflictA() {
      return conflictA;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public AugmentedMeeting getConflictB() {
      return conflictB;
    }
  }

  public static Schedule generateSchedule(ArrayList<AugmentedMeeting> meetings) {
    var schedule = new Schedule();
    if (meetings == null) {
      return schedule;
    }

    meetings.sort(Comparator.comparingInt(AugmentedMeeting::getMinutesInDay));
    for (AugmentedMeeting meeting : meetings) {
      switch (meeting.beginDate.get(ChronoField.DAY_OF_WEEK)) {
        case 1:
          schedule.mo.add(meeting);
          break;
        case 2:
          schedule.tu.add(meeting);
          break;
        case 3:
          schedule.we.add(meeting);
          break;
        case 4:
          schedule.th.add(meeting);
          break;
        case 5:
          schedule.fr.add(meeting);
          break;
        case 6:
          schedule.sa.add(meeting);
          break;
        case 7:
          schedule.su.add(meeting);
          break;
      }
    }

    for (int i = 0; i < meetings.size(); i++) {
      var a = meetings.get(i);
      for (int j = i + 1; j < meetings.size(); j++) {
        var b = meetings.get(j);
        if (meetingsCollide(a, b)) {
          schedule.conflictA = a;
          schedule.conflictB = b;
          schedule.valid = false;

          return schedule;
        }
      }
    }

    return schedule;
  }

  private static boolean meetingsCollide(AugmentedMeeting a, AugmentedMeeting b) {
    if (a.beginDate.isAfter(b.endDate) || b.beginDate.isAfter(a.endDate)) return false;

    int aDay = a.beginDate.get(ChronoField.DAY_OF_WEEK);
    int bDay = b.beginDate.get(ChronoField.DAY_OF_WEEK);
    if (aDay != bDay) return false;

    var aBeginLocal = a.beginDate.withZoneSameInstant(a.tz).toLocalDateTime();
    var bBeginLocal = b.beginDate.withZoneSameInstant(b.tz).toLocalDateTime();
    var aEndLocal = a.endDate.withZoneSameInstant(a.tz).toLocalDateTime();
    var bEndLocal = b.endDate.withZoneSameInstant(b.tz).toLocalDateTime();

    for (LocalDateTime aDate = aBeginLocal, bDate = bBeginLocal;
        aDate.isBefore(aEndLocal) && bDate.isBefore(bEndLocal); ) {
      var aDateEnd = aDate.plus(Duration.ofMinutes(a.minutesDuration));
      var bDateEnd = bDate.plus(Duration.ofMinutes(b.minutesDuration));

      if (bDateEnd.atZone(b.tz).isAfter(aDate.atZone(a.tz))
          && aDateEnd.atZone(a.tz).isAfter(bDate.atZone(b.tz))) {
        return true;
      }

      if (aDate.isBefore(bDate)) {
        aDate = aDate.plus(1, ChronoUnit.WEEKS);
      } else {
        bDate = bDate.plus(1, ChronoUnit.WEEKS);
      }
    }

    return false;
  }
}
