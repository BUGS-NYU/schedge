import React, { useState } from "react";
import { useQuery } from "react-query";
import { usePageState } from "components/state";
import styles from "./school.module.css";
import Link from "next/link";
import { useRouter } from "next/router";

export default function SchoolPage() {
  const router = useRouter();
  const { school } = router.query;
  const { year, semester } = usePageState();

  const { isLoading, data: subjects = {} } = useQuery(
    ["subjects", year, semester, school],
    async () => {
      const response = await fetch("https://schedge.a1liu.com/subjects");
      if (!response.ok) return;

      const data = await response.json();
      return data[school];
    }
  );

  const subjectNames = Object.keys(subjects).sort();

  return (
    <div className={styles.pageContainer}>
      <div className={styles.departmentHeader}>
        <div id="departmentTitle">{school}</div>
      </div>

      {isLoading && <span>Loading...</span>}

      {!isLoading && (
        <div className={styles.departments}>
          {subjectNames.map((subjectid, i) => {
            const subject = subjects[subjectid];
            const subjectName = subject?.name ?? "";

            return (
              <Link
                href={{
                  pathname: "/subject",
                  query: { school, subject: subjectid, year, semester },
                }}
                key={i}
              >
                <a className={styles.department}>
                  <span className={styles.departmentCode}>{subjectid}</span>

                  <span className="departmentName">&nbsp; {subjectName}</span>
                </a>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
}
