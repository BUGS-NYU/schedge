import React from "react";
import styles from "./recitation.module.css";
import Attributes from "./Attributes";
import DateSection from "./DateSection";
import {
  convertUnits,
  splitLocation,
  changeStatus,
  styleStatus,
} from "components/util";

function Recitation({
  year,
  semester,
  wishlistCourse,
  recitation,
  sortedRecitationsMeetings,
  courseName,
  lastRecitation,
}) {
  return (
    <div
      className={styles.recitationContainer}
      style={{ borderBottom: !lastRecitation && "1px solid" }}
    >
      {courseName !== recitation.name && (
        <h3 className="sectionName">{recitation.name}</h3>
      )}
      <h4 className="sectionNum">{recitation.code}</h4>
      <Attributes
        instructors={recitation.instructors}
        building={splitLocation(recitation.location).Building}
        units={convertUnits(recitation.minUnits, recitation.maxUnits)}
        status={recitation.status}
        type={recitation.type}
        registrationNumber={recitation.registrationNumber}
      />
      <div className={styles.recitationDescription}>{recitation.notes}</div>

      {sortedRecitationsMeetings && (
        <DateSection sortedSectionMeetings={sortedRecitationsMeetings} />
      )}
      <div className={styles.utilBar}>
        <div classNames={styles.statusContainer}>
          <span
            style={{
              color: styleStatus(recitation.status),
            }}
          >
            {changeStatus(recitation)}
          </span>
        </div>
        <button
          className={styles.wishlistButton}
          onClick={() =>
            wishlistCourse({
              year,
              semester,
              course: recitation,
            })
          }
        >
          <div style={{}} />
          <span style={{}}>Add to Wishlist</span>
        </button>
      </div>
    </div>
  );
}

export default Recitation;
