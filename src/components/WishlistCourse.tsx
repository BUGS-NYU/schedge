import React from "react";
import styles from "./wishlist-course.module.css";
import { generateScheduleTime } from "components/util";
import { Section, useSchedule } from "../pages/schedule";

interface Props {
  section: Section;
}

export const WishlistCourse: React.VFC<Props> = ({ section }) => {
  const {
    schedule,
    removeFromWishlist,
    scheduleFromWishlist,
    removeFromSchedule,
  } = useSchedule();

  return (
    <div className={styles.wishlistCourseContainer}>
      <div style={{ padding: "1rem" }}>
        <div>{section.name}</div>
        <div>
          Section: <span>{section.code}</span>
        </div>
        <div>
          Registration No: <span>{section.registrationNumber}</span>
        </div>
        <div className={styles.wishlistUtilBox}>
          <div className={styles.customFormControlLabel}>Schedule</div>

          <input
            type="checkbox"
            className={styles.CustomCheckbox}
            onChange={(e) =>
              schedule[section.registrationNumber]
                ? removeFromSchedule(section.registrationNumber)
                : scheduleFromWishlist(section.registrationNumber)
            }
          />

          <button
            className={styles.removeButton}
            onClick={() => removeFromWishlist(section.registrationNumber)}
            tabIndex={0}
          >
            Remove
          </button>
        </div>
      </div>
    </div>
  );
};

export default WishlistCourse;
