import React from "react";
import styles from "./Attributes.module.css";
import Instructor from "./Instructor";

export default function Attributes({
  instructors,
  building,
  room,
  units,
  status, // eslint-disable-line no-unused-vars
  type,
  registrationNumber,
}) {
  return (
    <div className="attributes">
      <div className={styles.attributeContainer}>
        <div className={styles.attributeLabel}>
          Instructor{instructors.length > 1 ? "s" : ""}
        </div>
        {instructors.map((instructor) => {
          return <Instructor key={instructor} instructor={instructor} />;
        })}
      </div>
      {building && (
        <div className={styles.attributeContainer}>
          <div className={styles.attributeLabel}>Building</div>
          {building}
        </div>
      )}
      {room && (
        <div className={styles.attributeContainer}>
          <div className={styles.attributeLabel}>Room</div>
          {room}
        </div>
      )}
      <div className={styles.attributeContainer}>
        <div className={styles.attributeLabel}>Units</div>
        {units}
      </div>
      <div className={styles.attributeContainer}>
        <div className={styles.attributeLabel}>Type</div>
        {type}
      </div>
      <div className={styles.attributeContainer}>
        <div className={styles.attributeLabel}>Registration #</div>
        {registrationNumber}
      </div>
    </div>
  );
}
