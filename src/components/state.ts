import create from "zustand";
import { z } from "zod";

export const SemesterSchema = z.union([
  z.literal("sp"),
  z.literal("fa"),
  z.literal("ja"),
  z.literal("su"),
]);

export type Semester = z.infer<typeof SemesterSchema>;

interface PageState {
  year: number;
  semester: Semester;
  update: (partial: Partial<{ semester: Semester; year: number }>) => void;
}

export const usePageState = create<PageState>((set) => {
  return {
    update: (part) => {
      set({ ...part });
    },

    year: 2021,
    semester: "sp",
  };
});
