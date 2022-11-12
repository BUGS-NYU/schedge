import React from "react";
import styles from "./date-section.module.css";
import {
  parseDate,
  addMinutes,
  isEqualTime,
  convertToLocaleTimeStr,
} from "components/util";
import { days } from "components/constants";

export default function DateSection({ sortedSectionMeetings }) {
  const parsedMeetings = sortedSectionMeetings.map((meeting) => {
    const parsedDate = parseDate(meeting.beginDate);
    return {
      startTime: parsedDate,
      minutesDuration: meeting.minutesDuration,
      endTime: addMinutes(parsedDate, meeting.minutesDuration),
    };
  });

  return (
    <>
      {/* Sections with one meeting a week */}
      {sortedSectionMeetings.length === 1 && (
        <div className={styles.dateContainer}>
          <div className={styles.boldedDate}>
            {days[parsedMeetings[0].startTime.getDay()]}s{" "}
          </div>
          from{" "}
          <div className={styles.boldedDate}>
            {convertToLocaleTimeStr(parsedMeetings[0].startTime)}{" "}
          </div>
          to{" "}
          <div className={styles.boldedDate}>
            {convertToLocaleTimeStr(parsedMeetings[0].endTime)}
          </div>
        </div>
      )}
      {/* Sections with two identical meetings a week */}
      {sortedSectionMeetings.length === 2 &&
        isEqualTime(parsedMeetings[0].startTime, parsedMeetings[1].startTime) &&
        parsedMeetings[0].minutesDuration ===
          parsedMeetings[1].minutesDuration && (
          <div className={styles.dateContainer}>
            <div className={styles.boldedDate}>
              {days[parsedMeetings[0].startTime.getDay()]}s{" "}
            </div>
            and{" "}
            <div className={styles.boldedDate}>
              {days[parsedMeetings[1].startTime.getDay()]}s{" "}
            </div>
            from{" "}
            <div className={styles.boldedDate}>
              {convertToLocaleTimeStr(parsedMeetings[0].startTime)}{" "}
            </div>
            to{" "}
            <div className={styles.boldedDate}>
              {convertToLocaleTimeStr(parsedMeetings[1].endTime)}
            </div>
          </div>
        )}
      {/* Section with two different meetings a week */}
      {sortedSectionMeetings.length === 2 &&
        !(
          isEqualTime(
            parsedMeetings[0].startTime,
            parsedMeetings[1].startTime
          ) &&
          parsedMeetings[0].minutesDuration ===
            parsedMeetings[1].minutesDuration
        ) && (
          <div className={styles.dateContainer}>
            <div className={styles.boldedDate}>
              {days[parsedMeetings[0].startTime.getDay()]}s{" "}
            </div>
            from{" "}
            <div className={styles.boldedDate}>
              {convertToLocaleTimeStr(parsedMeetings[0].startTime)}{" "}
            </div>
            to{" "}
            <div className={styles.boldedDate}>
              {convertToLocaleTimeStr(parsedMeetings[0].endTime)}
            </div>
            {" and "}
            <div className={styles.boldedDate}>
              {days[parsedMeetings[0].startTime.getDay()]}s{" "}
            </div>
            from{" "}
            <div className={styles.boldedDate}>
              {convertToLocaleTimeStr(parsedMeetings[1].startTime)}{" "}
            </div>
            to{" "}
            <div className={styles.boldedDate}>
              {convertToLocaleTimeStr(parsedMeetings[1].endTime)}
            </div>
          </div>
        )}
      {/* Sections with more than two meetings a week */}
      {sortedSectionMeetings.length > 2 && (
        <div className={styles.dateContainer}>
          {parsedMeetings.map((meeting, i) => (
            <React.Fragment key={i}>
              <div className={styles.boldedDate}>
                {days[meeting.startTime.getDay()]}s{" "}
              </div>
              from{" "}
              <div className={styles.boldedDate}>
                {convertToLocaleTimeStr(meeting.startTime)}{" "}
              </div>
              to{" "}
              <div className={styles.boldedDate}>
                {convertToLocaleTimeStr(meeting.endTime)}
              </div>
              {i < sortedSectionMeetings.length - 1 && ", "}
              <br />
            </React.Fragment>
          ))}
        </div>
      )}
    </>
  );
}
