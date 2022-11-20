import React, { useState } from "react";
import Attributes from "./Attributes";
import DateSection from "./DateSection";
import styles from "./section.module.css";
import { Recitation } from "components/Recitation";
import {
  convertUnits,
  splitLocation,
  changeStatus,
  styleStatus,
  parseDate,
} from "components/util";
import localStorageContainer from "components/localStorage";
import { useSchedule } from "../pages/schedule";
import type { Meeting, Section } from "../pages/subject";

interface Props {
  section: Section;
  sortedSectionMeetings: Meeting[];
  courseData: any;
  lastSection: any;
}

export const SectionInfo: React.VFC<Props> = ({
  section,
  sortedSectionMeetings,
  courseData,
  lastSection,
}) => {
  const [expandedList, setExpandedList] = useState({});
  const { addToWishlist } = useSchedule();

  const handleExpandList = (event, registrationNumber) => {
    event.preventDefault();
    let newLs = { ...expandedList };
    if (registrationNumber in expandedList) {
      newLs[registrationNumber] = !expandedList[registrationNumber];
    } else {
      newLs[registrationNumber] = true;
    }
    setExpandedList(newLs);
  };

  const handleOnClick = (course) => {
    const localStorage = new localStorageContainer();
    const courses = localStorage.getState("wishlist");
    courses.push(course);
    localStorage.saveState({ wishlist: courses });
  };

  return (
    <div
      className={styles.sectionContainer}
      style={{ borderBottom: !lastSection && "1px solid" }}
    >
      {courseData.sections.length > 1 && (
        <h4 className="sectionNum">{section.code}</h4>
      )}
      <Attributes
        instructors={section.instructors}
        building={splitLocation(section.location).Building}
        room={splitLocation(section.location).Room}
        units={convertUnits(section.minUnits, section.maxUnits)}
        status={section.status}
        type={section.type}
        registrationNumber={section.registrationNumber}
      />
      {!courseData.sections.every(
        (section) => section.notes === courseData.sections[0].notes
      ) && <div className={styles.sectionDescription}>{section.notes}</div>}

      {sortedSectionMeetings && (
        <DateSection sortedSectionMeetings={sortedSectionMeetings} />
      )}
      <div className={styles.utilBar}>
        {section.recitations !== undefined && section.recitations.length !== 0 && (
          <button
            className={styles.expandButton}
            onClick={(e) => handleExpandList(e, section.registrationNumber)}
            onKeyPress={(e) => handleExpandList(e, section.registrationNumber)}
            tabIndex={0}
          >
            <span style={{}}>Show Recitations</span>
          </button>
        )}
        <div className={styles.statusContainer}>
          <div
            style={{
              color: styleStatus(section.status),
            }}
          />
          <span
            style={{
              color: styleStatus(section.status),
            }}
          >
            {changeStatus(section)}
          </span>
        </div>
        <button
          className={styles.wishlistButton}
          onClick={() => addToWishlist(section)}
        >
          <div style={{}} />
          <span style={{}}>Add to Wishlist</span>
        </button>
      </div>
      <div>
        {section.recitations &&
          section.recitations.map((recitation, i) => {
            const sortedRecitationsMeetings = recitation.meetings
              ? recitation.meetings.sort(
                  (a, b) =>
                    parseDate(a.beginDate).getDay() -
                    parseDate(b.beginDate).getDay()
                )
              : [];
            return (
              <Recitation
                key={i}
                recitation={recitation}
                sortedRecitationsMeetings={sortedRecitationsMeetings}
                courseName={courseData.name}
                lastRecitation={i === section.recitations.length - 1}
              />
            );
          })}
      </div>
    </div>
  );
};
