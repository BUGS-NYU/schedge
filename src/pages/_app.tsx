import "./App.css";
import React from "react";
import headerStyles from "./header.module.css";
import Link from "next/link";
import { useRouter } from "next/router";
import {
  parseTerm,
  Semester,
  SemesterSchema,
  semName,
  Term,
  usePageState,
} from "components/state";
import { QueryClient, QueryClientProvider, useQuery } from "react-query";
import { QueryNumberSchema, useQueryParam } from "../components/useQueryParam";
import axios from "axios";
import { z } from "zod";

const queryClient = new QueryClient();

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

const Inner: React.FC = ({ children }) => {
  const { data: termData } = useQuery(["terms"], fetchTerms);
  const router = useRouter();

  const term = usePageState((s) => s.term);
  const update = usePageState((s) => s.update);

  const selectRef = React.useRef<HTMLSelectElement>();

  // Copy from this for now; we can decide on MUI or whatever later, but it's
  // basically just used for this one component.
  //
  // https://github.com/A1Liu/a1liu/blob/94d576634459ce9b954307b4fc7fad37c624c5bb/pages/dev/card-cutter.tsx#L78
  return (
    <>
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
    </>
  );
};

function App({ Component, pageProps }) {
  const update = usePageState((s) => s.update);

  const [year] = useQueryParam("year", QueryNumberSchema);
  const [semester] = useQueryParam("semester", SemesterSchema);

  React.useEffect(() => {
    if (year) {
      update({ year });
    }

    if (semester) {
      update({ semester });
    }
  }, [update, year, semester]);

  return (
    <QueryClientProvider client={queryClient}>
      <Inner>
        <Component {...pageProps} />
      </Inner>
    </QueryClientProvider>
  );
}

export default App;
