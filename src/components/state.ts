import create from "zustand";
import { z } from "zod";
import { QueryNumberSchema } from "./useQueryParam";

const SubjectSchema = z.object({
  code: z.string(),
  name: z.string(),
});

export const SchoolSchema = z.object({
  name: z.string(),
  subjects: z.array(SubjectSchema),
});

export const SemesterSchema = z.union([
  z.literal("sp"),
  z.literal("fa"),
  z.literal("ja"),
  z.literal("su"),
]);

export type Semester = z.infer<typeof SemesterSchema>;

export const parseTerm = (term: string): Term => {
  return {
    semester: SemesterSchema.parse(term.substring(0, 2)),
    year: QueryNumberSchema.parse(term.substring(2)),
    code: term,
  };
};

export const semName = (sem: Semester) => {
  switch (sem) {
    case "ja":
      return "January";
    case "sp":
      return "Spring";
    case "su":
      return "Summer";
    case "fa":
      return "Fall";
  }
};

export interface Term {
  year: number;
  semester: Semester;
  code: string;
}

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
      semester: "sp",
      code: "sp2023",
    },
  };
});
