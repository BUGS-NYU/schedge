import "./App.css";
import React from "react";
import headerStyles from "./header.module.css";
import css from "components/util.module.css";
import Link from "next/link";
import { QueryClient, QueryClientProvider } from "react-query";
import cx from "classnames";
import Image from "next/image";
import Head from "next/head";

/*
const BootstrapInput = styled.div`
  border-radius: 4px;
  border: 1px solid #9e9e9e;
  font-size: 1rem;
  padding: 10px 26px 10px 12px;
  font-weight: bold;
  color: var(--grey800);
  font-family: var(--primaryFont);
  transition: border-color 300ms, box-shadow 300ms;

  &:focus {
    border-radius: 4px;
  }
`;

const StyledImage = styled.img`
  height: 2.8rem;
  width: 2.8rem;
  margin: 1.2vmin;
  float: right;
  padding: 0.5rem;
  border-radius: 100%;
`;
*/

const queryClient = new QueryClient();

function App({ Component, pageProps }) {
  //const initialState = loadState();
  //const store = createStore(initialState);
  //store.subscribe(() => saveState(store.getState()));

  // window.location.pathname + window.location.search;
  const getPath = () => "";

  const options = [
    {
      name: "January 2021",
      code: "ja-2021",
    },
    {
      name: "Spring 2021",
      code: "sp-2021",
    },
    {
      name: "Summer 2021",
      code: "su-2021",
    },
    {
      name: "Fall 2021",
      code: "fa-2021",
    },
  ];

  /* eslint-disable no-unused-vars */
  const [year, setYear] = React.useState(2021);
  const [semester, setSemester] = React.useState("sp");
  /* eslint-enable no-unused-vars */

  // if we start on schedule page, the first toggle brings us to home
  // otherwise, the first toggle brings us to schedule page
  const [toggle, setToggle] = React.useState(
    getPath() === "/schedule" ? "/" : "/schedule"
  );

  const [showSchedule, setShowSchedule] = React.useState(false);

  const handleOnChange = (event) => {
    const code = event.target.value;
    const [sem, currYear] = code.split("-");

    setSemester(sem);
    setYear(parseInt(currYear));
  };

  return (
    <QueryClientProvider client={queryClient}>
      <div className={headerStyles.headerBox}>
        <div className={headerStyles.titleBox}>
          <Link href="/">
            <a className={headerStyles.title}>Bobcat Search</a>
          </Link>

          <div>Select</div>
        </div>

        {/*
            <Select
              displayEmpty
              onChange={handleOnChange}
              defaultValue={`${semester}-${year}`}
              value={`${semester}-${year}`}
              input={<BootstrapInput />}
            >
              {options.map((item) => {
                return (
                  <MenuItem key={item.name} value={item.code}>
                    {item.name}
                  </MenuItem>
                );
              })}
            </Select>
            */}

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

      <Component year={year} semester={semester} {...pageProps} />
    </QueryClientProvider>
  );
}

export default App;