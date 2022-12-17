import React from "react";
import { usePageState } from "components/state";
import styles from "./school.module.css";
import Link from "next/link";
import { useSchools } from "./index";
import { useQueryParam } from "hooks/useQueryParam";
import { MainLayout } from "components/Layout";
import { NumberStringSchema } from "components/types";

export default function SchoolPage() {
  const { term } = usePageState();

  const [schoolIndex] = useQueryParam("schoolIndex", NumberStringSchema);
  const { data: schools, isLoading } = useSchools(term);
  const school = schools?.schools?.[schoolIndex];

  return (
    <MainLayout>
      <div className={styles.departmentHeader}>{school?.name}</div>

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
    </MainLayout>
  );
}
