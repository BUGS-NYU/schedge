import React from "react";
import styled from "styled-components";

export default function ReviewTable({
  ratings,
  remaining,
  page,
  setPage,
  isPreviousData,
}) {
  return (
    <React.Fragment>
      <RatingTable>
        <TableHeaders>
          <tr>
            <Header>Information</Header>
            <Header comment={true}>Comment</Header>
          </tr>
        </TableHeaders>
        <TableBody>
          {ratings &&
            ratings.length > 0 &&
            ratings.map((rating, idx) => {
              const date = new Date(rating.rTimestamp);
              return (
                <RatingContainer key={rating.id} isOdd={idx % 2 === 0}>
                  <InfoContainer>
                    <Rating>{rating.rClass}</Rating>
                    <Rating>{`Overall: ${rating.rOverall}`}</Rating>
                    <Rating>{`Helpful: ${rating.rHelpful}`}</Rating>
                    <Rating>{`${
                      date.getMonth() + 1
                    }/${date.getDate()}/${date.getFullYear()}`}</Rating>
                  </InfoContainer>
                  <Comment
                    dangerouslySetInnerHTML={{
                      __html: rating.rComments,
                    }}
                  />
                </RatingContainer>
              );
            })}
        </TableBody>
      </RatingTable>
      <ButtonContainer>
        {!(page === 1) && (
          <ExpandButton
            onClick={() => {
              setPage((old) => Math.max(old - 1, 1));
            }}
          >
            <ExpandMoreIcon
              style={{
                transform: "rotate(90deg)",
              }}
            />
            <span>Prev</span>
          </ExpandButton>
        )}
        {remaining > 0 && (
          <ExpandButton
            onClick={() => {
              if (!isPreviousData && remaining > 0) {
                setPage((old) => old + 1);
              }
            }}
            next={true}
          >
            <span>Next</span>
            <ExpandMoreIcon
              style={{
                transform: "rotate(-90deg)",
              }}
            />
          </ExpandButton>
        )}
      </ButtonContainer>
    </React.Fragment>
  );
}

const RatingTable = styled.table`
  width: 40vw;
  border-spacing: 0;
  border-collapse: collapse;
`;

const TableHeaders = styled.thead`
  width: 100%;
`;

const Header = styled.th`
  border-top: 1.2px solid var(--grey600);
  border-bottom: 1.2px solid var(--grey600);
  padding: 1rem;
  background-color: var(--grey500);
  border-left: ${(props) =>
    props.comment ? "1.2px solid var(--grey600)" : ""};
`;

const TableBody = styled.tbody`
  width: 100%;
  background-color: var(--grey400);
  height: 100vh;
`;

const RatingContainer = styled.tr`
  padding: 0.4rem;
  font-size: 1rem;
  color: var(--grey900);
  font-weight: bold;
  background-color: ${(props) =>
    props.isOdd ? "var(--grey400)" : "var(--grey300)"};
  border-bottom: 1.2px solid var(--grey600);
  border-top: 1.2px solid var(--grey600);
`;

const InfoContainer = styled.td`
  text-align: center;
  vertical-align: middle;
  padding: 0.4rem 0;
`;

const Rating = styled.p`
  padding: 0.1rem 0.6rem;
`;

const Comment = styled.td`
  padding: 1rem;
  border-left: 1.2px solid var(--grey600);
  width: 75%;
`;

const ExpandButton = styled.div`
  font-size: 1.1rem;
  padding: 0.8rem 0.6rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background-color: var(--grey100);
  color: var(--grey800);
  transition: 0.1s;
  float: ${(props) => (props.next ? "right" : "left")};

  :hover {
  }
`;

const ButtonContainer = styled.div`
  background-color: var(--grey100);
`;
