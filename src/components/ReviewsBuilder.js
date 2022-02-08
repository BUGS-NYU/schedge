import React from "react";
import ReviewTableHeaders from "./ReviewTableHeaders";
import ReviewTable from "./ReviewTable";
import { useAsync } from "components/hooks";

export default function ReviewsBuilder({ currentInstructor }) {
  const [page, setPage] = React.useState(1);

  const { isLoaded, error, data } = useAsync(async () => {
    const url = `https://www.ratemyprofessors.com/paginate/professors/ratings?tid=${currentInstructor.rmpId}&page=${page}`;
    const resp = await fetch(url);

    const a = await res.json();
    console.log("RES", a);
  }, [page, currentInstructor]);

  if (!isLoaded) return <div> Loading...</div>;
  if (error) return <div>Error....{error.message}</div>;

  console.log(data);

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
