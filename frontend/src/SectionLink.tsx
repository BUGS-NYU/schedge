import React, { ReactNode } from "react";
import withStyles, { WithStyles } from "react-jss";
import { Link } from "@reach/router";
import {PaletteColor} from "@material-ui/core/styles/createPalette";
const styles = {
  SectionLink: {
    width: "300px",
    height: "300px",
    fontSize: "32px",
    color: "black",
    backgroundColor: "#eaeaea",
    display: "inline-block",
    verticalAlign: "middle",
    "-webkit-transform": "perspective(1px) translateZ(0)",
    transform: "perspective(1px) translateZ(0)",
    boxShadow: "0 0 1px rgba(0, 0, 0, 0)",
    position: "relative",
    "-webkit-transition-property": "color",
    transitionProperty: "color",
    "-webkit-transition-duration": "0.3s",
    transitionDuration: "0.3s",
    "&:before": {
      content: '""',
      position: "absolute",
      zIndex: -1,
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      background: (props: Props) => props.color,
      "-webkit-transform": "scaleX(0)",
      transform: "scaleX(0)",
      "-webkit-transform-origin": "0 50%",
      transformOrigin: "0 50%",
      "-webkit-transition-property": "transform",
      transitionProperty: "transform",
      "-webkit-transition-duration": "0.3s",
      transitionDuration: "0.3s",
      "-webkit-transition-timing-function": "ease-out",
      transitionTimingFunction: "ease-out"
    },
    "&:hover, &:focus, &:active": {
      color: "white"
    },
    "&:hover:before, &:focus:before, &:active:before": {
      "-webkit-transform": "scaleX(1)",
      transform: "scaleX(1)"
    }
  },
  text: {
    position: "absolute",
    top: "50%",
    left: "50%",
    marginLeft: "-40px",
    textDecoration: "none"
  }
};

interface Props extends WithStyles<typeof styles> {
  to: string;
  children: ReactNode;
  color: string;
}
const SectionLink: React.FC<Props> = ({ classes, to, children }) => {
  return (
    <Link to={to}>
      <div className={classes.SectionLink}>
        <div className={classes.text}> {children}</div>
      </div>
    </Link>
  );
};

export default withStyles(styles)(SectionLink);
