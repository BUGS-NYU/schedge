import React from "react";
import { connect } from "react-redux";
import PropTypes from "prop-types";

import styled from "styled-components";
import { CalendarTodayTwoTone, AddBoxTwoTone } from "@material-ui/icons";
import { grey } from "@material-ui/core/colors";

import Attributes from "./Attributes";
import DateSection from "./DateSection";
import {
  convertUnits,
  splitLocation,
  changeStatus,
  styleStatus,
} from "../utils"; // eslint-disable-line no-unused-vars

import * as actions from "../redux/modules/wishlist";

function Recitation({
  year,
  semester,
  wishlistCourse,
  recitation,
  sortedRecitationsMeetings,
  courseName,
  lastRecitation,
}) {
  console.log(lastRecitation);
  return (
    <RecitationContainer lastRecitation={lastRecitation}>
      {courseName !== recitation.name && (
        <h3 className="sectionName">{recitation.name}</h3>
      )}
      <h4 className="sectionNum">{recitation.code}</h4>
      <Attributes
        instructors={recitation.instructors}
        building={splitLocation(recitation.location).Building}
        units={convertUnits(recitation.minUnits, recitation.maxUnits)}
        status={recitation.status}
        type={recitation.type}
        registrationNumber={recitation.registrationNumber}
      />
      <RecitationDescription>{recitation.notes}</RecitationDescription>

      {sortedRecitationsMeetings && (
        <DateSection sortedSectionMeetings={sortedRecitationsMeetings} />
      )}
      <UtilBar>
        <StatusContainer>
          <CalendarTodayTwoTone
            style={{
              color: styleStatus(recitation.status),
            }}
          />
          <span
            style={{
              color: styleStatus(recitation.status),
            }}
          >
            {changeStatus(recitation)}
          </span>
        </StatusContainer>
        <WishlistButton
          onClick={() =>
            wishlistCourse({
              year,
              semester,
              course: recitation,
            })
          }
        >
          <AddBoxTwoTone
            style={{
              color: grey[700],
            }}
          />
          <span
            style={{
              color: grey[700],
            }}
          >
            Add to Wishlist
          </span>
        </WishlistButton>
      </UtilBar>
    </RecitationContainer>
  );
}

Recitation.propTypes = {
  year: PropTypes.number.isRequired,
  semester: PropTypes.string.isRequired,
  wishlistCourse: PropTypes.func.isRequired,
  recitation: PropTypes.object.isRequired,
  sortedRecitationsMeetings: PropTypes.array.isRequired,
  courseName: PropTypes.string.isRequired,
  lastRecitation: PropTypes.bool.isRequired,
};

const RecitationContainer = styled.div`
  padding: 1.8vmin 2.8vmin;
  background-color: var(--grey100);
  width: 100%;
  margin-left: 1%;
  position: relative;
  border-bottom: ${(props) => (props.lastRecitation ? "" : "1px solid")};

  & > .sectionName {
    font-size: 1.8rem;
    font-family: var(--condensedFont);
    color: var(--grey800);
    margin-bottom: 0.25rem;
  }

  & > .sectionNum {
    font-size: 1.6rem;
    font-family: var(--condensedFont);
    color: var(--grey700);
    margin: 0 0 -1rem 1rem;
  }

  & > .attributes {
    display: flex;
    flex-wrap: wrap;
  }
`;

const RecitationDescription = styled.div`
  padding: 0 1.5rem 1.5rem 0.5rem;
  max-width: 68%;
  color: var(--grey700);
`;

const UtilBar = styled.div`
  padding: 0.5rem;
  height: 6vh;
  display: flex;
  justify-content: flex-start;
  align-items: center;
`;

const StatusContainer = styled.div`
  font-size: 1.1rem;
  height: 100%;
  width: 12rem;
  border-radius: 0.6rem;
  padding: 0.8rem 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: ${grey[200]};
  margin-right: 2rem;

  & > svg {
    margin-right: 0.65rem;
  }
`;

const WishlistButton = styled.div`
  font-size: 1.1rem;
  height: 100%;
  width: 12rem;
  border-radius: 0.6rem;
  padding: 0.8rem 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background-color: ${grey[200]};
  margin-right: 2rem;
  transition: 0.1s;

  :hover {
    background-color: ${grey[300]};
  }

  & > svg {
    margin-right: 0.65rem;
  }
`;

const mapStateToProps = (state, props) => ({
  wishlist: state.wishlist[props.semester + props.year] || [],
  scheduled: state.scheduled[props.semester + props.year] || [],
});

export default connect(mapStateToProps, actions)(Recitation);
