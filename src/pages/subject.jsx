import React, { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/router";

import styled from "styled-components";

import { findSchool } from "./utils/utils";

export default function SubjectPage({ location }) {
  const router = useRouter();
  const { school, subject, year, semester } = router.query;

  const [courseList, setCourseList] = useState({ loading: true, data: [] });
  const [departmentList, setDepartmentList] = useState({
    loading: true,
    data: {},
  });
  const [schoolList, setSchoolList] = useState({ loading: true, data: {} });

  useEffect(() => {
    (async () => {
      try {
        const response = await fetch(
          `https://schedge.a1liu.com/${year}/${semester}/${school}/${subject}`
        );
        if (!response.ok) {
          // handle invalid search parameters
          return;
        }
        const data = await response.json();
        const sortedData = data.sort((a, b) => a.deptCourseId - b.deptCourseId);
        setCourseList(() => ({ loading: false, data: sortedData }));
      } catch (error) {
        console.error(error);
      }
    })();
    (async () => {
      try {
        const response = await fetch("https://schedge.a1liu.com/subjects");
        if (!response.ok) {
          // handle invalid search parameters
          return;
        }
        const data = await response.json();
        setDepartmentList(() => ({ loading: false, data }));
      } catch (error) {
        console.error(error);
      }
    })();

    (async () => {
      try {
        const response = await fetch("https://schedge.a1liu.com/schools");
        if (!response.ok) {
          // handle invalid search parameters
          return;
        }
        const data = await response.json();
        setSchoolList(() => ({ loading: false, data }));
      } catch (error) {
        console.error(error);
      }
    })();
  }, [year, semester, school, subject]);

  return (
    <PageContainer>
      <HeaderBackground></HeaderBackground>
      {courseList.loading && schoolList.loading && departmentList.loading && (
        <span></span>
      )}
      {!(
        courseList.loading ||
        schoolList.loading ||
        departmentList.loading
      ) && (
        <div>
          <DepartmentHeader>
            <Link
              href={{
                pathname: "/school",
                query: `school=${school}&year=${year}&semester=${semester}`,
                state: {
                  schoolName: schoolList.data[school]
                    ? schoolList.data[school].name
                    : findSchool(school),
                },
              }}
              style={{ textDecoration: "none" }}
            >
              <SchoolName>
                {schoolList.data[school]
                  ? schoolList.data[school].name
                  : school}
              </SchoolName>
            </Link>

            <DepartmentName>
              {departmentList.data[school][subject].name
                ? departmentList.data[school][subject].name
                : ""}
            </DepartmentName>
          </DepartmentHeader>
          <CourseContainer>
            {courseList.data.map((course, i) => (
              <Link
                href={{
                  pathname: "/course",
                  query: `&school=${course.subjectCode.school}&subject=${course.subjectCode.code}&courseid=${course.deptCourseId}&year=${year}&semester=${semester}`,
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
      )}
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
