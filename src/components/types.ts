import { z } from "zod";
import { QueryNumberSchema } from "./useQueryParam";

export const IdSchema = z.string();

export const SubjectSchema = z.object({
  code: z.string(),
  name: z.string(),
});

export const SchoolSchema = z.object({
  name: z.string(),
  subjects: z.array(SubjectSchema),
});

export type Semester = z.infer<typeof SemesterSchema>;
export const SemesterSchema = z.union([
  z.literal("sp"),
  z.literal("fa"),
  z.literal("ja"),
  z.literal("su"),
]);

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

export const StringDateSchema = z.preprocess(
  (obj) => new Date(obj as any),
  z.date()
);

export type Meeting = z.infer<typeof MeetingSchema>;
export const MeetingSchema = z.object({
  beginDate: StringDateSchema,
  beginDateLocal: z.string(),
  endDate: StringDateSchema,
  endDateLocal: z.string(),
  minutesDuration: z.number(),
});

export type Recitation = z.infer<typeof RecitationSchema>;
export const RecitationSchema = z.object({
  name: z.string().nullish(),
  registrationNumber: z.number(),
  campus: z.string(),
  code: z.string(),
  notes: z.string(),
  type: z.string(),
  instructors: z.array(z.string()),
  location: z.string().nullish(),
  minUnits: z.number(),
  maxUnits: z.number(),
  status: z.string(),
  meetings: z.array(MeetingSchema),
  waitlistTotal: z.number().nullish(),
});

export type Section = z.infer<typeof SectionSchema>;
export const SectionSchema = RecitationSchema.extend({
  recitations: z.array(RecitationSchema).nullish(),
});

export type Course = z.infer<typeof CourseSchema>;
export const CourseSchema = z.object({
  deptCourseId: z.string(),
  subjectCode: z.string(),
  name: z.string(),
  description: z.string(),
  sections: z.array(SectionSchema),
});
