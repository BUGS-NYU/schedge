import React from "react";
import { Grid, useTheme } from "@material-ui/core";
import SectionLink from "./SectionLink";
import { RouteComponentProps } from "@reach/router";

const HomePage: React.FC<RouteComponentProps> = () => {
  const theme = useTheme();
  return (
    <Grid item xs={12}>
      <Grid container justify="center" spacing={5}>
        {[
          {
            name: "Freshmen",
            url: "/freshmen",
            direction: "left",
            color: theme.palette.primary.main
          },
          {
            name: "Sophomores",
            url: "/sophomores",
            direction: "right",
            color: theme.palette.secondary.main
          }
        ].map(({ name, url, direction, color }) => (
          <Grid key={name} item>
            <SectionLink direction={direction} color={color} to={url}>
              {name}
            </SectionLink>
          </Grid>
        ))}
      </Grid>
    </Grid>
  );
};

export default HomePage;
