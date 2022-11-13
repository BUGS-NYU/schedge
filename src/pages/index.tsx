import React from "react";
import { useQuery } from "react-query";
import { SchoolSchema, usePageState } from "components/state";
import css from "./index.module.css";
import SearchBar from "components/SearchBar";
import axios from "axios";
import { z } from "zod";
import Link from "next/link";

export const SchoolInfoSchema = z.object({
  term: z.string(),
  schools: z.array(SchoolSchema),
});

function Home() {
  const { term } = usePageState();

  const { data: schools } = useQuery(["schools", term.code], async () => {
    const resp = await axios.get(`/api/schools/${term.code}`);
    return SchoolInfoSchema.parse(resp.data);
  });

  return (
    <div id="pageContainer">
      <div className={css.searchContainer}>
        <SearchBar year={term.year} semester={term.semester} />
      </div>
      <div className={css.schoolsContainer}>
        <div id="departmentTitle">Schools</div>
        {!!schools && (
          <div className={css.schools}>
            <div>
              <div className={css.schoolType}>Undergraduate</div>
              {schools.schools.map((school, i) => (
                <div className={css.schoolContainer}>
                  <Link
                    className={css.schoolLink}
                    style={{ textDecoration: "none" }}
                    href={{
                      pathname: "/school",
                      query: `schoolIndex=${i}`,
                    }}
                  >
                    <div className={css.schoolTitle}>
                      <span className={css.schoolCode}>
                        {school.subjects[0]?.code?.split("-")?.[1]}
                      </span>
                      <span className={css.schoolName}>{school.name}</span>
                    </div>
                  </Link>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Home;
