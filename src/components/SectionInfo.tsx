import React, { useState } from "react";
import Attributes from "./Attributes";
import styles from "./section.module.css";
import {
  convertUnits,
  splitLocation,
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
            <div className={styles.boldedDate}>
              {daysToStr[days[meeting.startTime.getDay()]]}
            </div>{" "}
            from{" "}
            <div className={styles.boldedDate}>
              {convertToLocaleTimeStr(meeting.startTime)}
            </div>{" "}
            to{" "}
            <div className={styles.boldedDate}>
              {convertToLocaleTimeStr(meeting.endTime)}
            </div>
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

export const SectionInfo: React.VFC<Props> = ({ section, lastSection }) => {
  const [expanded, setExpanded] = useState(false);
  const { addToWishlist } = useSchedule();

  return (
    <div
      className={styles.sectionContainer}
      style={{ borderBottom: !lastSection && "1px solid" }}
    >
      {section.sectionName && <h3 className="sectionName">{section.name}</h3>}
      <h4 className="sectionNum">{section.code}</h4>

      <Attributes
        instructors={section.instructors}
        building={splitLocation(section.location).Building}
        room={splitLocation(section.location).Room}
        units={convertUnits(section.minUnits, section.maxUnits)}
        status={section.status}
        type={section.type}
        registrationNumber={section.registrationNumber}
      />

      <div className={styles.statusContainer}>
        <div style={{ color: styleStatus(section.status) }} />
        <span style={{ color: styleStatus(section.status) }}>
          {changeStatus(section)}
        </span>
      </div>

      <div className={styles.sectionDescription}>{section.notes}</div>

      {section.meetings && <DateSection section={section} />}

      <div className={styles.utilBar}>
        {!!section.recitations?.length && (
          <button
            className={styles.expandButton}
            onClick={(e) => setExpanded((prev) => !prev)}
          >
            Show Recitations
          </button>
        )}

        <button
          className={styles.wishlistButton}
          onClick={() => addToWishlist(section)}
        >
          <div style={{}} />
          <span style={{}}>Add to Wishlist</span>
        </button>
      </div>

      <div>
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
