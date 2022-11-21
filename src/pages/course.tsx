import React from "react";
import Link from "next/link";
import { SectionInfo } from "components/SectionInfo";
import styles from "./course.module.css";
import { usePageState } from "components/state";
import { QueryNumberSchema, useQueryParam } from "../components/useQueryParam";
import { Course, SubjectSchema, useCourses } from "./subject";
import { z } from "zod";
import EditCalendarSVG from "components/edit-calendar.svg";
import { ScheduleButton } from "../components/Layout";

const IdSchema = z.string();

function CoursePage() {
  const { term } = usePageState();

  const [schoolIndex] = useQueryParam("schoolIndex", QueryNumberSchema);
  const [subjectCode] = useQueryParam("subject", SubjectSchema);
  const [courseid] = useQueryParam("courseid", IdSchema);

  const { isLoading, data: courseData } = useCourses(term, subjectCode);

  const course: Course = courseData?.find(
    (course) => course.deptCourseId === courseid
  );

  const header = (
    <div className={styles.colorHeader}>
      <div className={styles.iconBar}>
        <Link
          href={{
            pathname: "/subject",
            query: { schoolIndex, subject: subjectCode },
          }}
        >
          <a className={styles.svgButton}>
            <img
              src="/img/go-back.svg"
              alt="Go back"
              className={styles.svgButton}
            />
          </a>
        </Link>

        <ScheduleButton className={styles.svgButton} />
      </div>

      <div className={styles.courseHeader}>
        <div id={styles.titleDepartment}>{subjectCode}</div>
        <div id={styles.titleName}>{course?.name ?? "Loading..."}</div>
      </div>
    </div>
  );

  if (isLoading || !course) {
    return <div>{header}</div>;
  }

  const pullNotesToTop = course?.sections?.every(
    (section) => section.notes === course.sections[0].notes
  );

  return (
    <>
      {header}

      <div className={styles.courseBody}>
        <div className={styles.sectionsDescription}>
          <p>{course?.description}</p>

          {/* Handle course description here if all sections have the same one */}
          {pullNotesToTop && <p>{course.sections[0]?.notes}</p>}
        </div>

        {!!course?.sections?.length && (
          <div className={styles.sectionsHeader}>Sections</div>
        )}

        {course?.sections?.map((section, i) => {
          return (
            <SectionInfo
              key={i}
              lastSection={i === course.sections.length - 1}
              ignoreNotes={pullNotesToTop}
              section={{
                ...section,
                sectionName: section.name,
                name: section.name ?? course.name,
                deptCourseId: course.deptCourseId,
                subjectCode: course.subjectCode,
              }}
            />
          );
        })}
      </div>
    </>
  );
}

export default CoursePage;
