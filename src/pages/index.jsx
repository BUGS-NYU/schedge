import React from "react";
import { useQuery } from "react-query";
import { usePageState } from "components/state";
import css from "./index.module.css";
import SearchBar from "components/SearchBar";
import School from "components/School";

function Home() {
  const { year, semester } = usePageState();

  const schools = useQuery(["schools", year, semester], async () => {
    const response = await fetch("https://schedge.a1liu.com/schools");
    if (!response.ok) return;

    const data = await response.json();

    const undergraduate = {};
    const graduate = {};
    const others = {};

    Object.keys(data).forEach((schoolCode) => {
      if (schoolCode.startsWith("G")) {
        graduate[schoolCode] = data[schoolCode];
      } else if (schoolCode.startsWith("U") || data[schoolCode].name !== "") {
        undergraduate[schoolCode] = data[schoolCode];
      } else {
        others[schoolCode] = data[schoolCode];
      }
    });

    return { undergraduate, graduate, others };
  });

  const departments = useQuery(["subjects", year, semester], async () => {
    const response = await fetch("https://schedge.a1liu.com/subjects");
    if (!response.ok) return;

    return await response.json();
  });

  return (
    <div id="pageContainer">
      <div className={css.searchContainer}>
        <SearchBar year={year} semester={semester} />
      </div>
      <div className={css.schoolsContainer}>
        <div id="departmentTitle">Schools</div>
        {!!schools.data && !!departments.data && (
          <div className={css.schools}>
            <div>
              <div className={css.schoolType}>Undergraduate</div>
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
              <div className={css.schoolType}>Graduate</div>
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
          </div>
        )}
      </div>
    </div>
  );
}

export default Home;
