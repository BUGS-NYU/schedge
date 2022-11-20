import React from "react";
import styles from "./wishlist-course.module.css";
import { generateScheduleTime } from "components/util";

function WishlistCourse({ course, checkboxes, removeCourse, scheduleCourse }) {
  return (
    <div className={styles.wishlistCourseContainer}>
      <div style={{ padding: "1rem" }}>
        <div>{course.name}</div>
        <div>
          Section: <span>{course.code}</span>
        </div>
        <div>
          Registration No: <span>{course.registrationNumber}</span>
        </div>
        <div
          style={{
            marginTop: "1rem",
          }}
        >
          <div>
            Type: <span>{course.type}</span>
          </div>
          <div>
            Instructors: <span>{course.instructors.join(", ")}</span>
          </div>
          <div>
            Meetings: <span>{generateScheduleTime(course.meetings)}</span>
          </div>
        </div>
        <div className={styles.wishlistUtilBox}>
          <div
            className={styles.customFormControlLabel}
            value="add"
            control={
              <button
                className={styles.CustomCheckbox}
                checked={
                  checkboxes[course.registrationNumber] === undefined
                    ? false
                    : checkboxes[course.registrationNumber]
                }
                onChange={(e) =>
                  scheduleCourse(e, course, course.registrationNumber)
                }
              />
            }
            label="Schedule"
            labelPlacement="start"
          />
          <div
            role="button"
            className="removeButton"
            onClick={() => removeCourse(course)}
            onKeyDown={() => removeCourse(course)}
            tabIndex={0}
          >
            Remove
          </div>
        </div>
      </div>
    </div>
  );
}

export default WishlistCourse;
