import React from "react";
import { useStable } from "components/hooks";

const isDev = process.env.NODE_ENV !== "production";
const NO_PROVIDER = {};

// Required<Value> is used here to prevent the number of fields detected at runtime
// from changing.
export function createContext(useValue) {
  const hookName = `Context(${useValue.name ? useValue.name : "??"})`;

  const fieldMap = {};
  const baseCtx = React.createContext(NO_PROVIDER);
  baseCtx.displayName = hookName;

  const getFieldInfo = (key) => {
    const field = fieldMap[key];
    if (field) return field;

    const ctx = React.createContext(undefined);
    if (isDev) ctx.displayName = `${hookName}.context("${key}")`;
    const useCtx = () => React.useContext(ctx);

    return (fieldMap[key] = { ctx, hook: useCtx });
  };

  let propCount = null;
  const Provider = ({ children, ...props }) => {
    const hookValue = useValue(props);

    const valueEntries = Object.entries(hookValue);
    const element = valueEntries.reduce((agg, [key, value]) => {
      const Provider = getFieldInfo(key).ctx.Provider;

      return <Provider value={value}>{agg}</Provider>;
    }, <baseCtx.Provider value={null}>{children}</baseCtx.Provider>);

    let count = valueEntries.length;
    if (propCount === count) return element;

    if (propCount === null) {
      propCount = count;
      return element;
    }

    const msg = `prop count changed for ${hookName}, which will result in runtime errors`;
    throw new Error(msg);
  };

  const useProps = function (...props) {
    const base = React.useContext(baseCtx);
    if (base === NO_PROVIDER) {
      throw new Error(
        `The consumer of ${hookName} must be wrapped with its Provider`
      );
    }

    const propKeys = props.length === 0 ? Object.keys(fieldMap) : props;
    const partialValue = {};
    propKeys.forEach((key) => (partialValue[key] = getFieldInfo(key).hook()));
    const output = useStable(partialValue);

    return output;
  };

  if (isDev) {
    Provider.displayName = `${hookName}.Provider`;
    Object.defineProperty(useProps, "name", {
      value: `${hookName}.useProps`,
      writable: false,
    });
  }

  return [Provider, useProps];
}
