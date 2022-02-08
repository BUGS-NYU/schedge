import "./App.css";
import "./variables.css";
import React from "react";
import Link from "next/link";
import styled from "styled-components";
import { QueryClient, QueryClientProvider } from "react-query";
import Image from "next/image";

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

const StyledImage = styled(Image)`
  height: 2.8rem;
  width: 0rem;
  margin: 1.2vmin;
  float: right;
  padding: 0.5rem;
  border-radius: 100%;
  background-color: ${(props) => (props.isActive ? "var(--grey300)" : "")};
`;

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

  const handleOnChange = (event) => {
    const code = event.target.value;
    const [sem, currYear] = code.split("-");

    setSemester(sem);
    setYear(parseInt(currYear));
  };

  return (
    <QueryClientProvider client={queryClient}>
      <nav>
        <ul>
          <ul>
            <li id="title">
              <Link href="/">Bobcat Search</Link>
            </li>

            <div>
              Select
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
          </ul>

          <li className="icon">
            {toggle !== "/schedule" ? (
              <Link href={toggle} onClick={() => setToggle("/schedule")}>
                <StyledImage
                  src="/edit-calendar.svg"
                  alt="Edit Calendar"
                  width={2.8}
                  height={1}
                />
              </Link>
            ) : (
              <Link href="/schedule" onClick={() => setToggle(getPath)}>
                <StyledImage
                  src="/edit-calendar.svg"
                  width={2.8}
                  height={1}
                  alt="Edit Calendar"
                />
              </Link>
            )}
          </li>
        </ul>
      </nav>

      <Component year={year} semester={semester} {...pageProps} />
    </QueryClientProvider>
  );
}

export default App;