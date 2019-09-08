import React, { useEffect, useReducer } from "react";
import { createStyles, Paper, Theme, Typography } from "@material-ui/core";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";
import { navigate, RouteComponentProps } from "@reach/router";
import Button from "@material-ui/core/Button";
import withStyles, { WithStyles } from "react-jss";
import { API_URL } from "./constants";
import axios from "axios";
import { APIMeeting } from "./types";
import Grid from "@material-ui/core/Grid";
import CircularProgress from "@material-ui/core/CircularProgress";
import makeStyles from "@material-ui/core/styles/makeStyles";

const styles = {
  SophomoresForm: {
    display: "flex",
    flexDirection: "column",
    padding: "40px"
  },
  form: {
    display: "flex",
    flexDirection: "column"
  },
  menuItem: {
    margin: "20px",
    fontSize: "1.1em"
  },
  select: {
    maxWidth: "200px",
    fontSize: "1.1em",
    margin: "20px"
  },
  button: {
    margin: "20px",
    maxWidth: "200px"
  }
};

interface State {
  department: string;
  isLoading: boolean;
  error: string | undefined;
}

type ActionTypes =
  | { type: "SELECT_DEPARTMENT"; department: string }
  | { type: "SUBMIT_FORM_FAILED"; error: string };

function reducer(state: State, action: ActionTypes) {
  switch (action.type) {
    case "SELECT_DEPARTMENT":
      return { ...state, department: action.department };
    case "SUBMIT_FORM_FAILED":
      return { ...state, error: action.error };
    default:
      return state;
  }
}

const initialState = {
  department: "",
  isLoading: true,
  error: undefined
};

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    progress: {
      margin: theme.spacing(2)
    }
  })
);

interface Props extends RouteComponentProps, WithStyles<typeof styles> {
  addSchedule: (s: Array<APIMeeting>) => void;
}

const SophomoresForm: React.FC<Props> = ({ addSchedule, classes }) => {
  const [state, dispatch] = useReducer(reducer, initialState);
  const muiClasses = useStyles();
  useEffect(() => {}, []);
  if (state.isLoading) {
    return (
      <Paper>
        <Grid
          container
          spacing={10}
          direction="column"
          alignItems="center"
          style={{ minHeight: "100vh", padding: "100px" }}
        >
          <Typography variant="h4" component="h4">
            Fetching courses...
          </Typography>
          <CircularProgress className={muiClasses.progress} />
        </Grid>
      </Paper>
    );
  }
  return (
    <Paper className={classes.SophomoresForm}>
      <form
        className={classes.form}
        onSubmit={e => {
          e.preventDefault();
          if (state.department === "") {
            dispatch({
              type: "SUBMIT_FORM_FAILED",
              error: "You must select a department"
            });
          }
          axios
            .get(`${API_URL}/schedule-by-departments/${state.department}`)
            .then(res => {
              addSchedule(res.data);
              navigate(`/loading`);
            });
        }}
      >
        <Typography variant="h3" component="h3">
          Intro Course
        </Typography>
        <Select
          className={classes.select}
          value={state.department}
          onChange={event =>
            dispatch({
              type: "SELECT_DEPARTMENT",
              department: event.target.value as string
            })
          }
        >
          <MenuItem className={classes.menuItem} value="">
            {" "}
            Choose a subject{" "}
          </MenuItem>
          <MenuItem className={classes.menuItem} value="CSCI-UA">
            {" "}
            Computer Science
          </MenuItem>
          <MenuItem className={classes.menuItem} value="MATH-UA">
            {" "}
            Math{" "}
          </MenuItem>
        </Select>
        {state.department && (
          <div className={classes.button}>
            <Button type="submit" color="primary">
              {" "}
              Create Schedule
            </Button>
          </div>
        )}
      </form>
    </Paper>
  );
};

export default withStyles(styles)(SophomoresForm);
