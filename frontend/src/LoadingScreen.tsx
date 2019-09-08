import React, { useEffect, useState } from "react";
import { createStyles, Paper, Theme } from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import CircularProgress from "@material-ui/core/CircularProgress";
import makeStyles from "@material-ui/core/styles/makeStyles";
import { navigate, RouteComponentProps } from "@reach/router";
import Grid from "@material-ui/core/Grid";

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    progress: {
      margin: theme.spacing(2)
    }
  })
);

interface Progress {
  percent: number;
  iteration: number;
}

const getLoadingMessage = (iteration: number) => {
  if (iteration === 0) {
    return "Finding you a Writing The Essay course";
  } else if (iteration === 1) {
    return "Searching out a Core course";
  }
  return "Getting a major course just for you";
};

const LoadingScreen: React.FC<RouteComponentProps> = () => {
  const classes = useStyles();
  const [progress, setProgress] = useState({ percent: 0, iteration: 0 });
  useEffect(() => {
    function tick() {
      if (progress.iteration === 3) {
        navigate(`/schedule`);
      }
      setProgress(oldProgress =>
        oldProgress.percent >= 100
          ? { percent: 0, iteration: oldProgress.iteration + 1 }
          : {
              percent: oldProgress.percent + 1,
              iteration: oldProgress.iteration
            }
      );
    }
    const timer = setInterval(tick, 20);
    return () => {
      clearInterval(timer);
    };
  });
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
          {" "}
          {getLoadingMessage(progress.iteration)}
        </Typography>
        <CircularProgress
          className={classes.progress}
          variant="determinate"
          value={progress.percent}
        />
      </Grid>
    </Paper>
  );
};

export default LoadingScreen;
