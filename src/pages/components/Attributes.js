import React from "react";
import PropTypes from "prop-types";

import styled from "styled-components";
import Instructor from "./Instructor";

export default function Attributes({
  instructors,
  building,
  room,
  units,
  status, // eslint-disable-line no-unused-vars
  type,
  registrationNumber,
}) {
  return (
    <div className="attributes">
      <AttributeContainer>
        <div className="attributeLabel">
          Instructor{instructors.length > 1 ? "s" : ""}
        </div>
        {instructors.map((instructor) => {
          return <Instructor key={instructor} instructor={instructor} />;
        })}
      </AttributeContainer>
      <AttributeContainer>
        <div className="attributeLabel">Building</div>
        {building}
      </AttributeContainer>
      {room && (
        <AttributeContainer>
          <div className="attributeLabel">Room</div>
          {room}
        </AttributeContainer>
      )}
      <AttributeContainer>
        <div className="attributeLabel">Units</div>
        {units}
      </AttributeContainer>
      <AttributeContainer>
        <div className="attributeLabel">Type</div>
        {type}
      </AttributeContainer>
      <AttributeContainer>
        <div className="attributeLabel">Registration #</div>
        {registrationNumber}
      </AttributeContainer>
    </div>
  );
}

Attributes.propTypes = {
  instructors: PropTypes.array.isRequired,
  building: PropTypes.string.isRequired,
  room: PropTypes.string,
  units: PropTypes.number.isRequired,
  status: PropTypes.string.isRequired,
  type: PropTypes.string.isRequired,
  registrationNumber: PropTypes.number.isRequired,
};

const AttributeContainer = styled.div`
  padding: calc(0.8vmin + 0.8rem);
  font-size: 1.5rem;
  color: var(--grey800);
  font-weight: bold;

  & > .attributeLabel {
    font-size: 1rem;
    font-family: var(--condensedFont);
    color: var(--grey700);
  }
`;
