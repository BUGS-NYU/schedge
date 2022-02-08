import React from "react";

export function makeStableHook(useValue) {
  return (...args) => {
    const result = useValue(...args);
    return useStable(result);
  };
}

export function useStable(o) {
  return React.useMemo(() => o, Object.values(o)); // eslint-disable-line
}

export function useCounter(n) {
  const [counter, setCounter] = React.useState(n);
  const increment = React.useCallback(() => setCounter((c) => ++c), [
    setCounter,
  ]);

  return [counter, increment];
}
