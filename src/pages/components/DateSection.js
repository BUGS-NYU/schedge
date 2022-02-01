import React from "react";
import PropTypes from "prop-types";
import styled from "styled-components";
import { grey } from "@material-ui/core/colors";
import {
  parseDate,
  addMinutes,
  isEqualTime,
  convertToLocaleTimeStr,
} from "../utils";
import { days } from "../constants";

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
        <DateContainer>
          <BoldedDate>
            {days[parsedMeetings[0].startTime.getDay()]}s{" "}
          </BoldedDate>
          from{" "}
          <BoldedDate>
            {convertToLocaleTimeStr(parsedMeetings[0].startTime)}{" "}
          </BoldedDate>
          to{" "}
          <BoldedDate>
            {convertToLocaleTimeStr(parsedMeetings[0].endTime)}
          </BoldedDate>
        </DateContainer>
      )}
      {/* Sections with two identical meetings a week */}
      {sortedSectionMeetings.length === 2 &&
        isEqualTime(parsedMeetings[0].startTime, parsedMeetings[1].startTime) &&
        parsedMeetings[0].minutesDuration ===
          parsedMeetings[1].minutesDuration && (
          <DateContainer>
            <BoldedDate>
              {days[parsedMeetings[0].startTime.getDay()]}s{" "}
            </BoldedDate>
            and{" "}
            <BoldedDate>
              {days[parsedMeetings[1].startTime.getDay()]}s{" "}
            </BoldedDate>
            from{" "}
            <BoldedDate>
              {convertToLocaleTimeStr(parsedMeetings[0].startTime)}{" "}
            </BoldedDate>
            to{" "}
            <BoldedDate>
              {convertToLocaleTimeStr(parsedMeetings[1].endTime)}
            </BoldedDate>
          </DateContainer>
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
          <DateContainer>
            <BoldedDate>
              {days[parsedMeetings[0].startTime.getDay()]}s{" "}
            </BoldedDate>
            from{" "}
            <BoldedDate>
              {convertToLocaleTimeStr(parsedMeetings[0].startTime)}{" "}
            </BoldedDate>
            to{" "}
            <BoldedDate>
              {convertToLocaleTimeStr(parsedMeetings[0].endTime)}
            </BoldedDate>
            {" and "}
            <BoldedDate>
              {days[parsedMeetings[0].startTime.getDay()]}s{" "}
            </BoldedDate>
            from{" "}
            <BoldedDate>
              {convertToLocaleTimeStr(parsedMeetings[1].startTime)}{" "}
            </BoldedDate>
            to{" "}
            <BoldedDate>
              {convertToLocaleTimeStr(parsedMeetings[1].endTime)}
            </BoldedDate>
          </DateContainer>
        )}
      {/* Sections with more than two meetings a week */}
      {sortedSectionMeetings.length > 2 && (
        <DateContainer>
          {parsedMeetings.map((meeting, i) => (
            <React.Fragment key={i}>
              <BoldedDate>{days[meeting.startTime.getDay()]}s </BoldedDate>
              from{" "}
              <BoldedDate>
                {convertToLocaleTimeStr(meeting.startTime)}{" "}
              </BoldedDate>
              to{" "}
              <BoldedDate>{convertToLocaleTimeStr(meeting.endTime)}</BoldedDate>
              {i < sortedSectionMeetings.length - 1 && ", "}
              <br />
            </React.Fragment>
          ))}
        </DateContainer>
      )}
    </>
  );
}

DateSection.propTypes = {
  sortedSectionMeetings: PropTypes.array.isRequired,
};

const DateContainer = styled.div`
  color: ${grey[800]};
  margin: -0.2rem 0 1rem 1rem;
  font-size: 1.25rem;
`;

const BoldedDate = styled.span`
  font-weight: bold;
`;
