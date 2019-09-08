import React, { Dispatch, useReducer } from "react";
import withStyles, { WithStyles } from "react-jss";
import { blue, red } from "@material-ui/core/colors";
import { MuiThemeProvider, createMuiTheme } from "@material-ui/core/styles";
import FreshmenForm from "./FreshmenForm";
import HomePage from "./HomePage";
import { Link, Router } from "@reach/router";
import Typography from "@material-ui/core/Typography";
import SchedulePage from "./SchedulePage";
import { APIMeeting, Meeting } from "./types";
import LoadingScreen from "./LoadingScreen";
import SophomoresForm from "./SophomoresForm";

const styles = {
  App: {
    display: "flex",
    flexDirection: "column"
  },
  header: {
    display: "flex",
    padding: "20px",
    alignItems: "center",
    height: "100px",
    fontSize: "1.1em"
  },
  headerText: {
    textDecoration: "none",
    color: "black",
    "&:visited": {
      textDecoration: "none"
    }
  }
};

const theme = createMuiTheme({
  palette: { type: "light", primary: blue, secondary: red }
});

interface State {
  schedule: Array<Meeting> | undefined;
}

type ActionTypes = { type: "ADD_SCHEDULE"; schedule: Array<Meeting> };

function reducer(state: State, action: ActionTypes) {
  switch (action.type) {
    case "ADD_SCHEDULE":
      return { ...state, schedule: action.schedule };
    default:
      return state;
  }
}

const initialState = {
  schedule: undefined
};

const dayOffsets = {
  Mon: 0,
  Tues: 1,
  Wed: 2,
  Thurs: 3,
  Fri: 4
};

const totalMinutesToDateTime = (totalMinutes: number, dayOffset: number) => {
  const hours = totalMinutes / 60;
  const minutes = totalMinutes % 60;
  return new Date(2019, 8, 9 + dayOffset, hours, minutes);
};

const addSchedule = (dispatch: Dispatch<ActionTypes>) => (
  scheduleResponse: Array<APIMeeting>
) => {
  const schedule = scheduleResponse.flatMap(
    ({ startTime, endTime, days, courseName, professor, location }) => [
      {
        title: courseName,
        startDate: totalMinutesToDateTime(startTime, dayOffsets[days[0]]),
        endDate: totalMinutesToDateTime(endTime, dayOffsets[days[0]]),
        professor,
        location
      },
      {
        title: courseName,
        startDate: totalMinutesToDateTime(startTime, dayOffsets[days[1]]),
        endDate: totalMinutesToDateTime(endTime, dayOffsets[days[1]]),
        professor,
        location
      }
    ]
  );
  dispatch({ type: "ADD_SCHEDULE", schedule });
};

const App: React.FC<WithStyles<typeof styles>> = ({ classes }) => {
  const [state, dispatch] = useReducer(reducer, initialState);
  return (
    <MuiThemeProvider theme={theme}>
      <div className={classes.App}>
        <div className={classes.header}>
          <Link to="/">
            <Typography
              className={classes.headerText}
              variant="h1"
              component="h1"
            >
              Schedge
            </Typography>
          </Link>
        </div>
        <Router>
          <HomePage path="/" />
          <LoadingScreen path="/loading" />
          {state.schedule ? (
            <SchedulePage schedule={state.schedule} path="/schedule" />
          ) : (
            <HomePage path="/schedule" />
          )}
          <FreshmenForm addSchedule={addSchedule(dispatch)} path="/freshmen" />
          <SophomoresForm
            addSchedule={addSchedule(dispatch)}
            path="/sophomores"
          />
        </Router>
      </div>
    </MuiThemeProvider>
  );
};

export default withStyles(styles)(App);
