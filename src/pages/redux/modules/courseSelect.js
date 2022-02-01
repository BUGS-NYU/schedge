const TOGGLE_COURSE_SELECT = "bobcat-search/courseselect/TOGGLE_COURSE_SELECT";
const CLEAR_SCHEDULE = "bobcat-search/courseselect/CLEAR_SCHEDULE";
const initialState = {};

export default function reducer(state = initialState, action = {}) {
  switch (action.type) {
    case TOGGLE_COURSE_SELECT:
      //handle conflicting course
      if (action.payload.conflicts !== undefined) {
        return {
          ...state,
          [action.payload.semester + action.payload.year]: state[
            action.payload.semester + action.payload.year
          ].filter((course) => {
            const isFirstConflict =
              course.courseRegistrationNumber ===
              action.payload.conflicts[0].courseRegistrationNumber;
            const isSecondConflict =
              course.courseRegistrationNumber ===
              action.payload.conflicts[1].courseRegistrationNumber;
            return !isFirstConflict && !isSecondConflict;
          }),
        };
      }
      // let { year, semester,  courseRegistrationNumber, recitationRegistrationNumber } = action.payload;
      if (
        state[action.payload.semester + action.payload.year] !== undefined &&
        state[action.payload.semester + action.payload.year].filter(
          (selection) =>
            selection.courseRegistrationNumber ===
            action.payload.courseRegistrationNumber
        ).length !== 0
      ) {
        return {
          ...state,
          [action.payload.semester + action.payload.year]: state[
            action.payload.semester + action.payload.year
          ].filter(
            (course) =>
              course.courseRegistrationNumber !==
              action.payload.courseRegistrationNumber
          ),
        };
      }

      // eslint-disable-next-line no-case-declarations
      const arrSpread =
        state[action.payload.semester + action.payload.year] || [];
      return {
        ...state,
        [action.payload.semester + action.payload.year]: [
          ...arrSpread,
          {
            courseRegistrationNumber: action.payload.courseRegistrationNumber,
            recitationRegistrationNumber:
              action.payload.recitationRegistrationNumber,
          },
        ],
      };
    case CLEAR_SCHEDULE:
      if (
        state[action.payload.semester + action.payload.year] !== undefined &&
        state[action.payload.semester + action.payload.year].length > 0
      ) {
        return {
          ...state,
          [action.payload.semester + action.payload.year]: [],
        };
      }
      return state;
    default:
      return state;
  }
}

export const toggleCourseSelect = (courseRegistrationNumber) => ({
  type: TOGGLE_COURSE_SELECT,
  payload: courseRegistrationNumber, // { year, semester, courseRegistrationNumber, recitationRegistrationNumber }
});

export const clearSchedule = ({ year, semester }) => ({
  type: CLEAR_SCHEDULE,
  payload: { year, semester },
});
