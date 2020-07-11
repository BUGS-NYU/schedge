package actions;

import database.models.AugmentedMeeting;
import java.time.DayOfWeek;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

public final class ScheduleSections {
  private ScheduleSections() {}

  public static final class Schedule {
    public final ArrayList<AugmentedMeeting> mo;
    public final ArrayList<AugmentedMeeting> tu;
    public final ArrayList<AugmentedMeeting> we;
    public final ArrayList<AugmentedMeeting> th;
    public final ArrayList<AugmentedMeeting> fr;
    public final ArrayList<AugmentedMeeting> sa;
    public final ArrayList<AugmentedMeeting> su;
    public final AugmentedMeeting conflictA;
    public final AugmentedMeeting conflictB;

    public Schedule(AugmentedMeeting a, AugmentedMeeting b) {
      mo = tu = we = th = fr = sa = su = null;
      conflictA = a;
      conflictB = b;
    }

    public Schedule(AugmentedMeeting... meetings) {
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
  }

  public static Schedule
  generateSchedule(ArrayList<AugmentedMeeting> meetings) {
    return null;
  }
}
