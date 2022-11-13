import { useRouter, NextRouter } from "next/router";
import React from "react";
import { z, ZodTypeDef } from "zod";

export const QueryNumberSchema = z
  .preprocess((obj): number | undefined => {
    if (typeof obj === "string") {
      const parsed = Number.parseInt(obj, 10);

      // Number.parseInt returns NaN when it fails to parse.
      // This *should* never happen, but designers have started editing
      // the URL parameters manually, so we need to check this value
      // somewhat carefully to avoid propagating a NaN.
      if (Number.isNaN(parsed)) {
        return undefined;
      }

      return parsed;
    }

    if (typeof obj === "number") {
      return obj;
    }

    return undefined;
  }, z.number())
  .transform((value) => Math.max(1, value));

function unwrapValue<T>(
  schema: z.Schema<T, ZodTypeDef, unknown>,
  value?: string
): T | undefined {
  if (value) {
    const result = schema.safeParse(value);
    if (result.success) {
      return result.data;
    }
  }
  return undefined;
}

function getFirstParamValue<T>(
  router: NextRouter,
  schema: z.Schema<T, ZodTypeDef, unknown>,
  name: string
): T | undefined {
  const value = router.query[name];
  if (Array.isArray(value)) {
    return unwrapValue(schema, value[0]);
  }

  return unwrapValue(schema, value);
}

// Field types available for ParsedUrlQueryInput in @node/querystring
type QueryInputField =
  | string
  | number
  | boolean
  | ReadonlyArray<string>
  | ReadonlyArray<number>
  | ReadonlyArray<boolean>
  | null;

export function useQueryParam<T extends QueryInputField>(
  name: string,
  schema: z.Schema<T, ZodTypeDef, unknown>,
  { defaultValue, pathname }: { defaultValue?: T; pathname?: string } = {}
): [T | undefined, (value: T | undefined) => void] {
  const router = useRouter();
  const paramValue = React.useMemo<T | undefined>(
    () => getFirstParamValue(router, schema, name),
    [router, schema, name]
  );
  const setParamValue = React.useCallback(
    (value: T | undefined) => {
      router.push({
        pathname: pathname || router.pathname,
        query: {
          ...router.query,
          [name]: value ?? "",
        },
      });
    },
    [name, pathname, router]
  );
  React.useEffect(() => {
    if (router.isReady && !paramValue && defaultValue) {
      setParamValue(defaultValue);
    }
  }, [defaultValue, paramValue, router.isReady, setParamValue]);
  return [paramValue || defaultValue, setParamValue];
}
