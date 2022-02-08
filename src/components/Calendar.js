import React, { Fragment } from "react";
import styled from "styled-components";
import { times, days } from "components/constants";

export default function Calendar({ renderCourses }) {
  return (
    <CalendarContainer>
      <TimeGrid>
        {times.map((time, i) => (
          <Time key={i}> {time} </Time>
        ))}
      </TimeGrid>
      <CourseCalendar>
        {Object.keys(days).map((dayNumber, i) => {
          //ignoring Saturday and Sunday for now
          return days[dayNumber] === "Sunday" ||
            days[dayNumber] === "Saturday" ? (
            <Fragment key={i} />
          ) : (
            <CalendarDay key={i}>
              {days[dayNumber]}
              {renderCourses(dayNumber)}
            </CalendarDay>
          );
        })}
        {/* The calendar is generated using a grid. The time range is from 8:00 to
        21:00 so we need 13 rows. Currently we are having 5 days from Mon-Fri.
        So we need to generate 13 * 5 = 65 grid tile. */}
        {Array(65)
          .fill(1)
          .map((item, i) => {
            return <CalendarDay key={i} />;
          })}
      </CourseCalendar>
    </CalendarContainer>
  );
}

const CalendarContainer = styled.div`
  min-height: 100vh;
  padding: 1rem;
  display: flex;
`;

const CourseCalendar = styled.div`
  width: 100%;
  display: grid;
  grid-template-columns: repeat(5, 12rem);
  grid-template-rows: 48px repeat(13, 4rem);
  @media (max-width: 1000px) {
    grid-template-columns: 1fr;
  }
`;

const CalendarDay = styled.div`
  width: 100%;
  min-height: 2vh;
  padding: 15px;
  border-bottom: 1px dashed var(--grey400);
  text-align: center;
  align-items: center;

  @media (max-width: 1000px) {
    min-height: 150px;
  }
`;

const TimeGrid = styled.div`
  width: 5%;
  display: grid;
  grid-template-columns: auto;
  grid-template-rows: 40px repeat(13, 4rem);
`;

const Time = styled.div`
  text-align: right;
  font-size: 1rem;
  padding: 0 5px 0 0;
`;
