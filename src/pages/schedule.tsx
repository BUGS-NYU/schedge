import React from "react";
import Link from "next/link";
import WishlistCourse from "components/WishlistCourse";
import styles from "./schedule.module.css";
import Calendar from "components/Calendar";
import create from "zustand";

interface ScheduleState {
  schedule: Record<number, true>;
  wishlist: Record<number, true>;

  addToWishlist: (courseNum: number) => void;
  removeFromWishlist: (courseNum: number) => void;
  scheduleFromWishlist: (courseNum: number) => void;
  removeFromSchedule: (courseNum: number) => void;
  clearSchedule: () => void;
}

export const useSchedule = create<ScheduleState>((set, get) => {
  const scheduleFromWishlist = (regNum: number) => {
    const { schedule, wishlist } = get();
    const wishlistEntry = wishlist[regNum];
    if (!wishlistEntry) return;

    set({ schedule: { ...schedule, [regNum]: wishlistEntry } });
  };

  const removeFromSchedule = (regNum: number) => {
    const { schedule } = get();

    const newSchedule = { ...schedule };
    delete newSchedule[regNum];

    set({
      schedule: newSchedule,
    });
  };

  const addToWishlist = (regNum: number) => {
    const { wishlist } = get();
    set({ wishlist: { ...wishlist, [regNum]: true } });
  };

  const removeFromWishlist = (regNum: number) => {
    const { wishlist, schedule } = get();
    const newWishlist = { ...wishlist };
    delete newWishlist[regNum];
    const newSchedule = { ...schedule };
    delete newSchedule[regNum];

    set({ wishlist: newWishlist, schedule: newSchedule });
  };

  const clearSchedule = () => {
    set({ schedule: [], wishlist: [] });
  };

  return {
    schedule: [],
    wishlist: [],

    addToWishlist,
    removeFromWishlist,
    scheduleFromWishlist,
    removeFromSchedule,
    clearSchedule,
  } as ScheduleState;
});

function SchedulePage() {
  const {
    schedule,
    wishlist,
    scheduleFromWishlist,
    removeFromWishlist,
    clearSchedule,
  } = useSchedule();

  return (
    <div className={styles.container}>
      <Calendar registrationNumbers={Object.keys(schedule)} />

      <div
        style={{
          marginTop: "2rem",
        }}
      >
        <div className={styles.header}>
          <h2 className={styles.wishlist}>{`Wishlist (${
            Object.keys(wishlist).length
          })`}</h2>
        </div>

        <div className={styles.wishlistCoursesList}>
          {Object.keys(wishlist).length === 0 ? (
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
            Object.keys(wishlist).map((course, i) => {
              return null;
              // return (
              //   <WishlistCourse
              //     key={i}
              //     course={course}
              //     removeCourse={removeFromWishlist}
              //     scheduleCourse={scheduleFromWishlist}
              //   />
              // );
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
  );
}

export default SchedulePage;
