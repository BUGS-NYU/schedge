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

function useAsyncHelper(fn, _deps) {
  const [fetches, setFetches] = React.useState(0);

  const [active, setActive] = React.useState(0);
  const startedRef = React.useRef(0);
  const doneRef = React.useRef(0);

  const [data, setData] = React.useState(null);
  const [error, setError] = React.useState(null);

  const refetch = React.useCallback(() => setFetches((s) => ++s), [setFetches]);
  const deps = [fetches].concat(_deps ?? []);

  // Intentionally removing most stuff from the dependencies. This effect should
  // only trigger when the dependencies change or when the refetch is called.
  /* eslint-disable */
  React.useEffect(() => {
    const started = startedRef.current++;
    if (_deps === null && started === 0) return;

    let mounted = true;

    const doCall = async () => {
      let newValue = null;
      let newError = null;

      setActive((a) => a + 1);

      await fn()
        .then((v) => (newValue = v))
        .catch((e) => (newError = e));

      setActive((a) => a - 1);

      if (mounted && doneRef.current <= started) {
        doneRef.current = started + 1;
        setData(newValue);
        setError(newError);
      }
    };

    doCall();

    return () => {
      mounted = false;
    };
  }, deps);
  /* eslint-enable */

  if (error === null && doneRef.current > 0) {
    return {
      refetch,
      isLoaded: true,
      isLoading: active > 0,
      data,
      error: null,
    };
  }

  return {
    refetch,
    isLoaded: false,
    isLoading: active > 0,
    data: null,
    error: error,
  };
}

export function useAsyncLazy(fn) {
  const result = useAsyncHelper(fn, null);
  const stableResult = useStable(result);

  return stableResult;
}

export function useAsync(fn, deps) {
  const result = useAsyncHelper(fn, deps);
  const stableResult = useStable(result);

  return stableResult;
}
