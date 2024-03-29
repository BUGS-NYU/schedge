import React, { useState } from "react";
import Link from "next/link";
import cx from "classnames";
import styles from "./css/SearchBar.module.css";
import { useQuery } from "react-query";
import axios from "axios";
import { useSchools } from "pages/index";
import Loading from "components/img/loading.svg";
import { Term } from "./types";

interface Props {
  term: Term;
}

export const SearchBar: React.VFC<Props> = ({ term }) => {
  const [rawText, setSearchText] = useState("");

  const query = rawText.substring(0, 50);
  const {
    data: searchResults,
    isLoading,
    error,
  } = useQuery(["search", term.code, query], async () => {
    const resp = await axios.get(`/api/search/${term.code}`, {
      params: { query, limit: 5 },
    });

    return [...resp.data].sort((a, b) => a.deptCourseId - b.deptCourseId);
  });
  const { data: schools } = useSchools(term);

  React.useEffect(() => {
    if (error) {
      console.error(error);
    }
  }, [error]);

  return (
    <div>
      <Loading
        className={cx(styles.loader, !!isLoading && styles.loading)}
        alt="loading symbol"
      />

      <input
        className={styles.searchBox}
        value={rawText}
        placeholder="Search Courses"
        onChange={(evt) => setSearchText(evt.target.value)}
      />

      <div className={styles.searchResults}>
        {!!searchResults &&
          searchResults.map((course, i) => {
            const schoolIndex = schools?.schools.findIndex(
              (school) =>
                school.subjects.findIndex(
                  (subject) => subject.code === course.subjectCode
                ) !== -1
            );
            return (
              <Link
                key={i}
                href={{
                  pathname: "/course",
                  query: {
                    schoolIndex,
                    subject: course.subjectCode,
                    courseid: course.deptCourseId,
                  },
                }}
              >
                <a className={styles.course} style={{ textDecoration: "none" }}>
                  <span className={styles.courseSchoolCode}>
                    {course.subjectCode} {course.deptCourseId}
                  </span>
                  <span className={styles.courseName}>{course.name}</span>
                </a>
              </Link>
            );
          })}
      </div>
    </div>
  );
};
