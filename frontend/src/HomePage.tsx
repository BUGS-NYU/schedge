import React from "react"
import {Grid, useTheme} from "@material-ui/core";
import SectionLink from "./SectionLink";
import {RouteComponentProps} from "@reach/router";

const HomePage: React.FC<RouteComponentProps> = () => {
  const theme = useTheme();
  return(<Grid item xs={12}>
    <Grid container justify="center" spacing={5}>
      {[
        { name: "Freshmen", url: "/freshmen", color: theme.palette.primary.main },
        { name: "Sophomores", url: "/sophomores", color: theme.palette.secondary.main },
        { name: "Juniors", url: "/juniors", color: theme.palette.primary.light }
      ].map(({ name, url, color }) => (
        <Grid key={name} item>
          <SectionLink color={color} to={url}>{name}</SectionLink>
        </Grid>
      ))}
    </Grid>
  </Grid>)
}

export default HomePage;