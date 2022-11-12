import React, { Fragment } from "react";
import styles from "./calendar.module.css";
import { times, days } from "components/constants";

export default function Calendar({ renderCourses }) {
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
        {Object.keys(days).map((dayNumber, i) => {
          //ignoring Saturday and Sunday for now
          return days[dayNumber] === "Sunday" ||
            days[dayNumber] === "Saturday" ? (
            <Fragment key={i} />
          ) : (
            <div className={styles.calendarDay} key={i}>
              {days[dayNumber]}
              {renderCourses(dayNumber)}
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
}
