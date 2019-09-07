SELECT
    courses.crn, courses.c_num, courses.sub,
    course_meetings.days,course_meetings.times_start,
    course_meetings.times_end
  FROM
    courses INNER JOIN course_meetings
  ON
    courses.crn = course_meetings.reg_num
  WHERE
    NOT course_meetings.days = '' AND
    courses.sub = 'CORE-UA' AND
    courses.c_num < 300 AND
    LENGTH(course_meetings.days) <= 7;
