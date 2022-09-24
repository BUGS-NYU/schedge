import React from "react";
import { usePageState } from "components/state";
import css from "./index.module.css";
import SearchBar from "components/SearchBar";
import School from "components/School";

function Home() {
  const { year, semester } = usePageState();
  const [departments, setDepartments] = React.useState({
    loading: true,
    data: {},
  });
  const [schools, setSchools] = React.useState({
    loading: true,
    data: {
      undergraduate: {},
      graduate: {},
      others: {},
    },
  });

  React.useEffect(() => {
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
      <div className={css.searchContainer}>
        <SearchBar year={year} semester={semester} />
      </div>
      <div className={css.schoolsContainer}>
        <div id="departmentTitle">Schools</div>
        {!schools.loading && !departments.loading && (
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
