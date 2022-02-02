import React from "react";
import SearchPage from "./_search";


function Home({
  year,
  semester
}) {

  return (
    <div>
      <div className="App">
            <SearchPage year={year} semester={semester} />
      </div>
    </div>
  );
}

export default Home;
