import React from "react";
import fonts from "components/css/fonts.module.css";
import cx from "classnames";
import Link from "next/link";
import { SectionInfo } from "components/SectionInfo";
import styles from "./course.module.css";
import { usePageState } from "components/state";
import { useQueryParam } from "components/useQueryParam";
import { SubjectSchema, useCourses } from "./subject";
import GoBack from "components/img/go-back.svg";
import { ScheduleButton } from "components/Layout";
import { Course, IdSchema, NumberStringSchema } from "components/types";

function CoursePage() {
  const term = usePageState((s) => s.term);

  const [schoolIndex] = useQueryParam("schoolIndex", NumberStringSchema);
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
            <GoBack className={styles.svgButton} alt="Go back" />
          </a>
        </Link>

        <ScheduleButton className={styles.svgButton} />
      </div>

      <div className={styles.courseHeader}>
        <div className={fonts.body2}>{subjectCode}</div>
        <div className={fonts.heading1}>{course?.name ?? "Loading..."}</div>
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
        <p className={cx(fonts.body1, styles.courseNotes)}>
          {course?.description}
        </p>

        {/* Handle course description here if all sections have the same one */}
        {pullNotesToTop && (
          <p className={cx(fonts.body1, styles.courseNotes)}>
            {course.sections[0]?.notes}
          </p>
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
