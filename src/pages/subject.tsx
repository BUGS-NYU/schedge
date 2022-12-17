import React from "react";
import { usePageState } from "components/state";
import styles from "./subject.module.css";
import Link from "next/link";
import { useQuery } from "react-query";
import { useQueryParam } from "../hooks/useQueryParam";
import { useSchools } from "./index";
import { z } from "zod";
import axios from "axios";
import { MainLayout } from "../components/Layout";
import {
  Course,
  CourseSchema,
  NumberStringSchema,
  Term,
} from "../components/types";

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

  const [schoolIndex] = useQueryParam("schoolIndex", NumberStringSchema);
  const [subjectCode] = useQueryParam("subject", SubjectSchema);
  const { data: schools } = useSchools(term);
  const school = schools?.schools?.[schoolIndex];
  const subject = school?.subjects?.find(
    (subject) => subject.code === subjectCode
  );

  const { data: courseList } = useCourses(term, subjectCode);

  return (
    <MainLayout>
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
    </MainLayout>
  );
}
