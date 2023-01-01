import "./App.css";
import React from "react";
import { usePageState } from "components/state";
import { QueryClient, QueryClientProvider } from "react-query";
import { useQueryParam } from "components/useQueryParam";
import { NumberStringSchema, SemesterSchema } from "components/types";

const queryClient = new QueryClient();

function App({ Component, pageProps }) {
  const update = usePageState((s) => s.cb.updateTerm);

  const [year] = useQueryParam("year", NumberStringSchema);
  const [semester] = useQueryParam("semester", SemesterSchema);

  React.useEffect(() => {
    if (year && semester) {
      update({ year, sem: semester, code: `${semester}${year}` });
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
