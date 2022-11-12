import React from "react";
import styles from "./review-table.module.css";

const ExpandMoreIcon = ({ style }) => {
  return (
    <svg
      focusable="false"
      aria-hidden="true"
      viewBox="0 0 24 24"
      data-testid="ExpandMoreIcon"
    >
      <path
        d="M16.59 8.59 12 13.17 7.41 8.59 6 10l6 6 6-6z"
        style={style}
      ></path>
    </svg>
  );
};

export default function ReviewTable({ ratings, remaining, page, setPage }) {
  return (
    <React.Fragment>
      <table className={styles.ratingTable}>
        <thead style={{ width: "100%" }}>
          <tr>
            <th className={styles.header}>Information</th>
            <th
              className={styles.header}
              style={{ borderLeft: "1.2px solid var(--grey600)" }}
            >
              Comment
            </th>
          </tr>
        </thead>
        <tbody className={styles.tableBody}>
          {ratings &&
            ratings.length > 0 &&
            ratings.map((rating, idx) => {
              const date = new Date(rating.rTimestamp);
              return (
                <tr
                  className={styles.ratingContainer}
                  key={rating.id}
                  style={{
                    backgroundColor:
                      idx % 2 === 0 ? "var(--grey400)" : "var(--grey300)",
                  }}
                >
                  <td className={styles.infoContainer}>
                    <p className={styles.rating}>{rating.rClass}</p>
                    <p
                      className={styles.rating}
                    >{`Overall: ${rating.rOverall}`}</p>
                    <p
                      className={styles.rating}
                    >{`Helpful: ${rating.rHelpful}`}</p>
                    <p className={styles.rating}>{`${
                      date.getMonth() + 1
                    }/${date.getDate()}/${date.getFullYear()}`}</p>
                  </td>
                  <td
                    className={styles.comment}
                    dangerouslySetInnerHTML={{
                      __html: rating.rComments,
                    }}
                  />
                </tr>
              );
            })}
        </tbody>
      </table>

      <div style={{ backgroundColor: "var(--grey100)" }}>
        {!(page === 1) && (
          <button
            className={styles.expandButton}
            style={{ float: "left" }}
            onClick={() => {
              setPage((old) => Math.max(old - 1, 1));
            }}
          >
            <ExpandMoreIcon
              style={{
                transform: "rotate(90deg)",
              }}
            />
            <span>Prev</span>
          </button>
        )}
        {remaining > 0 && (
          <button
            className={styles.expandButton}
            style={{ float: "right" }}
            onClick={() => {
              console.log("expand");
              // if (!isPreviousData && remaining > 0) {
              //   setPage((old) => old + 1);
              // }
            }}
          >
            <span>Next</span>
            <ExpandMoreIcon
              style={{
                transform: "rotate(-90deg)",
              }}
            />
          </button>
        )}
      </div>
    </React.Fragment>
  );
}
