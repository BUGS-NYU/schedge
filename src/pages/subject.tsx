import React from "react";
import { Term, usePageState } from "components/state";
import styles from "./subject.module.css";
import Link from "next/link";
import { useQuery } from "react-query";
import { QueryNumberSchema, useQueryParam } from "../components/useQueryParam";
import { useSchools } from "./index";
import { z } from "zod";
import axios from "axios";

export const StringDateSchema = z.preprocess(
  (obj) => new Date(obj as any),
  z.date()
);

export type Meeting = z.infer<typeof MeetingSchema>;
export const MeetingSchema = z.object({
  beginDate: StringDateSchema,
});

export const RecitationSchema = z.object({
  registrationNumber: z.number(),
  code: z.string(),
  notes: z.string(),
  type: z.string(),
  instructors: z.array(z.string()),
  location: z.string(),
  minUnits: z.number(),
  maxUnits: z.number(),
  status: z.string(),
  meetings: z.array(MeetingSchema),
  waitlistTotal: z.number().optional(),
});

export type Section = z.infer<typeof SectionSchema>;
export const SectionSchema = RecitationSchema.extend({
  recitations: z.array(RecitationSchema).optional(),
});

export type Course = z.infer<typeof CourseSchema>;
export const CourseSchema = z.object({
  deptCourseId: z.string(),
  subjectCode: z.string(),
  name: z.string(),
  description: z.string(),
  sections: z.array(SectionSchema),
});

export const useCourses = (term: Term, subject: string) => {
  return useQuery(
    ["courses", term.code, subject],
    async (): Promise<Course[]> => {
      const resp = await axios.get(`/api/courses/${term.code}/${subject}`);
      const data: Course[] = z.array(CourseSchema).parse(resp.data);

      const sortedData = data.sort(
        (a, b) =>
          Number.parseInt(a.deptCourseId) - Number.parseInt(b.deptCourseId)
      );
      return sortedData;
    }
  );
};

export const SubjectSchema = z.string();
export default function SubjectPage() {
  const { term } = usePageState();

  const [schoolIndex] = useQueryParam("schoolIndex", QueryNumberSchema);
  const [subjectCode] = useQueryParam("subject", SubjectSchema);
  const { data: schools } = useSchools(term);
  const school = schools?.schools?.[schoolIndex];
  const subject = school?.subjects?.find(
    (subject) => subject.code === subjectCode
  );

  const { data: courseList } = useCourses(term, subjectCode);

  return (
    <div className={styles.pageContainer}>
      <div className={styles.headerBackground}></div>

      <div>
        <div className={styles.departmentHeader}>
          <Link
            href={{
              pathname: "/school",
              query: { schoolIndex },
            }}
          >
            <a className={styles.schoolName}>{school?.name}</a>
          </Link>

          <div className={styles.departmentName}>{subject?.name}</div>
        </div>

        <div className={styles.courseContainer}>
          {courseList?.map((course, i) => (
            <Link
              href={{
                pathname: "/course",
                query: {
                  courseid: course.deptCourseId,
                  schoolIndex,
                  subject: subjectCode,
                },
              }}
              key={i}
            >
              <a className={styles.course}>
                <h4>
                  {course.subjectCode} {course.deptCourseId}
                </h4>
                <h3>{course.name}</h3>
                <p>{course.sections.length} Sections</p>
              </a>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
