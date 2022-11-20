import React from "react";
import Link from "next/link";
import { Section } from "components/Section";
import styles from "./course.module.css";
import { parseDate } from "components/util";
import { usePageState } from "components/state";
import { QueryNumberSchema, useQueryParam } from "../components/useQueryParam";
import { SubjectSchema, useCourses } from "./subject";
import { z } from "zod";

const IdSchema = z.string();

function CoursePage() {
  const { term } = usePageState();

  const [schoolIndex] = useQueryParam("schoolIndex", QueryNumberSchema);
  const [subjectCode] = useQueryParam("subject", SubjectSchema);
  const [courseid] = useQueryParam("courseid", IdSchema);

  const { isLoading, data: courseData } = useCourses(term, subjectCode);

  const course = courseData?.find((course) => course.deptCourseId === courseid);

  const header = (
    <div className={styles.colorHeader}>
      <div className={styles.courseHeader}>
        <Link
          href={{
            pathname: "/subject",
            query: { schoolIndex, subject: subjectCode },
          }}
        >
          <a>
            <img src="./img/go-back.svg" alt="Go back" id={styles.backButton} />
          </a>
        </Link>

        <div>
          <div id={styles.titleDepartment}>{subjectCode}</div>
          <div id={styles.titleName}>{course?.name ?? "Loading..."}</div>
        </div>
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
    <div>
      {header}
      <div className={styles.sectionsDescription}>
        {course?.description}

        {pullNotesToTop && (
          <>
            {/* Handle course description here if all sections have the same one */}
            <br />
            <br />
            {course.sections[0].notes}
          </>
        )}
      </div>

      {course?.sections?.length > 1 && (
        <div className={styles.sectionsHeader}>Sections</div>
      )}

      <div>
        {course?.sections?.map((section, i) => {
          const sortedSectionMeetings = (section.meetings ?? []).sort(
            (a, b) =>
              parseDate(a.beginDate).getDay() - parseDate(b.beginDate).getDay()
          );

          return (
            <Section
              key={i}
              section={section}
              sortedSectionMeetings={sortedSectionMeetings}
              courseData={course}
              year={term.year}
              semester={term.semester}
              lastSection={i === course.sections.length - 1}
            />
          );
        })}
      </div>
    </div>
  );
}

export default CoursePage;
