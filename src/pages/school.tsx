import React from "react";
import anim from "components/css/animation.module.css";
import fonts from "components/css/fonts.module.css";
import cx from "classnames";
import { usePageState } from "components/state";
import styles from "./school.module.css";
import Link from "next/link";
import { useSchools } from "./index";
import { useQueryParam } from "components/useQueryParam";
import { MainLayout } from "components/Layout";
import { NumberStringSchema } from "components/types";

export default function SchoolPage() {
  const term = usePageState((s) => s.term);

  const [schoolIndex] = useQueryParam("schoolIndex", NumberStringSchema);
  const { data: { schools } = {}, isLoading } = useSchools(term);
  const school = schools?.[schoolIndex];

  return (
    <MainLayout>
      <div className={fonts.heading2}>{school?.name}</div>

      {isLoading && <span>Loading...</span>}

      {!!school && (
        <div className={cx(anim.verticalFadeIn, styles.departments)}>
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
                  <span className={fonts.boldInfo}>{subject.code}</span>

                  <span className={fonts.body2}>&nbsp; {subject.name}</span>
                </a>
              </Link>
            );
          })}
        </div>
      )}
    </MainLayout>
  );
}
