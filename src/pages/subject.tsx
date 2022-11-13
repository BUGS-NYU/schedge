import React from "react";
import { usePageState } from "components/state";
import styles from "./subject.module.css";
import Link from "next/link";
import { useRouter } from "next/router";
import { useQuery } from "react-query";
import { QueryNumberSchema, useQueryParam } from "../components/useQueryParam";
import { useSchools } from "./index";
import { z } from "zod";
import axios from "axios";

const SubjectSchema = z.string();
export default function SubjectPage() {
  const router = useRouter();
  const { term } = usePageState();

  const [schoolIndex] = useQueryParam("schoolIndex", QueryNumberSchema);
  const [subjectCode] = useQueryParam("subject", SubjectSchema);
  const { data: schools } = useSchools(term);
  const school = schools?.schools?.[schoolIndex];
  const subject = school?.subjects?.find(subject => subject.code === subjectCode);

  const { data: courseList } = useQuery(
    ["courses", term.code, schoolIndex, subjectCode],
    async () => {
      const resp = await axios.get(`/api/courses/${term.code}/${subjectCode}`);
      const data = resp.data;

      const sortedData = data.sort((a, b) => a.deptCourseId - b.deptCourseId);
      return sortedData;
    },
  );

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
            <a className={styles.schoolName}>
              {school?.name}
            </a>
          </Link>

          <div className={styles.departmentName}>
            {subject?.name}
          </div>
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
                  {course.subject} {course.deptCourseId}
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
