import React, { useState, useEffect } from "react";

import styled, { keyframes } from "styled-components";

import SearchBar from "./components/SearchBar";
import School from "./components/School";

export default function SearchPage({ year, semester }) {
  const [departments, setDepartments] = useState({ loading: true, data: {} });
  const [schools, setSchools] = useState({
    loading: true,
    data: {
      undergraduate: {},
      graduate: {},
      others: {},
    },
  });

  useEffect(() => {
    (async () => {
      try {
        const response = await fetch("https://schedge.a1liu.com/subjects");
        if (!response.ok) {
          // handle invalid search parameters
          return;
        }
        const data = await response.json();
        setDepartments(() => ({ loading: false, data }));
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
        const undergraduate = {},
          graduate = {},
          others = {};
        Object.keys(data).forEach((schoolCode) => {
          if (schoolCode.startsWith("G")) {
            graduate[schoolCode] = data[schoolCode];
          } else if (
            schoolCode.startsWith("U") ||
            data[schoolCode].name !== ""
          ) {
            undergraduate[schoolCode] = data[schoolCode];
          } else {
            others[schoolCode] = data[schoolCode];
          }
        });
        setSchools(() => ({
          loading: false,
          data: { undergraduate, graduate, others },
        }));
      } catch (error) {
        console.error(error);
      }
    })();
  }, []);

  return (
    <div id="pageContainer">
      <SearchContainer>
        <SearchBar year={year} semester={semester} />
      </SearchContainer>
      <SchoolsContainer>
        <div id="departmentTitle">Schools</div>
        {!schools.loading && !departments.loading && (
          <Schools>
            <div>
              <SchoolType>Undergraduate</SchoolType>
              {Object.keys(schools.data.undergraduate).map((schoolCode, i) => (
                <School
                  key={i}
                  schoolCode={schoolCode}
                  schoolName={schools.data.undergraduate[schoolCode].name}
                  year={year}
                  semester={semester}
                />
              ))}
              {Object.keys(schools.data.others).map((schoolCode, i) => (
                <School
                  key={i}
                  schoolCode={schoolCode}
                  schoolName={schools.data.others[schoolCode].name}
                  year={year}
                  semester={semester}

                />
              ))}
            </div>
            <div>
              <SchoolType>Graduate</SchoolType>
              {Object.keys(schools.data.graduate).map((schoolCode, i) => (
                <School
                  key={i}
                  schoolCode={schoolCode}
                  schoolName={schools.data.graduate[schoolCode].name}
                  year={year}
                  semester={semester}

                />
              ))}
            </div>
          </Schools>
        )}
      </SchoolsContainer>
    </div>
  );
}

const fadeIn = keyframes`
  from { opacity: 0; }
  to { opacity: 1; }
`;

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

const SearchContainer = styled.div`
  position: relative;
  min-height: 60vh;
`;

const SchoolsContainer = styled.div`
  width: 100%;
  min-height: 50vh;
  border-top: 1rem solid var(--grey200);
  background-color: var(--grey300);
  position: relative;

  & > #departmentTitle {
    font-size: 1.7rem;
    padding: 1rem;
    font-weight: bold;
    color: var(--grey700);
    position: absolute;
    top: 0.3rem;
    left: 0.3rem;
    animation: ${fadeIn} 1.2s ease forwards;
  }
`;

const Schools = styled.div`
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, minmax(22rem, 1fr));
  grid-gap: 0.5rem;
  padding: 6rem 2rem 2rem 2rem;
  animation: ${deptFadeIn} 0.8s ease forwards;
  @media (max-width: 1000px) {
    grid-template-columns: 1fr;
  }
`;

const SchoolType = styled.div`
  font-size: 1.5rem;
  padding: 1rem 0.5rem;
  font-weight: bold;
  color: var(--grey700);
  top: 0.3rem;
  animation: ${fadeIn} 1.2s ease forwards;
`;
