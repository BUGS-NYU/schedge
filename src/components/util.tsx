import React from "react";
import cx from "classnames";
import { daysToStr } from "./constants";
import css from "./util.module.css";
import { Section } from "../pages/subject";

export const timeout = (ms) => new Promise((res) => setTimeout(res, ms));

export async function post(url, data) {
  const resp = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  return resp.json();
}

export async function get(urlString, query) {
  const queryString = new URLSearchParams(query).toString();
  if (queryString) {
    urlString += "?" + queryString;
  }

  const resp = await fetch(urlString);

  return resp.json();
}

const backgroundColor = (bg) => {
  switch (bg) {
    case "lightGray":
      return css.bgLightGray;
    case "white":
      return css.bgWhite;
    default:
      return undefined;
  }
};

export const Scroll = ({ children, ...props }) => {
  const style = { height: props.height };
  const outerClass = cx(css.rounded, backgroundColor(props.background));
  const innerClass = cx(css.scrollColImpl, props.flexBox ? css.col : null);

  let inner;
  switch (props.tag) {
    case "pre":
      inner = <pre className={innerClass}>{children}</pre>;
      break;

    case "div":
    default:
      inner = <div className={innerClass}>{children}</div>;
      break;
  }

  return (
    <div className={outerClass} style={style}>
      {inner}
    </div>
  );
};

export const Btn = ({ children, ...props }) => {
  const className = cx(css.muiButton, backgroundColor(props.background));

  const clickHandler = (evt) => {
    if (!props.propagate) evt.stopPropagation();
    if (!props.preventDefault) evt.preventDefault();

    props.onClick?.();
  };

  return (
    <button className={className} onClick={clickHandler}>
      {children}
    </button>
  );
};

export function convertUnits(minUnit, maxUnit) {
  if (minUnit === 0) {
    return maxUnit;
  }
  return `${minUnit} - ${maxUnit}`;
}

export function splitLocation(location) {
  if (!location) {
    return {};
  }

  if (location.includes("-") && location.includes("Room")) {
    let locations = location.split("-");
    let roomNumber = locations[1].split(":");
    return {
      Building: locations[0],
      Room: roomNumber[1],
    };
  }
  return {
    Building: location,
  };
}

export function changeStatus(section: Section): string {
  if (section.status === "WaitList") {
    return `Waitlist (${section.waitlistTotal})`;
  } else {
    return section.status;
  }
}

export function styleStatus(_status): React.CSSProperties["color"] {
  // if (status === "Open") {
  // } else if (status === "Closed") {
  // } else {
  // }

  return "unset";
}

export function parseDate(date): Date {
  return new Date(date);
  /*
  const datePattern = /^(\d{4})-(\d{2})-(\d{2})\s(\d{1,2}):(\d{2}):(\d{2})$/;
  const [, year, month, day, rawHour, min, sec] = datePattern.exec(date);
  return new Date(
    `${year}-${month}-${day}T${("0" + rawHour).slice(-2)}:${min}:${sec}`
  );
   */
}

export function addMinutes(date, minutes): Date {
  return new Date(date.getTime() + minutes * 60000);
}

export function isEqualTime(timeA, timeB): boolean {
  return (
    timeA.getHours() === timeB.getHours() &&
    timeA.getMinutes() === timeB.getMinutes() &&
    timeA.getSeconds() === timeB.getSeconds()
  );
}

export function convertToLocaleTimeStr(parsedDate): string {
  return parsedDate.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function generateScheduleTime(meetings): string {
  //parse meeting time and sort based on day
  const parsedMeetings = meetings
    .map((meeting) => {
      const parsedDate = parseDate(meeting.beginDate);
      const endTime = addMinutes(parsedDate, meeting.minutesDuration);
      return {
        startTime: parsedDate,
        startTimeStr: convertToLocaleTimeStr(parsedDate),
        day: parsedDate.getDay(),
        endTime: endTime,
        endTimeStr: convertToLocaleTimeStr(endTime),
      };
    })
    .sort((a, b) => a.day - b.day);

  if (meetings.length === 1) {
    //meeting only once per week
    const day = daysToStr[parsedMeetings[0].day].toUpperCase();
    return `${day} ${parsedMeetings[0].startTimeStr}-${parsedMeetings[0].endTimeStr}`;
  } else if (meetings.length === 2) {
    if (isEqualTime(parsedMeetings[0].startTime, parsedMeetings[1].startTime)) {
      //2 meetings a week are identical
      const firstDay = daysToStr[parsedMeetings[0].day].toUpperCase();
      const secondDay = daysToStr[parsedMeetings[1].day].toUpperCase();
      return `${firstDay},${secondDay} ${parsedMeetings[0].startTimeStr}-${parsedMeetings[0].endTimeStr}`;
    } else {
      //handle if 2 meetings a week aren't identical
      let res = "";
      parsedMeetings.forEach((parsedMeeting, i) => {
        const day = daysToStr[parsedMeeting.day].toUpperCase();
        res += `${day} ${parsedMeeting.startTimeStr}-${parsedMeeting.endTimeStr}`;
        if (i !== parsedMeetings.length - 1) res += ". ";
      });
      return res;
    }
  }
  //TODO: Handle if there are more than 2 different meeting time
  return "";
}
