import React from "react";
import styles from "./schedule-course.module.css";
import { parseDate, addMinutes } from "components/util";

export default function ScheduleCourse({ course }) {
  const computeMargin = (startTime) => {
    const parsedDate = parseDate(startTime);
    // The start time is 8:00. The size of each grid block is 4rem. We need a margin
    // top of: (currenthours - 8:00) * 4rem + (currentMinutes/60) * 4rem.
    // We add 1rem extra to ensure no collision between block during rendering
    return (
      (parsedDate.getHours() - 8) * 4 + (parsedDate.getMinutes() / 60) * 4 + 1
    );
  };

  return (
    <div
      className={styles.courseBlock}
      style={{
        minHeight: `${(course.minutesDuration / 60) * 4}rem`,
        marginTop: `${computeMargin(course.beginDate)}rem`,
      }}
    >
      <div className={styles.textContainer}>
        <div className="courseCode">
          {`${course.subject.code}-${course.subject.school} ${course.deptCourseId}-${course.sectionCode}`}
        </div>
      </div>
      <div className="time">
        {`${parseDate(course.beginDate).toLocaleTimeString([], {
          hour: "2-digit",
          minute: "2-digit",
        })}
              - ${addMinutes(
                parseDate(course.beginDate),
                course.minutesDuration
              ).toLocaleTimeString([], {
                hour: "2-digit",
                minute: "2-digit",
              })}`}
      </div>
      <div className="location">{course.location}</div>
    </div>
  );
}
