import React from "react";
import cx from "classnames";
import css from "./util.module.css";

export const timeout = (ms) => new Promise((res) => setTimeout(res, ms));

export async function post(url, data) {
  const resp = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  return resp.json();
}

export async function get(urlString, query) {
  const queryString = new URLSearchParams(query).toString();
  if (queryString) {
    urlString += "?" + queryString;
  }

  const resp = await fetch(urlString);

  return resp.json();
}

const backgroundColor = (bg) => {
  switch (bg) {
    case "lightGray":
      return css.bgLightGray;
    case "white":
      return css.bgWhite;
    default:
      return undefined;
  }
};

export const Scroll = ({ children, ...props }) => {
  const style = { height: props.height };
  const outerClass = cx(css.rounded, backgroundColor(props.background));
  const innerClass = cx(css.scrollColImpl, props.flexBox ? css.col : null);

  let inner;
  switch (props.tag) {
    case "pre":
      inner = <pre className={innerClass}>{children}</pre>;
      break;

    case "div":
    default:
      inner = <div className={innerClass}>{children}</div>;
      break;
  }

  return (
    <div className={outerClass} style={style}>
      {inner}
    </div>
  );
};

export const Btn = ({ children, ...props }) => {
  const className = cx(css.muiButton, backgroundColor(props.background));

  const clickHandler = (evt) => {
    if (!props.propagate) evt.stopPropagation();
    if (!props.preventDefault) evt.preventDefault();

    props.onClick?.();
  };

  return (
    <button className={className} onClick={clickHandler}>
      {children}
    </button>
  );
};
