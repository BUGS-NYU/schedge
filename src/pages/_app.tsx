import "./App.css";
import React from "react";
import headerStyles from "./header.module.css";
import Link from "next/link";
import { SemesterSchema, usePageState } from "components/state";
import { useRouter } from "next/router";
import { QueryClient, QueryClientProvider } from "react-query";
import { QueryNumberSchema, useQueryParam } from "../components/useQueryParam";

const queryClient = new QueryClient();

function App({ Component, pageProps }) {
  //const initialState = loadState();
  //const store = createStore(initialState);
  //store.subscribe(() => saveState(store.getState()));

  // window.location.pathname + window.location.search;
  const getPath = () => "";

  /* eslint-disable no-unused-vars */
  const update = usePageState((s) => s.update);
  const router = useRouter();
  const [year] = useQueryParam("year", QueryNumberSchema);
  const [semester] = useQueryParam("semester", SemesterSchema);

  React.useEffect(() => {
    if (!router.isReady) return;

    if (router.query.year) {
      update({ year });
    }

    if (router.query.semester) {
      update({ semester });
    }
  }, [router, update]);
  /* eslint-enable no-unused-vars */

  // if we start on schedule page, the first toggle brings us to home
  // otherwise, the first toggle brings us to schedule page
  const [showSchedule, setShowSchedule] = React.useState(
    getPath() === "/schedule"
  );

  // const handleOnChange = (event) => {
  //   const code = event.target.value;
  //   const sem = code.substring(0, 2);
  //   const currYear = code.substring(2);

  //   setSemester(sem);
  //   setYear(parseInt(currYear));
  // };

  // Copy from this for now; we can decide on MUI or whatever later, but it's
  // basically just used for this one component.
  //
  // https://github.com/A1Liu/a1liu/blob/94d576634459ce9b954307b4fc7fad37c624c5bb/pages/dev/card-cutter.tsx#L78
  return (
    <QueryClientProvider client={queryClient}>
      <div className={headerStyles.headerBox}>
        <div className={headerStyles.titleBox}>
          <Link href="/">
            <a className={headerStyles.title}>Bobcat Search</a>
          </Link>

          <div>
            <div className={headerStyles.selectBox}>
              <div>Spring 2021</div>

              <svg
                className={headerStyles.selectArrow}
                focusable="false"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path d="M7 10l5 5 5-5z"></path>
              </svg>
            </div>

            <div></div>
          </div>
        </div>

        <button
          className={headerStyles.scheduleButton}
          onClick={() => setShowSchedule(!showSchedule)}
        >
          <img
            className={headerStyles.scheduleIcon}
            src="/edit-calendar.svg"
            alt="Edit Calendar"
          />
        </button>
      </div>

      <Component {...pageProps} />
    </QueryClientProvider>
  );
}

export default App;
