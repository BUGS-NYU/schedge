import React from "react";
import { useQuery } from "react-query";
import styled from "styled-components";
import ReviewsBuilder from "./ReviewsBuilder";
import ReviewTableHeaders from "./ReviewTableHeaders";
import ReviewTable from "./ReviewTable";

export default function Instructor({ instructor }) {
  const [drawer, setDrawer] = React.useState(false);
  const names = instructor.split(" ");

  const query =
    names.length >= 2 ? `${names[0]} ${names[names.length - 1]}` : instructor;

  const ratingsList = useQuery(["rmp-instructors", query], async () => {
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
  });

  const { data, isLoading, error } = ratingsList;

  if (!isLoading || !data) return <div>Loading</div>;
  if (error) return <div>Error</div>;

  return (
    <React.Fragment>
      <InstructorName clickable={true} onClick={() => setDrawer(true)}>
        {instructor}
      </InstructorName>
      <div>
        {data.rmpId !== "" ? (
          <ReviewsBuilder currentInstructor={data} />
        ) : (
          <React.Fragment>
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
          </React.Fragment>
        )}
      </div>
    </React.Fragment>
  );
}

const InstructorName = styled.div`
  cursor: pointer;
  transition: 0.1s;

  :hover {
  }
`;
