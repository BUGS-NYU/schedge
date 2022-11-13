import React from "react";
import Link from "next/link";
import styles from "./school.module.css";
import { findSchool } from "components/util";

export default function School({ schoolCode, schoolName, year, semester }) {
  return (
    <div className={styles.schoolContainer}>
      <Link
        className="schoolLink"
        href={{
          pathname: "/school",
          query: `school=${schoolCode}&year=${year}&semester=${semester}`,
        }}
        style={{ textDecoration: "none" }}
      >
        <div className="schoolTitle">
          <span className="schoolCode">{schoolCode}</span>
          <span className="schoolName">
            {schoolName !== "" ? schoolName : findSchool(schoolCode)}
          </span>
        </div>
      </Link>
    </div>
  );
}

