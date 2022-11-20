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
import { Term, usePageState } from "./state";
import { useSchedule } from "../pages/schedule";
import { Recitation } from "../pages/subject";

interface Props {
  recitation: Recitation;
  sortedRecitationsMeetings: any;
  courseName: string;
  lastRecitation: boolean;
}

export const RecitationInfo: React.VFC<Props> = ({
  recitation,
  sortedRecitationsMeetings,
  courseName,
  lastRecitation,
}) => {
  const { addToWishlist } = useSchedule();

  return (
    <div
      className={styles.recitationContainer}
      style={{ borderBottom: !lastRecitation && "1px solid" }}
    >
      {recitation.name && <h3 className="sectionName">{recitation.name}</h3>}
      <h4 className="sectionNum">{recitation.code}</h4>
      <Attributes
        instructors={recitation.instructors}
        building={splitLocation(recitation.location).Building}
        units={convertUnits(recitation.minUnits, recitation.maxUnits)}
        status={recitation.status}
        type={recitation.type}
        registrationNumber={recitation.registrationNumber}
        room={undefined}
      />
      <div className={styles.recitationDescription}>{recitation.notes}</div>

      {sortedRecitationsMeetings && (
        <DateSection sortedSectionMeetings={sortedRecitationsMeetings} />
      )}
      <div className={styles.utilBar}>
        <div className={styles.statusContainer}>
          <span style={{ color: styleStatus(recitation.status) }}>
            {changeStatus(recitation)}
          </span>
        </div>
        <button
          className={styles.wishlistButton}
          onClick={() => addToWishlist(recitation)}
        >
          <div style={{}} />
          <span style={{}}>Add to Wishlist</span>
        </button>
      </div>
    </div>
  );
};
