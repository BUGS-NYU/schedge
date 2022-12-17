import React from "react";
import Link from "next/link";
import WishlistCourse from "components/WishlistCourse";
import styles from "./schedule.module.css";
import { Calendar } from "components/Calendar";
import create from "zustand";
import { Section } from "./subject";
import { MainLayout } from "components/Layout";
import { Term } from "components/state";

export interface AugmentedSection extends Section {
  name: string;
  sectionName?: string | null;
  deptCourseId: string;
  subjectCode: string;
}

interface Schedule {
  term: Term;
  schedule: Record<number, AugmentedSection>;
  wishlist: Record<number, AugmentedSection>;
}

interface ScheduleState {
  term: Term | undefined;
  schedule: Record<number, AugmentedSection>;
  wishlist: Record<number, AugmentedSection>;
  archive: Schedule[];

  cb: {
    addToWishlist: (section: AugmentedSection) => void;
    removeFromWishlist: (regNum: number) => void;
    scheduleFromWishlist: (regNum: number) => void;
    removeFromSchedule: (regNum: number) => void;
    clearSchedule: () => void;
  };
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

  const addToWishlist = (section: AugmentedSection) => {
    const regNum = section.registrationNumber;
    const { wishlist } = get();
    set({ wishlist: { ...wishlist, [regNum]: section } });
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
    set({ schedule: {}, wishlist: {} });
  };

  return {
    term: undefined,
    schedule: {},
    wishlist: {},
    archive: [],

    cb: {
      addToWishlist,
      removeFromWishlist,
      scheduleFromWishlist,
      removeFromSchedule,
      clearSchedule,
    },
  };
});

export function useScheduleCb() {
  return useSchedule().cb;
}

function SchedulePage() {
  const { schedule, wishlist } = useSchedule();
  const { clearSchedule } = useScheduleCb();
  const wishlistLength = Object.keys(wishlist).length;

  return (
    <MainLayout>
      <div className={styles.container}>
        <Calendar registrationNumbers={Object.keys(schedule)} />

        <div
          style={{
            marginTop: "2rem",
          }}
        >
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
