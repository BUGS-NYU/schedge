import React, { Reducer, useEffect, useReducer } from "react";
import {
  Checkbox,
  createStyles,
  Paper,
  Theme,
  Typography
} from "@material-ui/core";
import { navigate, RouteComponentProps } from "@reach/router";
import withStyles, { WithStyles } from "react-jss";
import { API_URL } from "./constants";
import axios from "axios";
import { APICourse } from "./types";
import Grid from "@material-ui/core/Grid";
import CircularProgress from "@material-ui/core/CircularProgress";
import makeStyles from "@material-ui/core/styles/makeStyles";
import Button from "@material-ui/core/Button";

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
  courses: Array<APICourse> | undefined;
  isLoading: boolean;
  error: string | undefined;
  checkboxes: Set<number>;
  recommendedCourses: Array<APICourse> | undefined;
}

type ActionTypes =
  | { type: "SELECT_DEPARTMENT"; department: string }
  | { type: "SUBMIT_FORM_FAILED"; error: string }
  | { type: "ADD_COURSES"; courses: APICourse[] }
  | { type: "UPDATE_CHECKBOXES"; checked: boolean; id: number }
  | { type: "SUBMIT_FORM_SUCCEEDED"; recommendedCourses: APICourse[] };

function reducer(state: State, action: ActionTypes): State {
  switch (action.type) {
    case "ADD_COURSES":
      return { ...state, courses: action.courses, isLoading: false };
    case "UPDATE_CHECKBOXES":
      if (action.checked) {
        state.checkboxes.add(action.id);
      } else {
        state.checkboxes.delete(action.id);
      }
      return { ...state, checkboxes: state.checkboxes };
    case "SUBMIT_FORM_FAILED":
      return { ...state, error: action.error };
    case "SUBMIT_FORM_SUCCEEDED":
      return { ...state, recommendedCourses: action.recommendedCourses };
    default:
      return state;
  }
}

const initialState = {
  isLoading: true,
  courses: undefined,
  error: undefined,
  checkboxes: new Set() as Set<number>,
  recommendedCourses: undefined
};

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    progress: {
      margin: theme.spacing(2)
    }
  })
);

interface Props extends RouteComponentProps, WithStyles<typeof styles> {}

const SophomoresForm: React.FC<Props> = ({ classes }) => {
  const [state, dispatch] = useReducer(reducer, initialState);
  const muiClasses = useStyles();
  useEffect(() => {
    async function f() {
      for (let i = 0; i < 3; i++) {
        let res;
        try {
          res = await axios.get(`${API_URL}/courses`);
          dispatch({ type: "ADD_COURSES", courses: res.data });
          break;
        } catch {}
      }
    }
    f();
  }, []);
  console.log(state);
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
          axios
            .post(
              `${API_URL}/available-by-completed`,
              Array.from(state.checkboxes).map(c => c + 1)
            )
            .then(res => {
              dispatch({
                type: "SUBMIT_FORM_SUCCEEDED",
                recommendedCourses: res.data
              });
            })
            .catch(err => console.log(err));
        }}
      >
        <Typography variant="h3" component="h3">
          Pick the courses you've taken already
        </Typography>
        {state.courses &&
          state.courses.map(course => (
            <Typography>
              <Checkbox
                value={state.checkboxes.has(course.id)}
                onChange={event =>
                  dispatch({
                    type: "UPDATE_CHECKBOXES",
                    checked: event.target.checked,
                    id: course.id
                  })
                }
              />
              {course.name}
            </Typography>
          ))}
        <div className={classes.button}>
          <Button type="submit" color="primary">
            {" "}
            Create Schedule
          </Button>
        </div>
        {state.recommendedCourses && (
          <Typography>
            {" "}
            We can recommend the following courses:
            <ul>
              {state.recommendedCourses.map(course => (
                <li> {course.name} </li>
              ))}
            </ul>
          </Typography>
        )}
      </form>
    </Paper>
  );
};

export default withStyles(styles)(SophomoresForm);
