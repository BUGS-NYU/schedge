import React from "react";
import { useQuery } from "react-query";
import { usePageState } from "components/state";
import css from "./index.module.css";
import { SearchBar } from "components/SearchBar";
import axios from "axios";
import { z } from "zod";
import Link from "next/link";
import { MainLayout } from "components/Layout";
import { SchoolSchema, Term } from "components/types";

const SchoolInfoSchema = z.object({
  term: z.string(),
  schools: z.array(SchoolSchema),
});

export const useSchools = (term: Term) => {
  return useQuery(["schools", term.code], async () => {
    const resp = await axios.get(`/api/schools/${term.code}`);
    return SchoolInfoSchema.parse(resp.data);
  });
};

function Home() {
  const term = usePageState((s) => s.term);

  const { data: schools } = useSchools(term);

  return (
    <MainLayout>
      <div className={css.searchContainer}>
        <SearchBar term={term} />
      </div>
      <div className={css.schoolsContainer}>
        <div className={css.departmentTitle}>Schools</div>
        {!!schools && (
          <div className={css.schools}>
            {schools.schools.map((school, i) => (
              <div key={school.name} className={css.schoolContainer}>
                <Link
                  href={{
                    pathname: "/school",
                    query: { schoolIndex: i },
                  }}
                >
                  <a
                    className={css.schoolTitle}
                    style={{ textDecoration: "none" }}
                  >
                    <span className={css.schoolCode}>
                      {school.subjects[0]?.code?.split("-")?.[1]}
                    </span>
                    <span className={css.schoolName}>{school.name}</span>
                  </a>
                </Link>
              </div>
            ))}
          </div>
        )}
      </div>
    </MainLayout>
  );
}

export default Home;
