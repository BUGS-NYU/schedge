import create, { StateCreator } from "zustand";
import { Section, Term } from "./types";
import { persist } from "zustand/middleware";

export interface AugmentedSection extends Section {
  name: string;
  sectionName?: string | null;
  deptCourseId: string;
  subjectCode: string;
}

interface PageState {
  term: Term;
  schedule: Record<number, AugmentedSection>;
  wishlist: Record<number, AugmentedSection>;

  cb: {
    updateTerm: (term: Term) => void;
    addToWishlist: (section: AugmentedSection) => void;
    removeFromWishlist: (regNum: number) => void;
    scheduleFromWishlist: (regNum: number) => void;
    removeFromSchedule: (regNum: number) => void;
    clearSchedule: () => void;
  };
}

const store: StateCreator<PageState, any> = (set, get) => {
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

    set({ schedule: newSchedule });
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

  return {
    term: {
      year: 2023,
      sem: "sp",
      code: "sp2023",
    },

    schedule: {},
    wishlist: {},

    cb: {
      updateTerm: (term) => set({ term }),
      clearSchedule: () => set({ schedule: {}, wishlist: {} }),
      addToWishlist,
      removeFromWishlist,
      scheduleFromWishlist,
      removeFromSchedule,
    },
  };
};

export const usePageState = create(
  persist(store, {
    name: "page-globals-storage", // name of item in the storage (must be unique)
    partialize: (state) => {
      const { cb, ...partial } = state;
      return partial;
    },
  })
);
