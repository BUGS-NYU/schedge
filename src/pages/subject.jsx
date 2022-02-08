import React from "react";
import Link from "next/link";
import { useRouter } from "next/router";
import styled from "styled-components";
import { useAsync } from "components/hooks";
import { findSchool } from "components/util";

export default function SubjectPage({ location }) {
  const router = useRouter();
  const { school, subject, year, semester } = router.query;

  const courseList = useAsync(async () => {
    if (!year || !semester || !school || !subject) {
      return null;
    }

    const url = `https://schedge.a1liu.com/${year}/${semester}/${school}/${subject}`;
    const response = await fetch(url);
    const data = await response.json();

    const sortedData = data.sort((a, b) => a.deptCourseId - b.deptCourseId);
    return sortedData;
  }, [year, semester, school, subject]);

  const departmentList = useAsync(async () => {
    const response = await fetch("https://schedge.a1liu.com/subjects");
    return response.json();
  }, []);

  const schoolList = useAsync(async () => {
    const response = await fetch("https://schedge.a1liu.com/schools");
    return response.json();
  }, []);

  React.useEffect(() => {
    (async () => {
      try {
      } catch (error) {
        console.error(error);
      }
    })();
  }, [year, semester, school, subject]);

  return (
    <PageContainer>
      <HeaderBackground></HeaderBackground>

      <div>
        <DepartmentHeader>
          <Link
            href={{
              pathname: "/school",
              query: `school=${school}&year=${year}&semester=${semester}`,
              state: {
                schoolName:
                  schoolList.data?.[school]?.name ?? findSchool(school),
              },
            }}
            style={{ textDecoration: "none" }}
          >
            <SchoolName>{schoolList.data?.[school]?.name ?? school}</SchoolName>
          </Link>

          <DepartmentName>
            {departmentList.data?.[school]?.[subject]?.name}
          </DepartmentName>
        </DepartmentHeader>

        <CourseContainer>
          {courseList.data?.map((course, i) => (
            <Link
              href={{
                pathname: "/course",
                query: `school=${course.subjectCode.school}&subject=${course.subjectCode.code}&courseid=${course.deptCourseId}&year=${year}&semester=${semester}`,
              }}
              key={i}
              style={{ textDecoration: "none", color: "inherit" }}
            >
              <Course>
                <h4>
                  {course.subjectCode.code}-{course.subjectCode.school}{" "}
                  {course.deptCourseId}
                </h4>
                <h3>{course.name}</h3>
                <p>{course.sections.length} Sections</p>
              </Course>
            </Link>
          ))}
        </CourseContainer>
      </div>
    </PageContainer>
  );
}

const PageContainer = styled.div`
  width: 100vw;
  min-height: 100vh;
`;

const HeaderBackground = styled.div`
  width: 100vw;
  height: 2rem;
`;

const DepartmentHeader = styled.div`
  margin: 2vmin 2vmin 4vmin 4vmax;
`;

const SchoolName = styled.div`
  font-size: 1.4rem;
`;

const DepartmentName = styled.div`
  font-weight: bold;
  font-size: 2.6rem;
  margin-top: -0.1rem;
`;

const CourseContainer = styled.div`
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  flex-wrap: wrap;
  width: 85vw;
  margin: 0 auto;
`;

const Course = styled.div`
  padding: 0.75vmax 3vmin;
  word-break: break-word;
  width: 60vmin;
  min-height: 5vmax;
  margin: 1vmax;
  border-radius: 0.3rem;
  @media (max-width: 1000px) {
    width: 38vmin;
  }

  &:hover {
  }

  & > h4 {
  }
  & > p {
  }
`;
