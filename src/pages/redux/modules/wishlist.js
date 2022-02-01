const WISHLIST_COURSE = "bobcat-search/wishlist/WISHLIST_COURSE";
const CLEAR_WISHLIST = "bobcat-search/wishlist/CLEAR_WISHLIST";

const initialState = {};

export default function reducer(state = initialState, action = {}) {
  switch (action.type) {
    case WISHLIST_COURSE:
      // let { year, semester, course } = action.payload;
      if (
        state[action.payload.semester + action.payload.year] !== undefined &&
        state[action.payload.semester + action.payload.year].filter(
          (course) =>
            course.registrationNumber ===
            action.payload.course.registrationNumber
        ).length !== 0
      ) {
        // If course exists, remove it
        return {
          ...state,
          [action.payload.semester + action.payload.year]: state[
            action.payload.semester + action.payload.year
          ].filter(
            (course) =>
              course.registrationNumber !==
              action.payload.course.registrationNumber
          ),
        };
      }

      // if we have an entry for the current semester in our waitlist get its state
      // eslint-disable-next-line no-case-declarations
      const arrSpread =
        state[action.payload.semester + action.payload.year] || [];
      return {
        ...state,
        [action.payload.semester + action.payload.year]: [
          action.payload.course,
          ...arrSpread,
        ],
      };
    case CLEAR_WISHLIST:
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

export const wishlistCourse = (course) => ({
  type: WISHLIST_COURSE,
  payload: course, // { year, semester, course data}
});

export const clearWishlist = ({ year, semester }) => ({
  type: CLEAR_WISHLIST,
  payload: { year, semester },
});
