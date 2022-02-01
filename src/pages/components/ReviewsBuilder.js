import React, { useState } from "react";
import PropTypes from "prop-types";
import { useQuery } from "react-query";
import ReviewTableHeaders from "./ReviewTableHeaders";
import ReviewTable from "./ReviewTable";

export default function ReviewsBuilder({ currentInstructor }) {
  const [page, setPage] = useState(1);

  const fetchReviewsWithPage = async (page) => {
    return fetch(
      `https://www.ratemyprofessors.com/paginate/professors/ratings?tid=${currentInstructor.rmpId}&page=${page}`
    ).then((res) => res.json());
  };

  const {
    isLoading,
    isError,
    error,
    data,
    isFetching, // eslint-disable-line no-unused-vars
    isPreviousData,
  } = useQuery(
    ["rmp", currentInstructor.name, page],
    () => fetchReviewsWithPage(page),
    {
      keepPreviousData: true,
      cacheTime: Infinity, //for now
      staleTime: Infinity,
    }
  );

  if (isLoading) return <div> Loading...</div>;
  if (isError) return <div> {`Error....${error.message}`}</div>;

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
        isPreviousData={isPreviousData}
        page={page}
      />
    </React.Fragment>
  );
}

ReviewsBuilder.propTypes = {
  currentInstructor: PropTypes.object.isRequired,
};
