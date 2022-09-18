import React, { useState } from "react";
import Link from "next/link";
import cx from 'classnames';
import styles from './SearchBar.module.css';

export default function SearchBar({ year, semester }) {
  const [searchText, setSearchText] = useState("");
  const [searchResults, setSearchResults] = useState({
    loading: false,
    results: [],
  });

  const _handleChange = async (event) => {
    if (event.target.value.length >= 50) {
      setSearchText(event.target.value.substring(0, 50));
      return;
    }
    setSearchText(event.target.value);

    // handle empty search text
    if (event.target.value.replace(/\s/g, "").length === 0) {
      setSearchResults({ loading: false, results: [] });
      return;
    }
    setSearchResults({ loading: true, results: [] });
    // fetch results
    fetch(
      `https://schedge.a1liu.com/${year}/${semester}/search?query=${event.target.value.replace(
        /\s/g,
        "+"
      )}&limit=5`
    )
      .then((response) => {
        if (!response.ok) {
          setSearchResults({ loading: false, results: [] });
          return;
        }
        return response.json();
      })
      .then((results) => setSearchResults({ loading: false, results }))
      .catch((error) => console.error(error));
  };

  return (
    <>
      <img Loader
        className={cx(styles.loader, !!searchResults.loading && styles.loading)}
        src="./loading.svg"
        alt="loading symbol"
      />

      <input
        className={styles.searchBox}
        value={searchText}
        placeholder="Search Courses"
        onChange={_handleChange}
      />

      <div className={styles.searchResults}>
        {searchText.replace(/\s/g, "").length !== 0 &&
          searchResults.results.map((course, i) => (
            <Link
              href={{
                pathname: "/course",
                query: `school=${course.subjectCode.school}&subject=${course.subjectCode.code}&courseid=${course.deptCourseId}&year=${year}&semester=${semester}`,
              }}
              key={i}
              style={{ textDecoration: "none" }}
            >
              <div className={styles.course}>
                <span className={styles.courseSchoolCode}>
                  {course.subjectCode.school}-{course.subjectCode.code}
                </span>

                <span className={styles.courseId}>{course.deptCourseId}</span>
                <span className={styles.courseName}>{course.name}</span>
              </div>
            </Link>
          ))}
      </div>
    </>
  );
}
