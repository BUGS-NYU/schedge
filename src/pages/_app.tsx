import "./App.css";
import React from "react";
import { SemesterSchema, usePageState } from "components/state";
import { QueryClient, QueryClientProvider } from "react-query";
import { QueryNumberSchema, useQueryParam } from "../components/useQueryParam";
import { MainLayout } from "../components/Layout";

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
      <Component {...pageProps} />
    </QueryClientProvider>
  );
}

export default App;
