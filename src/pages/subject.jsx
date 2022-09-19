import React from "react";
import { usePageState } from "components/state";
import styles from "./subject.module.css";
import Link from "next/link";
import { useRouter } from "next/router";
import { useQuery } from "react-query";

export default function SubjectPage() {
  const router = useRouter();
  const { year, semester } = usePageState();
  const { school, subject } = router.query;

  const courseList = useQuery(
    ["courses", year, semester, school, subject],
    async () => {
      if (!year || !semester || !school || !subject) {
        return null;
      }

      const url = `https://schedge.a1liu.com/${year}/${semester}/${school}/${subject}`;
      const response = await fetch(url);
      const data = await response.json();

      const sortedData = data.sort((a, b) => a.deptCourseId - b.deptCourseId);
      return sortedData;
    }
  );

  const departmentList = useQuery("subjects", async () => {
    const response = await fetch("https://schedge.a1liu.com/subjects");
    return response.json();
  });

  const schoolList = useQuery("schools", async () => {
    const response = await fetch("https://schedge.a1liu.com/schools");
    return response.json();
  });

  return (
    <div className={styles.pageContainer}>
      <div className={styles.headerBackground}></div>

      <div>
        <div className={styles.departmentHeader}>
          <Link
            href={{
              pathname: "/school",
              query: { school, year, semester },
            }}
          >
            <a className={styles.schoolName}>
              {schoolList.data?.[school]?.name ?? school}
            </a>
          </Link>

          <div className={styles.departmentName}>
            {departmentList.data?.[school]?.[subject]?.name}
          </div>
        </div>

        <div className={styles.courseContainer}>
          {courseList.data?.map((course, i) => (
            <Link
              href={{
                pathname: "/course",
                query: {
                  year,
                  semester,
                  courseid: course.deptCourseId,
                  school: course.subjectCode.school,
                  subject: course.subjectCode.code,
                },
              }}
              key={i}
            >
              <a className={styles.course}>
                <h4>
                  {course.subjectCode.code}-{course.subjectCode.school}{" "}
                  {course.deptCourseId}
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
