import React from "react";
import Link from "next/link";
import WishlistCourse from "components/WishlistCourse";
import styles from "./schedule.module.css";
import { Calendar } from "components/Calendar";
import { MainLayout } from "components/Layout";
import { useSchedule, useScheduleCb } from "components/state";

function SchedulePage() {
  const { schedule, wishlist } = useSchedule();
  const { clearSchedule } = useScheduleCb();
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
            onClick={clearSchedule}
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
