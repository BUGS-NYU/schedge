import { z } from "zod";

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

export const NumberStringSchema = z.preprocess((obj): number | undefined => {
  if (typeof obj === "string") {
    const parsed = Number.parseInt(obj, 10);

    // Number.parseInt returns NaN when it fails to parse.
    // This *should* never happen, but designers have started editing
    // the URL parameters manually, so we need to check this value
    // somewhat carefully to avoid propagating a NaN.
    if (Number.isNaN(parsed)) {
      return undefined;
    }

    return parsed;
  }

  if (typeof obj === "number") {
    return obj;
  }

  return undefined;
}, z.number());

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
