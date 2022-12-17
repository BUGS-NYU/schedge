import React from "react";
import { useQuery } from "react-query";
import { useRouter } from "next/router";
import { usePageState } from "./state";
import headerStyles from "./css/header.module.css";
import Link from "next/link";
import axios from "axios";
import { z } from "zod";
import EditCalendarSVG from "./edit-calendar.svg";
import cx from "classnames";
import { NumberStringSchema, Semester, SemesterSchema, Term } from "./types";

export const ScheduleButton: React.VFC<{
  className?: string;
  style?: React.CSSProperties;
}> = ({ className, style }) => {
  const router = useRouter();

  return (
    <button
      className={cx(headerStyles.scheduleButton, className)}
      style={style}
      onClick={() =>
        router.pathname === "/schedule"
          ? router.back()
          : router.push("/schedule")
      }
    >
      <EditCalendarSVG
        className={cx(headerStyles.scheduleIcon, className)}
        alt="Edit Calendar"
      />
    </button>
  );
};

const semName: Record<Semester, string> = {
  ja: "January",
  sp: "Spring",
  su: "Summer",
  fa: "Fall",
};

async function fetchTerms(): Promise<Record<string, Term>> {
  const resp = await axios.get("/api/terms");
  const data = z.array(z.string()).parse(resp.data);
  const terms = data.map((term: string): Term => {
    return {
      sem: SemesterSchema.parse(term.substring(0, 2)),
      year: NumberStringSchema.parse(term.substring(2)),
      code: term,
    };
  });

  const semNum = (sem: Semester): number => {
    switch (sem) {
      case "ja":
        return 0;
      case "sp":
        return 1;
      case "su":
        return 2;
      case "fa":
        return 3;
    }
  };

  terms.sort((t1, t2) => {
    if (t1.year !== t2.year) return t1.year - t2.year;
    return semNum(t1.sem) - semNum(t2.sem);
  });
  terms.reverse();

  const termsByCode = {};
  for (const term of terms) {
    termsByCode[term.code] = term;
  }

  return termsByCode;
}

export const MainLayout: React.FC = ({ children }) => {
  const { data: termsByCode = {} } = useQuery(["terms"], fetchTerms);

  const term = usePageState((s) => s.term);
  const update = usePageState((s) => s.update);

  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        height: "100%",
        width: "100%",
        padding: "0px 5%",
        gap: "16px",
      }}
    >
      <div className={headerStyles.headerBox}>
        <div className={headerStyles.titleBox}>
          <Link href="/">
            <a className={headerStyles.title}>Bobcat Search</a>
          </Link>

          <div className={headerStyles.selectWrapper}>
            <select
              className={headerStyles.selectBox}
              value={term.code}
              onChange={(evt) => {
                const newTerm = termsByCode[evt.target.value];
                newTerm && update(newTerm);
              }}
            >
              {Object.values(termsByCode).map((term) => {
                return (
                  <option key={term.code} value={term.code}>
                    {semName[term.semester]} {term.year}
                  </option>
                );
              })}
            </select>

            <svg
              className={headerStyles.selectArrow}
              focusable="false"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path d="M7 10l5 5 5-5z"></path>
            </svg>
          </div>
        </div>

        <ScheduleButton />
      </div>

      {children}
    </div>
  );
};
