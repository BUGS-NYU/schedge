import React from "react";
import Link from "next/link";
import styled from "styled-components";
import Section from "components/Section";
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

  return (
    <div>
      {isLoading && (
        <>
          <span>Loading...</span>
          <ColorHeader>
            <CourseHeader>
              <Link href="/" style={{ textDecoration: "none" }}>
                <img src="./img/go-back.svg" alt="Go back" id="backButton" />
              </Link>
            </CourseHeader>
          </ColorHeader>
        </>
      )}

      {!isLoading && (
        <>
          <ColorHeader>
            <CourseHeader>
              <Link
                href={`/subject?school=${school}&subject=${subject}&year=${year}&semester=${semester}`}
                style={{ textDecoration: "none" }}
              >
                <img src="./img/go-back.svg" alt="Go back" id="backButton" />
              </Link>
              <div>
                <div id="titleDepartment">
                  {subject}-{school}
                </div>
                <div id="titleName">{courseData?.name}</div>
              </div>
            </CourseHeader>
          </ColorHeader>

          {/* Handle course description here if all sections have the same one */}
          <SectionsDescription>
            {courseData?.description}
            {courseData?.sections?.every(
              (section) => section.notes === courseData.sections[0].notes
            ) && (
              <>
                <br />
                <br />
                {courseData.sections[0].notes}
              </>
            )}
          </SectionsDescription>

          {courseData?.sections?.length > 1 && (
            <SectionsHeader>Sections</SectionsHeader>
          )}

          <div>
            {courseData?.sections?.map((section, i) => {
              const sortedSectionMeetings = section.meetings
                ? section.meetings.sort(
                    (a, b) =>
                      parseDate(a.beginDate).getDay() -
                      parseDate(b.beginDate).getDay()
                  )
                : [];

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
        </>
      )}
    </div>
  );
}

const ColorHeader = styled.div`
  width: 100vw;
  /* height: calc(14vmin + 8rem); */
  padding-top: 6rem;
  margin-top: -6rem;
  background: linear-gradient(
    167deg,
    var(--purpleMain) 21%,
    #712991 60%,
    rgba(135, 37, 144, 1) 82%
  );
  position: relative;
  display: flex;
  align-items: flex-end;
  @media (max-width: 1000px) {
    padding-top: 5rem;
  }
`;

const CourseHeader = styled.div`
  width: 90vw;
  margin-left: 5vw;
  background-color: var(--grey100);
  color: var(--grey800);
  padding: 3vmin 4vmin 10vmin 4%;
  border-top-left-radius: 0.8rem;
  border-top-right-radius: 0.8rem;
  box-shadow: 0 -5px 5px rgba(0, 0, 0, 0.15);
  margin-bottom: calc(-3vh - 5vmin);

  & #backButton {
    position: absolute;
    z-index: 2;
    top: 2.5vmin;
    left: 5vw;
    height: 2.5rem;
    opacity: 0.7;
    transition: 0.15s;
  }

  & #backButton:hover {
    opacity: 1;
  }

  & #titleDepartment {
    font-size: calc(1vmin + 0.7rem);
    margin: 0 0 -0.5vmin 0.3vmin;
    font-family: var(--grey200);
  }

  & #titleName {
    font-size: calc(2.2vmin + 1.4rem);
    font-weight: bold;
  }
`;

const SectionsDescription = styled.div`
  padding: 1.8vmin 2.8vmin;
  font-size: 1.2rem;
  line-height: 1.65rem;
  width: 73%;
  margin-left: 9%;
  color: var(--grey800);
  position: relative;
  @media (max-width: 1000px) {
    margin-top: calc(12vmin);
  }
`;

const SectionsHeader = styled.div`
  font-weight: bold;
  text-align: center;
  font-size: calc(1.2vmin + 1rem);
  padding: 2vmin;
  color: var(--grey800);
  margin-top: calc(2vmin + 1rem);
`;

const _mapStateToProps = (state, props) => ({
  wishlist: state.wishlist[props.semester + props.year] || [],
  scheduled: state.scheduled[props.semester + props.year] || [],
});

//export default connect(mapStateToProps, actions)(CoursePage);
export default CoursePage;
