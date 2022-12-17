import create from "zustand";
import { Term } from "./types";

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
