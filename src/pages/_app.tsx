import "./App.css";
import React from "react";
import { usePageState } from "components/state";
import { QueryClient, QueryClientProvider } from "react-query";
import { QueryNumberSchema, useQueryParam } from "../components/useQueryParam";
import { SemesterSchema } from "../components/types";

const queryClient = new QueryClient();

function App({ Component, pageProps }) {
  const update = usePageState((s) => s.update);

  const [year] = useQueryParam("year", QueryNumberSchema);
  const [semester] = useQueryParam("semester", SemesterSchema);

  React.useEffect(() => {
    if (year) {
      update({ year });
    }

    if (semester) {
      update({ semester });
    }
  }, [update, year, semester]);

  return (
    <QueryClientProvider client={queryClient}>
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          height: "100%",
          width: "100%",
        }}
      >
        <Component {...pageProps} />
      </div>
    </QueryClientProvider>
  );
}

export default App;
