import React, { useState } from "react";
import styled from "styled-components";
import Attributes from "./Attributes";
import DateSection from "./DateSection";
import Recitation from "components/Recitation";
import {
  convertUnits,
  splitLocation,
  changeStatus,
  styleStatus,
  parseDate,
} from "components/util";
import localStorageContainer from "components/localStorage";

function Section({
  year,
  semester,
  section,
  sortedSectionMeetings,
  courseData,
  lastSection,
}) {
  const [expandedList, setExpandedList] = useState({});

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
    <SectionContainer lastSection={lastSection}>
      {courseData.name !== section.name && (
        <h3 className="sectionName">{section.name}</h3>
      )}
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
      ) && <SectionDescription>{section.notes}</SectionDescription>}

      {sortedSectionMeetings && (
        <DateSection sortedSectionMeetings={sortedSectionMeetings} />
      )}
      <UtilBar>
        {section.recitations !== undefined && section.recitations.length !== 0 && (
          <ExpandButton
            onClick={(e) => handleExpandList(e, section.registrationNumber)}
            onKeyPress={(e) => handleExpandList(e, section.registrationNumber)}
            role="button"
            tabIndex={0}
          >
            <span style={{}}>Show Recitations</span>
          </ExpandButton>
        )}
        <StatusContainer>
          <div
            style={{
              color: styleStatus(section.status),
            }}
          />
          <span style={{ color: styleStatus(section.status) }}>
            {changeStatus(section)}
          </span>
        </StatusContainer>
        <WishlistButton onClick={() => handleOnClick(section)}>
          <div style={{}} />
          <span style={{}}>Add to Wishlist</span>
        </WishlistButton>
      </UtilBar>
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
                year={year}
                semester={semester}
                lastRecitation={i === section.recitations.length - 1}
              />
            );
          })}
      </div>
    </SectionContainer>
  );
}

const SectionContainer = styled.div`
  padding: 1.8vmin 2.8vmin;
  background-color: var(--grey100);
  width: 84%;
  margin-left: 8%;
  position: relative;
  border-bottom: ${(props) => (props.lastSection ? "" : "1px solid")};

  & > .sectionName {
    font-size: 1.8rem;
    font-family: var(--condensedFont);
    color: var(--grey800);
    margin-bottom: 0.25rem;
  }

  & > .sectionNum {
    font-size: 1.6rem;
    font-family: var(--condensedFont);
    color: var(--grey700);
    margin: 0 0 -1rem 1rem;
  }

  & > .attributes {
    display: flex;
    flex-wrap: wrap;
  }
`;

const ExpandButton = styled.div`
  font-size: 1.1rem;
  height: 100%;
  width: 12rem;
  border-radius: 0.6rem;
  padding: 0.8rem 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-right: 2rem;
  transition: 0.1s;

  :hover {
  }

  & > svg {
    margin-right: 0.65rem;
  }
`;

const SectionDescription = styled.div`
  padding: 0 1.5rem 1.5rem 0.5rem;
  max-width: 68%;
  color: var(--grey700);
`;

const UtilBar = styled.div`
  padding: 0.5rem;
  height: 6vh;
  display: flex;
  justify-content: flex-start;
  align-items: center;
`;

const StatusContainer = styled.div`
  font-size: 1.1rem;
  height: 100%;
  width: 12rem;
  border-radius: 0.6rem;
  padding: 0.8rem 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 2rem;

  & > svg {
    margin-right: 0.65rem;
  }
`;

const WishlistButton = styled.div`
  font-size: 1.1rem;
  height: 100%;
  width: 12rem;
  border-radius: 0.6rem;
  padding: 0.8rem 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-right: 2rem;
  transition: 0.1s;

  :hover {
  }

  & > svg {
    margin-right: 0.65rem;
  }
`;

export default Section;
