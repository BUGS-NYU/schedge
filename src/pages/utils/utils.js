import { missingPrograms, dayToStr } from "./constants";

export function convertUnits(minUnit, maxUnit) {
  if (minUnit === 0) {
    return maxUnit;
  }
  return `${minUnit} - ${maxUnit}`;
}

export function splitLocation(location) {
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

export function changeStatus(section) {
  if (section.status === "WaitList") {
    return `Waitlist (${section.waitlistTotal})`;
  } else {
    return section.status;
  }
}

export function styleStatus(status) {
  if (status === "Open") {
  } else if (status === "Closed") {
  } else {
  }
}

export function parseDate(date) {
  const datePattern = /^(\d{4})-(\d{2})-(\d{2})\s(\d{1,2}):(\d{2}):(\d{2})$/;
  const [, year, month, day, rawHour, min, sec] = datePattern.exec(date);
  return new Date(
    `${year}-${month}-${day}T${("0" + rawHour).slice(-2)}:${min}:${sec}`
  );
}

export function addMinutes(date, minutes) {
  return new Date(date.getTime() + minutes * 60000);
}

export function isEqualTime(timeA, timeB) {
  return (
    timeA.getHours() === timeB.getHours() &&
    timeA.getMinutes() === timeB.getMinutes() &&
    timeA.getSeconds() === timeB.getSeconds()
  );
}

export function convertToLocaleTimeStr(parsedDate) {
  return parsedDate.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function generateScheduleTime(meetings) {
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
    const day = dayToStr[parsedMeetings[0].day].toUpperCase();
    return `${day} ${parsedMeetings[0].startTimeStr}-${parsedMeetings[0].endTimeStr}`;
  } else if (meetings.length === 2) {
    if (isEqualTime(parsedMeetings[0].startTime, parsedMeetings[1].startTime)) {
      //2 meetings a week are identical
      const firstDay = dayToStr[parsedMeetings[0].day].toUpperCase();
      const secondDay = dayToStr[parsedMeetings[1].day].toUpperCase();
      return `${firstDay},${secondDay} ${parsedMeetings[0].startTimeStr}-${parsedMeetings[0].endTimeStr}`;
    } else {
      //handle if 2 meetings a week aren't identical
      let res = "";
      parsedMeetings.forEach((parsedMeeting, i) => {
        const day = dayToStr[parsedMeeting.day].toUpperCase();
        res += `${day} ${parsedMeeting.startTimeStr}-${parsedMeeting.endTimeStr}`;
        if (i !== parsedMeetings.length - 1) res += ". ";
      });
      return res;
    }
  }
  //TODO: Handle if there are more than 2 different meeting time
  return "";
}

export function findSchool(school) {
  return missingPrograms[school] ?? "";
}
