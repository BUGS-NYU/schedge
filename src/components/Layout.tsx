import React from "react";
import { useQuery } from "react-query";
import { useRouter } from "next/router";
import { parseTerm, Semester, semName, Term, usePageState } from "./state";
import headerStyles from "./header.module.css";
import Link from "next/link";
import axios from "axios";
import { z } from "zod";

async function fetchTerms(): Promise<Term[]> {
  const resp = await axios.get("/api/terms");
  const data = z.array(z.string()).parse(resp.data);
  const terms = data.map(parseTerm);

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
    return semNum(t1.semester) - semNum(t2.semester);
  });

  terms.reverse();

  return terms;
}

export const MainLayout: React.FC = ({ children }) => {
  const { data: termData } = useQuery(["terms"], fetchTerms);
  const router = useRouter();

  const term = usePageState((s) => s.term);
  const update = usePageState((s) => s.update);

  const selectRef = React.useRef<HTMLSelectElement>();

  return (
    <div>
      <div className={headerStyles.headerBox}>
        <div className={headerStyles.titleBox}>
          <Link href="/">
            <a className={headerStyles.title}>Bobcat Search</a>
          </Link>

          <div className={headerStyles.selectWrapper}>
            <select
              ref={selectRef}
              className={headerStyles.selectBox}
              value={term.code}
              onChange={(evt) => {
                update(parseTerm(evt.target.value));
              }}
            >
              {termData?.map((term) => {
                return (
                  <option key={term.code} value={term.code}>
                    {semName(term.semester)} {term.year}
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

        <button
          className={headerStyles.scheduleButton}
          onClick={() =>
            router.pathname === "/schedule"
              ? router.back()
              : router.push("/schedule")
          }
        >
          <img
            className={headerStyles.scheduleIcon}
            src="/edit-calendar.svg"
            alt="Edit Calendar"
          />
        </button>
      </div>

      {children}
    </div>
  );
};
