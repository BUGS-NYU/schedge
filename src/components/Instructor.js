import React from "react";
import { useQuery } from "react-query";
import styles from "./css/instructor.module.css";
import ReviewsBuilder from "./ReviewsBuilder";
import ReviewTableHeaders from "./ReviewTableHeaders";
import ReviewTable from "./ReviewTable";

export default function Instructor({ instructor }) {
  const [drawer, setDrawer] = React.useState(false);
  const names = instructor.split(" ");

  const query =
    names.length >= 2 ? `${names[0]} ${names[names.length - 1]}` : instructor;

  const { data, isLoading, error } = useQuery(
    ["rmp-instructors", query],
    async () => {
      const url = `https://www.ratemyprofessors.com/filter/professor/?&page=1&queryBy=schoolsid&sid=675&queryoption=TEACHER&queryBy=teacher&query=${query}`;

      const res = await fetch(url);
      const data = await res.json();

      if (data === undefined || data.searchResultsTotal === 0) {
        return {
          rmpId: "",
          page: 1,
          overallRating: -1,
          totalRatings: 0,
        };
      }

      const professorInfo = data.professors[0];
      if (professorInfo.overall_rating === "N/A") {
        return {
          rmpId: "",
          page: 1,
          overallRating: -1,
          totalRatings: 0,
        };
      }

      return {
        rmpId: professorInfo.tid,
        page: 1,
        overallRating: parseFloat(professorInfo.overall_rating),
        totalRatings: professorInfo.tNumRatings,
      };
    }
  );

  if (isLoading || !data) return <div>Loading</div>;
  if (error) return <div>Error</div>;

  return (
    <>
      <button className={styles.instructorName} onClick={() => setDrawer(true)}>
        {instructor}
      </button>

      {drawer && (
        <div>
          {data.rmpId !== "" ? (
            <ReviewsBuilder currentInstructor={data} />
          ) : (
            <>
              <ReviewTableHeaders
                name={instructor}
                totalRatings={data.totalRatings}
                overallRating={data.overallRating}
              />

              <ReviewTable
                ratings={[]}
                remaining={false}
                page={1}
                setPage={() => {}}
                isPreviousData={true}
              />
            </>
          )}
        </div>
      )}
    </>
  );
}
