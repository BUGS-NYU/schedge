import React from "react";
import { connect } from "react-redux";
import PropTypes from "prop-types";
import styled from "styled-components";
import { FormControlLabel } from "@material-ui/core";
import Checkbox from "@material-ui/core/Checkbox";
import { generateScheduleTime } from "../utils";

import * as wishlistActions from "../redux/modules/wishlist";
import * as courseActions from "../redux/modules/courseSelect";

function WishlistCourse({ course, checkboxes, removeCourse, handleOnChange }) {
  return (
    <WishlistCourseContainer>
      <WishlistTextBox>
        <div>{course.name}</div>
        <div>
          Section: <span>{course.code}</span>
        </div>
        <div>
          Registration No: <span>{course.registrationNumber}</span>
        </div>
        <div
          style={{
            marginTop: "1rem",
          }}
        >
          <div>
            Type: <span>{course.type}</span>
          </div>
          <div>
            Instructors: <span>{course.instructors.join(", ")}</span>
          </div>
          <div>
            Meetings: <span>{generateScheduleTime(course.meetings)}</span>
          </div>
        </div>
        <WishlistUtilBox>
          <CustomFormControlLabel
            value="add"
            control={
              <CustomCheckbox
                checked={
                  checkboxes[course.registrationNumber] === undefined
                    ? false
                    : checkboxes[course.registrationNumber]
                }
                onChange={(e) =>
                  handleOnChange(e, course, course.registrationNumber)
                }
              />
            }
            label="Schedule"
            labelPlacement="start"
          />
          <div
            role="button"
            className="removeButton"
            onClick={() => removeCourse(course)}
            onKeyDown={() => removeCourse(course)}
            tabIndex={0}
          >
            Remove
          </div>
        </WishlistUtilBox>
      </WishlistTextBox>
    </WishlistCourseContainer>
  );
}

WishlistCourse.propTypes = {
  course: PropTypes.object.isRequired,
  checkboxes: PropTypes.object.isRequired,
  removeCourse: PropTypes.func.isRequired,
  handleOnChange: PropTypes.func.isRequired,
};

const WishlistCourseContainer = styled.div`
  min-height: 15rem;
  background-color: var(--grey300);
  border-bottom: 1px solid var(--grey200);
  border-top: 1px solid var(--grey200);
`;

const WishlistTextBox = styled.div`
  padding: 1rem;
`;

const WishlistUtilBox = styled.div`
  display: flex;
  height: 4rem;
  margin-top: 1.5rem;
  align-items: center;

  & > .removeButton {
    cursor: pointer;
    color: #bd2f2f;
    font-size: 0.9rem;
    margin-left: 1rem;
  }
`;

const CustomFormControlLabel = styled(FormControlLabel)`
  margin: 0;
  color: black;
  background-color: var(--grey400);
  border-radius: 5px;
  padding: 0 8px;
  font-weight: bold;
`;

const CustomCheckbox = styled(Checkbox)`
  color: var(--purpleMain);
  &.Mui-checked {
    color: var(--purpleMain);
  }
`;

const mapStateToProps = (state, props) => ({
  wishlist: state.wishlist[props.semester + props.year] || [],
  scheduled: state.scheduled[props.semester + props.year] || [],
});

const allActions = {
  ...wishlistActions,
  ...courseActions,
};

export default connect(mapStateToProps, allActions)(WishlistCourse);
