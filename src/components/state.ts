import create, { StateCreator } from "zustand";
import { Section, Term } from "./types";
import { persist } from "zustand/middleware";

interface PageState {
  term: Term;
  update: (partial: Partial<Term>) => void;
}

export const usePageState = create<PageState>((set) => {
  return {
    update: (term) => {
      set((prev) => ({
        term: {
          ...prev.term,
          ...term,
        },
      }));
    },

    term: {
      year: 2023,
      sem: "sp",
      code: "sp2023",
    },
  };
});

export interface AugmentedSection extends Section {
  name: string;
  sectionName?: string | null;
  deptCourseId: string;
  subjectCode: string;
}

interface ScheduleState {
  schedule: Record<number, AugmentedSection>;
  wishlist: Record<number, AugmentedSection>;

  cb: {
    addToWishlist: (section: AugmentedSection) => void;
    removeFromWishlist: (regNum: number) => void;
    scheduleFromWishlist: (regNum: number) => void;
    removeFromSchedule: (regNum: number) => void;
    clearSchedule: () => void;
  };
}

const store: StateCreator<ScheduleState, any, []> = (set, get) => {
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
    schedule: {},
    wishlist: {},

    cb: {
      addToWishlist,
      removeFromWishlist,
      scheduleFromWishlist,
      removeFromSchedule,
      clearSchedule,
    },
  };
};

export const useSchedule = create(
  persist(store, {
    name: "schedule-local-storage", // name of item in the storage (must be unique)
    partialize: (state) => {
      const { cb, ...partial } = state;
      return partial;
    },
  }),
);

export function useScheduleCb() {
  return useSchedule().cb;
}