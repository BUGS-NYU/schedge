import React from "react";
import styles from "./css/wishlist-course.module.css";
import {
  AugmentedSection,
  useSchedule,
  useScheduleCb,
} from "../pages/schedule";

interface Props {
  section: AugmentedSection;
}

export const WishlistCourse: React.VFC<Props> = ({ section }) => {
  const { schedule } = useSchedule();
  const { removeFromWishlist, scheduleFromWishlist, removeFromSchedule } =
    useScheduleCb();

  return (
    <div className={styles.wishlistCourseContainer}>
      <div style={{ padding: "1rem" }}>
        <div>
          {section.subjectCode} {section.deptCourseId}: {section.name}
        </div>
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
            checked={!!schedule[section.registrationNumber]}
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
