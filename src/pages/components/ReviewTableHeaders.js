import React from "react";
import styled from "styled-components";

export default function ReviewTableHeaders({
  name,
  totalRatings,
  overallRating,
}) {
  return (
    <MetaContainer>
      <div className="instructorName">{name}</div>
      <div>{`${totalRatings} review${totalRatings >= 1 ? "s" : ""}`}</div>
      <div>
        {overallRating > -1 ? `Overall ${overallRating}` : "No Overall Rating"}
      </div>
    </MetaContainer>
  );
}


const MetaContainer = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: calc(0.8vmin + 0.8rem);
  font-size: 1.4rem;
  color: var(--grey200);
  font-weight: bold;
  background: linear-gradient(
    167deg,
    var(--purpleMain) 21%,
    #712991 60%,
    rgba(135, 37, 144, 1) 82%
  );

  > div {
    padding: 0.2rem 0;
  }

  & > .instructorName {
    font-size: 2rem;
  }
`;
