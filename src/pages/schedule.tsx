import React from "react";
import Link from "next/link";
import { Calendar } from "components/Calendar";
import { MainLayout } from "components/Layout";
import { AugmentedSection, usePageState } from "components/state";
import styles from "components/css/wishlist-course.module.css";

interface WishlistProps {
  section: AugmentedSection;
}

export const WishlistCourse: React.VFC<WishlistProps> = ({ section }) => {
  const { schedule, cb } = usePageState();

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
                ? cb.removeFromSchedule(section.registrationNumber)
                : cb.scheduleFromWishlist(section.registrationNumber)
            }
          />

          <button
            className={styles.removeButton}
            onClick={() => cb.removeFromWishlist(section.registrationNumber)}
            tabIndex={0}
          >
            Remove
          </button>
        </div>
      </div>
    </div>
  );
};

function SchedulePage() {
  const { schedule, wishlist, cb } = usePageState();
  const wishlistLength = Object.keys(wishlist).length;

  return (
    <MainLayout>
      <div className={styles.container}>
        <Calendar registrationNumbers={Object.keys(schedule)} />

        <div style={{ marginTop: "2rem" }}>
          <div className={styles.header}>
            <h2
              className={styles.wishlist}
            >{`Wishlist (${wishlistLength})`}</h2>
          </div>

          <div className={styles.wishlistCoursesList}>
            {wishlistLength === 0 ? (
              <div className={styles.emptyWishlistContainer}>
                Your wishlist appears empty!{" "}
                <Link
                  href={{
                    pathname: "/",
                  }}
                >
                  <a style={{ textDecoration: "none", color: "purpleLight" }}>
                    Search
                  </a>
                </Link>{" "}
                for courses to add to your wishlist
              </div>
            ) : (
              Object.values(wishlist).map((section, i) => {
                return <WishlistCourse key={i} section={section} />;
              })
            )}
          </div>

          <button
            onClick={cb.clearSchedule}
            className={styles.clearScheduleButton}
            tabIndex={0}
          >
            Clear Schedule
          </button>
        </div>
      </div>
    </MainLayout>
  );
}

export default SchedulePage;
