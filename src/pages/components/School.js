import React from "react";
import Link from "next/link";
import PropTypes from "prop-types";
import styled from "styled-components";
import { grey } from "@material-ui/core/colors";
import { findSchool } from "../utils";

export default function School({ schoolCode, schoolName, year, semester }) {
  return (
    <SchoolContainer>
      <Link
        className="schoolLink"
        href={{
          pathname: "/school",
          query: `school=${schoolCode}&year=${year}&semester=${semester}`,
          /*state: {
            schoolName: schoolName !== "" ? schoolName : findSchool(schoolCode),
          },*/
        }}
        style={{ textDecoration: "none" }}
      >
        <div className="schoolTitle">
          <span className="schoolCode">{schoolCode}</span>
          <span className="schoolName">
            {schoolName !== "" ? schoolName : findSchool(schoolCode)}
          </span>
        </div>
      </Link>
    </SchoolContainer>
  );
}

School.propTypes = {
  schoolCode: PropTypes.string.isRequired,
  schoolName: PropTypes.string.isRequired,
};

const SchoolContainer = styled.div`
  padding: 0.25rem 0;
  cursor: pointer;

  & > .schoolLink > .schoolTitle {
    font-size: 1.2rem;
    font-family: var(--condensedFont);
    text-align: left;
    margin: 0.2rem 0;
    position: sticky;

    & > .schoolCode {
      padding: 0.5rem;
      color: var(--grey600);
      font-weight: 800;
    }

    & > .schoolName {
      color: var(--grey900);
    }
  }

  &:hover {
    background-color: ${grey[400]};
  }
`;
