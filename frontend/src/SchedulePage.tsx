import React from "react";
import { Paper } from "@material-ui/core";
import { ViewState } from "@devexpress/dx-react-scheduler";
import {
  Scheduler,
  WeekView,
  Appointments
} from "@devexpress/dx-react-scheduler-material-ui";
import { RouteComponentProps } from "@reach/router";
import { Meeting } from "./types";
import withStyles, { WithStyles } from "react-jss";

const styles = {
  SchedulePage: {
    //height: "10vh"
  }
};

interface Props extends RouteComponentProps, WithStyles<typeof styles> {
  schedule: Array<Meeting>;
}

const SchedulePage: React.FC<Props> = ({ classes, schedule }) => {
  return (
    <Paper className={classes.SchedulePage}>
      <Scheduler height={650} data={schedule}>
        <ViewState currentDate="2019-09-09" />
        <WeekView startDayHour={8} endDayHour={18} />
        <Appointments />
      </Scheduler>
    </Paper>
  );
};

export default withStyles(styles)(SchedulePage);
