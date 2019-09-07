import React, { useReducer } from "react";
import { Paper, Typography } from "@material-ui/core";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";
import { RouteComponentProps } from "@reach/router";
import Button from "@material-ui/core/Button";
import withStyles, { WithStyles } from "react-jss";

const styles = {
  FreshmanForm: {
    display: "flex",
    flexDirection: "column",
    padding: "40px"
  },
  form: {
    display: "flex",
    flexDirection: "column"
  },
  select: {
    maxWidth: "200px",
  },
  button: {
    margin: "100px",
    maxWidth: "200px"
  }
};

interface State {
  department: string;
}

function reducer(state: State, action: { type: string; department: string }) {
  switch (action.type) {
    case "SELECT_DEPARTMENT":
      return { ...state, department: action.department };
    default:
      return state;
  }
}

const initialState = {
  department: ""
};

const FreshmenForm: React.FC<
  RouteComponentProps & WithStyles<typeof styles>
> = ({ classes }) => {
  const [state, dispatch] = useReducer(reducer, initialState);
  return (
    <Paper className={classes.FreshmanForm}>
      <div className={classes.form}>
        <Typography> Intro Course</Typography>
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
          <MenuItem value=""> Choose a subject </MenuItem>
          <MenuItem value="CSCI-UA"> Computer Science</MenuItem>
          <MenuItem value="MATH-UA"> Math </MenuItem>
        </Select>
        {state.department && <Button className={classes.button} color="primary"> Create Schedule</Button>}
      </div>
    </Paper>
  );
};

export default withStyles(styles)(FreshmenForm);
