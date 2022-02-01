import React from "react";
import PropTypes from "prop-types";
import styled from "styled-components";
import { parseDate, addMinutes } from "../utils";

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
    <CourseBlock
      style={{
        minHeight: `${(course.minutesDuration / 60) * 4}rem`,
        marginTop: `${computeMargin(course.beginDate)}rem`,
      }}
    >
      <TextContainer>
        <div className="courseCode">
          {`${course.subject.code}-${course.subject.school} ${course.deptCourseId}-${course.sectionCode}`}
        </div>
      </TextContainer>
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
    </CourseBlock>
  );
}

ScheduleCourse.propTypes = {
  course: PropTypes.object.isRequired,
};

const CourseBlock = styled.div`
  background-color: var(--grey400);
  width: 10rem;
  min-height: 4rem;
  padding: 5px 5px 5px 10px;
  border-radius: 10px;
  position: absolute;

  & > .time {
    font-size: 0.8rem;
    text-align: center;
    padding: 0 2px;
    width: 100%;
  }

  & > .location {
    font-size: 0.8rem;
    text-align: center;
    padding: 0 2px;
    width: 100%;
  }
`;

const TextContainer = styled.div`
  display: flex;
  justify-content: space-between;

  & > .courseCode {
    font-size: 0.9rem;
    text-align: center;
    padding: 0 2px;
    width: 100%;
  }

  & > .closeButton {
    cursor: pointer;
    &:hover {
      color: var(--grey100);
    }
  }
`;
