import React, { useState } from "react";
import Link from 'next/link';
import SearchPage from "./_search";


function Home({
  year,
  semester
}) {

  return (
    <div>
      <div className="App">
        <Link href="/">
            <SearchPage year={year} semester={semester} />
        </Link>
      </div>
    </div>
  );
}

export default Home;
