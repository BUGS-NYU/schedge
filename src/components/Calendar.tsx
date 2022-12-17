import React from "react";
import styles from "./css/calendar.module.css";
import { useQuery } from "react-query";
import { z } from "zod";
import axios from "axios";
import { usePageState } from "./state";
import { StringDateSchema } from "./types";

const times = [
  "",
  "8:00",
  "9:00",
  "10:00",
  "11:00",
  "12:00",
  "13:00",
  "14:00",
  "15:00",
  "16:00",
  "17:00",
  "18:00",
  "19:00",
  "20:00",
  "21:00",
];

const days = {
  su: "Sunday",
  mo: "Monday",
  tu: "Tuesday",
  we: "Wednesday",
  th: "Thursday",
  fr: "Friday",
  sa: "Saturday",
} as const;

const CalendarMeetingSchema = z.object({
  minutesDuration: z.number(),
  subject: z.string(),
  deptCourseId: z.string(),
  sectionCode: z.string(),
  registrationNumber: z.number(),
  sectionType: z.string(),
  minutesInDay: z.number(),
  instructionMode: z.string(),
  beginDate: StringDateSchema,
  location: z.string(),
});

type CalendarMeeting = z.infer<typeof CalendarMeetingSchema>;

const CalendarSchema = z.object({
  mo: z.array(CalendarMeetingSchema),
  tu: z.array(CalendarMeetingSchema),
  we: z.array(CalendarMeetingSchema),
  th: z.array(CalendarMeetingSchema),
  fr: z.array(CalendarMeetingSchema),
  sa: z.array(CalendarMeetingSchema),
  su: z.array(CalendarMeetingSchema),
});

const ScheduleSchema = CalendarSchema.extend({
  valid: z.boolean(),
  conflictA: CalendarMeetingSchema.optional(),
  conflictB: CalendarMeetingSchema.optional(),
});

function addMinutes(date: Date, minutes: number): Date {
  return new Date(date.getTime() + minutes * 60000);
}

interface MeetingProps {
  meeting: CalendarMeeting;
}
export const ScheduleCourse: React.VFC<MeetingProps> = ({ meeting }) => {
  const computeMargin = (parsedDate: Date) => {
    // The start time is 8:00. The size of each grid block is 4rem. We need a margin
    // top of: (currenthours - 8:00) * 4rem + (currentMinutes/60) * 4rem.
    // We add 1rem extra to ensure no collision between block during rendering
    return (
      (parsedDate.getHours() - 8) * 4 + (parsedDate.getMinutes() / 60) * 4 + 1
    );
  };

  const endDate = addMinutes(meeting.beginDate, meeting.minutesDuration);
  const begin = meeting.beginDate.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });
  const end = endDate.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });

  return (
    <div
      className={styles.courseBlock}
      style={{
        minHeight: `${(meeting.minutesDuration / 60) * 4}rem`,
        marginTop: `${computeMargin(meeting.beginDate)}rem`,
      }}
    >
      <div className={styles.textContainer}>
        <div className="courseCode">
          {`${meeting.subject} ${meeting.deptCourseId}-${meeting.sectionCode}`}
        </div>
      </div>
      <div className="time">
        {begin} - {end}
      </div>
      <div className="location">{meeting.location}</div>
    </div>
  );
};

interface Props {
  registrationNumbers: string[];
}
export const Calendar: React.VFC<Props> = ({ registrationNumbers }) => {
  const { term } = usePageState();
  const { data: schedule, isLoading } = useQuery(
    ["schedule", term.code, ...registrationNumbers],
    async () => {
      console.log(registrationNumbers);
      if (registrationNumbers.length === 0) {
        return undefined;
      }
      const resp = await axios.get(`/api/generateSchedule/${term.code}`, {
        params: {
          registrationNumbers: registrationNumbers.join(","),
        },
      });

      const parsed = ScheduleSchema.parse(resp.data);
      return parsed;
    }
  );

  return (
    <div className={styles.calendar}>
      <div className={styles.timeGrid}>
        {times.map((time, i) => (
          <div className={styles.time} key={i}>
            {" "}
            {time}{" "}
          </div>
        ))}
      </div>
      <div className={styles.course}>
        {Object.entries(days).map(([day, name], i) => {
          // ignoring Saturday and Sunday for now
          if (day == "su" || day == "sa") {
            return null;
          }

          const meetings = schedule?.[day] ?? [];
          return (
            <div className={styles.calendarDay} key={day}>
              {name}
              {meetings.map((meeting, i) => {
                return <ScheduleCourse meeting={meeting} key={i} />;
              })}
            </div>
          );
        })}

        {/* The calendar is generated using a grid. The time range is from 8:00 to
        21:00 so we need 13 rows. Currently we are having 5 days from Mon-Fri.
        So we need to generate 13 * 5 = 65 grid tile. */}
        {Array(65)
          .fill(1)
          .map((item, i) => {
            return <div className={styles.calendarDay} key={i} />;
          })}
      </div>
    </div>
  );
};
