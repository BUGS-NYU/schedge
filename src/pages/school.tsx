import React from "react";
import { usePageState } from "components/state";
import styles from "./school.module.css";
import Link from "next/link";
import { useSchools } from "./index";
import { QueryNumberSchema, useQueryParam } from "../components/useQueryParam";

export default function SchoolPage() {
  const { term } = usePageState();

  const [schoolIndex] = useQueryParam("schoolIndex", QueryNumberSchema);
  const { data: schools, isLoading } = useSchools(term);
  const school = schools?.schools?.[schoolIndex];
  console.log(school, schools, schoolIndex)

  return (
    <div className={styles.pageContainer}>
      <div className={styles.departmentHeader}>
        <div id="departmentTitle">{school?.name}</div>
      </div>

      {isLoading && <span>Loading...</span>}

      {!!school && (
        <div className={styles.departments}>
          {school.subjects.map((subject, i) => {
            return (
              <Link
                key={subject.code}
                href={{
                  pathname: "/subject",
                  query: { schoolIndex, subject: subject.code },
                }}
              >
                <a className={styles.department}>
                  <span className={styles.departmentCode}>{subject.code}</span>

                  <span className="departmentName">&nbsp; {subject.name}</span>
                </a>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
}
