import React from "react";
import styles from "./css/review-table-headers.module.css";

export default function ReviewTableHeaders({
  name,
  totalRatings,
  overallRating,
}) {
  return (
    <div className={styles.metaContainer}>
      <div className={styles.instructorName}>{name}</div>
      <div>{`${totalRatings} review${totalRatings >= 1 ? "s" : ""}`}</div>
      <div>
        {overallRating > -1 ? `Overall ${overallRating}` : "No Overall Rating"}
      </div>
    </div>
  );
}
