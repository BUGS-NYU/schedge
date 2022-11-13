import React from "react";
import ReviewTableHeaders from "./ReviewTableHeaders";
import ReviewTable from "./ReviewTable";
import { useQuery } from "react-query";

export default function ReviewsBuilder({ currentInstructor }) {
  const [page, setPage] = React.useState(1);

  const rmpKey = ["rmp-ratings", page, currentInstructor];
  const { isLoading, error, data } = useQuery(rmpKey, async () => {
    const url = `https://www.ratemyprofessors.com/paginate/professors/ratings?tid=${currentInstructor.rmpId}&page=${page}`;
    const resp = await fetch(url);
    const data = await resp.json();

    return data;
  });

  if (!isLoading) return <div>Loading...</div>;
  if (error) return <div>Error.... {error.message}</div>;

  return (
    <React.Fragment>
      <ReviewTableHeaders
        name={currentInstructor.name}
        totalRatings={currentInstructor.totalRatings}
        overallRating={currentInstructor.overallRating}
      />

      <ReviewTable
        ratings={data.ratings}
        remaining={data.remaining}
        setPage={setPage}
        page={page}
      />
    </React.Fragment>
  );
}
