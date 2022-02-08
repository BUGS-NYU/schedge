import React, { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/router";

import styled, { keyframes } from "styled-components";

export default function SchoolPage() {
  const router = useRouter();
  const { school, year, semester } = router.query;
  const { schoolName } = school ?? { schoolName: school };
  const [loading, setLoading] = useState(true);
  const [schoolData, setSchoolData] = useState({});
  const [schools, setSchools] = useState({});

  useEffect(() => {
    (async () => {
      try {
        const response = await fetch("https://schedge.a1liu.com/subjects");
        if (!response.ok) {
          // handle invalid search parameters
          return;
        }

        const data = await response.json();
        setSchoolData(() => data[school]);
        const sortedSchools = Object.keys(data[school]).sort();
        console.log(sortedSchools);
        setSchools(sortedSchools);
        setLoading(() => false);
      } catch (error) {
        console.error(error);
      }
    })();
  }, [school, setSchoolData]);

  return (
    <PageContainer>
      <DepartmentHeader>
        <div id="departmentTitle">{schoolName}</div>
      </DepartmentHeader>
      {loading && <span>Loading...</span>}
      {!loading && (
        <Departments>
          {schools.map((subjectid, i) => {
            const subject = schoolData[subjectid];
            const subjectName = subject === undefined ? "" : subject.name;
            return (
              <Link
                href={{
                  pathname: "/subject",
                  query: `&school=${school}&subject=${subjectid}&year=${year}&semester=${semester}`,
                }}
                key={i}
                style={{ textDecoration: "none", color: "inherit" }}
              >
                <Department>
                  <span className="departmentCode">{subjectid}</span>
                  <span className="departmentName">
                    &nbsp;
                    {subjectName}
                  </span>
                </Department>
              </Link>
            );
          })}
        </Departments>
      )}
    </PageContainer>
  );
}

const deptFadeIn = keyframes`
  from {
    opacity: 0;
    padding-top: 6rem;
  }

  to {
    opacity: 1;
    padding-top: 4rem;
  }
`;

const PageContainer = styled.div`
  width: 100vw;
  min-height: 100vh;
`;

const DepartmentHeader = styled.div`
  width: 100vw;
  padding: 2vmin 2vmin 0vmin 4vmin;
  font-size: 2rem;
`;

const Departments = styled.div`
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, minmax(22rem, 1fr));
  grid-gap: 0.5rem;
  padding: 0 2rem;
  animation: ${deptFadeIn} 0.8s ease forwards;
  @media (max-width: 1000px) {
    grid-template-columns: 1fr;
  }
`;

const Department = styled.div`
  padding: 0.3rem 0;

  & > .departmentCode {
    color: var(--grey600);
    font-family: var(--condensedFont);
    font-weight: 700;
  }

  &:hover {
  }
`;
