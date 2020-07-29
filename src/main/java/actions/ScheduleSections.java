package actions;

import com.fasterxml.jackson.annotation.JsonInclude;
import database.models.AugmentedMeeting;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;

public final class ScheduleSections {
  private ScheduleSections() {}

  public static final class Schedule {
    public final boolean valid;
    public final ArrayList<AugmentedMeeting> mo;
    public final ArrayList<AugmentedMeeting> tu;
    public final ArrayList<AugmentedMeeting> we;
    public final ArrayList<AugmentedMeeting> th;
    public final ArrayList<AugmentedMeeting> fr;
    public final ArrayList<AugmentedMeeting> sa;
    public final ArrayList<AugmentedMeeting> su;
    public final AugmentedMeeting conflictA;
    public final AugmentedMeeting conflictB;

    public Schedule() {
      valid = false;
      mo = tu = we = th = fr = sa = su = null;
      conflictA = conflictB = null;
    }

    public Schedule(AugmentedMeeting a, AugmentedMeeting b) {
      mo = tu = we = th = fr = sa = su = null;
      valid = false;
      conflictA = a;
      conflictB = b;
    }

    public Schedule(ArrayList<AugmentedMeeting> meetings) {
      meetings.sort(Comparator.comparingInt(AugmentedMeeting::getMinutesInDay));
      valid = true;
      mo = new ArrayList<>();
      tu = new ArrayList<>();
      we = new ArrayList<>();
      th = new ArrayList<>();
      fr = new ArrayList<>();
      sa = new ArrayList<>();
      su = new ArrayList<>();
      conflictA = conflictB = null;

      for (AugmentedMeeting meeting : meetings) {
        switch (meeting.beginDate.get(ChronoField.DAY_OF_WEEK)) {
        case 1:
          mo.add(meeting);
          break;
        case 2:
          tu.add(meeting);
          break;
        case 3:
          we.add(meeting);
          break;
        case 4:
          th.add(meeting);
          break;
        case 5:
          fr.add(meeting);
          break;
        case 6:
          sa.add(meeting);
          break;
        case 7:
          su.add(meeting);
          break;
        }
      }
    }

    public boolean getValid() { return valid; }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ArrayList<AugmentedMeeting> getMo() {
      return mo;
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ArrayList<AugmentedMeeting> getTu() {
      return tu;
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ArrayList<AugmentedMeeting> getWe() {
      return we;
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ArrayList<AugmentedMeeting> getTh() {
      return th;
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ArrayList<AugmentedMeeting> getFr() {
      return fr;
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ArrayList<AugmentedMeeting> getSa() {
      return sa;
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ArrayList<AugmentedMeeting> getSu() {
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

  public static Schedule
  generateSchedule(ArrayList<AugmentedMeeting> meetings) {
    if (meetings.size() == 0 || meetings == null) {
      return new Schedule();
    }

    for (int i = 0; i < meetings.size(); i++) {
      for (int j = i + 1; j < meetings.size(); j++) {
        if (meetingsCollide(meetings.get(i), meetings.get(j)))
          return new Schedule(meetings.get(i), meetings.get(j));
      }
    }
    return new Schedule(meetings);
  }

  private static boolean meetingsCollide(AugmentedMeeting a,
                                         AugmentedMeeting b) {
    if (a.beginDate.isAfter(b.endDate) || b.beginDate.isAfter(a.endDate))
      return false;

    for (LocalDateTime aDate = a.beginDate, bDate = b.beginDate;
         aDate.isBefore(a.endDate) && bDate.isBefore(b.endDate);) {

      int aDay = aDate.get(ChronoField.DAY_OF_WEEK);
      int bDay = bDate.get(ChronoField.DAY_OF_WEEK);

      if (aDay != bDay)
        return false;

      int aBegin = aDate.get(ChronoField.SECOND_OF_DAY);
      int bBegin = bDate.get(ChronoField.SECOND_OF_DAY);
      int aEnd = a.minutesDuration + aBegin;
      int bEnd = b.minutesDuration + bBegin;

      if (aBegin < bEnd && bBegin < aEnd) {
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
