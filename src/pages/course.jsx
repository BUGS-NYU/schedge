import React from "react";
import Link from "next/link";
import Section from "components/Section";
import styles from "./course.module.css";
import { useQuery } from "react-query";
import { parseDate } from "components/util";
import { useRouter } from "next/router";

function CoursePage() {
  const router = useRouter();
  const { school, subject, courseid, year, semester } = router.query;

  const coursesKey = ["course", year, semester, school, subject, courseid];
  const { isLoading, data: courseData } = useQuery(coursesKey, async () => {
    if (!year || !semester || !school || !subject) {
      return null;
    }

    const url = `https://schedge.a1liu.com/${year}/${semester}/${school}/${subject}?full=true`;
    const response = await fetch(url);
    const data = await response.json();

    return data.find((course) => course.deptCourseId === courseid);
  });

  const header = (
    <div className={styles.colorHeader}>
      <div className={styles.courseHeader}>
        <Link
          href={{
            pathname: "/subject",
            query: { school, subject, year, semester },
          }}
        >
          <a>
            <img src="./img/go-back.svg" alt="Go back" id={styles.backButton} />
          </a>
        </Link>

        <div>
          <div id={styles.titleDepartment}>
            {subject}-{school}
          </div>
          <div id={styles.titleName}>{courseData?.name ?? "Loading..."}</div>
        </div>
      </div>
    </div>
  );

  if (isLoading) {
    return <div>{header}</div>;
  }

  const pullNotesToTop = courseData?.sections?.every(
    (section) => section.notes === courseData.sections[0].notes
  );

  return (
    <div>
      {header}
      <div className={styles.sectionsDescription}>
        {courseData?.description}

        {pullNotesToTop && (
          <>
            {/* Handle course description here if all sections have the same one */}
            <br />
            <br />
            {courseData.sections[0].notes}
          </>
        )}
      </div>

      {courseData?.sections?.length > 1 && (
        <div className={styles.sectionsHeader}>Sections</div>
      )}

      <div>
        {courseData?.sections?.map((section, i) => {
          const sortedSectionMeetings = (section.meetings ?? []).sort(
            (a, b) =>
              parseDate(a.beginDate).getDay() - parseDate(b.beginDate).getDay()
          );

          return (
            <Section
              key={i}
              section={section}
              sortedSectionMeetings={sortedSectionMeetings}
              courseData={courseData}
              year={year}
              semester={semester}
              lastSection={i === courseData.sections.length - 1}
            />
          );
        })}
      </div>
    </div>
  );
}

const _mapStateToProps = (state, props) => ({
  wishlist: state.wishlist[props.semester + props.year] || [],
  scheduled: state.scheduled[props.semester + props.year] || [],
});

//export default connect(mapStateToProps, actions)(CoursePage);
export default CoursePage;
