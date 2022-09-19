import React, { useState, useEffect } from "react";
import styles from "./school.module.css";
import Link from "next/link";
import { useRouter } from "next/router";

export default function SchoolPage() {
  const router = useRouter();
  const { school, year, semester } = router.query;
  const [loading, setLoading] = useState(true);
  const [subjects, setSubjects] = useState({});

  useEffect(() => {
    if (!router.isReady) {
      return;
    }

    async function query() {
      try {
        const response = await fetch("https://schedge.a1liu.com/subjects");
        if (!response.ok) {
          // handle invalid search parameters
          return;
        }

        const data = await response.json();

        setLoading(false);

        const subjects = data[school];
        if (subjects) {
          setSubjects(subjects);
        }
      } catch (error) {
        console.error(error);
      }
    }

    query();
  }, [school, router]);

  const subjectNames = Object.keys(subjects).sort();

  return (
    <div className={styles.pageContainer}>
      <div className={styles.departmentHeader}>
        <div id="departmentTitle">{school}</div>
      </div>

      {loading && <span>Loading...</span>}

      {!loading && (
        <div className={styles.departments}>
          {subjectNames.map((subjectid, i) => {
            const subject = subjects[subjectid];
            const subjectName = subject?.name ?? "";
            const query = `school=${school}&subject=${subjectid}&year=${year}&semester=${semester}`;

            return (
              <Link href={{ pathname: "/subject", query }} key={i}>
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
