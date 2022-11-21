import React, { useState } from "react";
import styles from "./section.module.css";
import cx from "classnames";
import {
  changeStatus,
  styleStatus,
  addMinutes,
  convertToLocaleTimeStr,
} from "components/util";
import { AugmentedSection, useSchedule } from "../pages/schedule";
import { days, daysToStr } from "./constants";

interface DateProps {
  section: AugmentedSection;
}

const DateSection: React.VFC<DateProps> = ({ section }) => {
  const meetings = React.useMemo(() => {
    const sortedSectionMeetings = [...(section.meetings ?? [])].sort(
      (a, b) => a.beginDate.getDay() - b.beginDate.getDay()
    );
    const parsedMeetings = sortedSectionMeetings.map((meeting) => {
      return {
        startTime: meeting.beginDate,
        minutesDuration: meeting.minutesDuration,
        endTime: addMinutes(meeting.beginDate, meeting.minutesDuration),
      };
    });
    return parsedMeetings;
  }, [section.meetings]);

  return (
    <div>
      {meetings.map((meeting, index) => {
        return (
          <div key={index} className={styles.dateContainer}>
            <span className={styles.boldedDate}>
              {daysToStr[days[meeting.startTime.getDay()]]}
            </span>{" "}
            from{" "}
            <span className={styles.boldedDate}>
              {convertToLocaleTimeStr(meeting.startTime)}
            </span>{" "}
            to{" "}
            <span className={styles.boldedDate}>
              {convertToLocaleTimeStr(meeting.endTime)}
            </span>
          </div>
        );
      })}
    </div>
  );
};

interface Props {
  section: AugmentedSection;
  lastSection: boolean;
}

const SectionAttribute: React.FC<{ label: string }> = ({ label, children }) => {
  return (
    <div className={styles.sectionAttribute}>
      <h5>{label}</h5>
      <div className={styles.sectionAttrValue}>{children}</div>
    </div>
  );
};

export const SectionInfo: React.VFC<Props> = ({ section, lastSection }) => {
  const [expanded, setExpanded] = useState(false);
  const { addToWishlist } = useSchedule();

  return (
    <div
      className={cx(
        styles.sectionContainer,
        !lastSection && styles.sectionBorder
      )}
    >
      {section.sectionName && (
        <h3 className={styles.sectionName}>{section.name}</h3>
      )}
      <h4 className={styles.sectionNum}>
        {section.type} {section.code}
      </h4>

      <div className={styles.attributeContainer}>
        <SectionAttribute label="Registration Number">
          #{section.registrationNumber}
        </SectionAttribute>

        <SectionAttribute label="Status">
          <span style={{ color: styleStatus(section.status) }}>
            {changeStatus(section)}
          </span>
        </SectionAttribute>

        <SectionAttribute label="Location">
          {section.location ?? "TBA"}
        </SectionAttribute>

        <SectionAttribute label="Credits">
          {section.minUnits === section.maxUnits
            ? `${section.minUnits}`
            : `${section.minUnits} - ${section.maxUnits}`}
        </SectionAttribute>
      </div>

      <SectionAttribute label="instructors">
        {section.instructors.map((instructor) => (
          <span key={instructor}>{instructor}</span>
        ))}
      </SectionAttribute>

      {section.notes && (
        <div className={styles.sectionDescription}>{section.notes}</div>
      )}

      {section.meetings && <DateSection section={section} />}

      <div className={styles.utilBar}>
        {!!section.recitations?.length && (
          <button
            className={styles.expandButton}
            onClick={(e) => setExpanded((prev) => !prev)}
          >
            {expanded ? "Hide" : "Show"} Recitations
          </button>
        )}

        <button
          className={styles.wishlistButton}
          onClick={() => addToWishlist(section)}
        >
          Add to Wishlist
        </button>
      </div>

      <div className={styles.recitationBox}>
        {!!section.recitations?.length &&
          expanded &&
          section.recitations.map((recitation, i) => {
            return (
              <SectionInfo
                key={i}
                lastSection={i === section.recitations.length - 1}
                section={{
                  ...recitation,
                  name: recitation.name ?? section.name,
                  sectionName: recitation.name,
                  deptCourseId: section.deptCourseId,
                  subjectCode: section.subjectCode,
                }}
              />
            );
          })}
      </div>
    </div>
  );
};
