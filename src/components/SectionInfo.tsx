import React, { useState } from "react";
import fonts from "./css/fonts.module.css";
import styles from "./css/section.module.css";
import cx from "classnames";
import { DateTime } from "luxon";
import { AugmentedSection, usePageState } from "./state";
import { useQuery } from "react-query";
import { z } from "zod";
import axios from "axios";
import { Section } from "./types";

interface DateProps {
  section: AugmentedSection;
}

const fmtTime = (dt: DateTime): string => {
  const local = DateTime.local();
  const formatted = dt.toLocaleString(DateTime.TIME_SIMPLE);

  if (local.offset === dt.offset) {
    return formatted;
  }

  const convertedTime = dt
    .setZone(local.zone)
    .toLocaleString(DateTime.TIME_SIMPLE);

  return `${formatted} ${dt.offsetNameShort} (${convertedTime} ${local.offsetNameShort})`;
};

const CampusSchema = z.object({
  name: z.string(),
  timezoneId: z.string(),
  timezoneName: z.string(),
});

const CampusesSchema = z.object({
  campuses: z.record(z.string(), CampusSchema),
});

const DateSection: React.VFC<DateProps> = ({ section }) => {
  const term = usePageState((s) => s.term);
  const { data: { campuses } = {} } = useQuery(
    ["campuses", term.code],
    async () => {
      const resp = await axios.get("/api/campus");
      console.log(resp.data);
      const parsed = CampusesSchema.parse(resp.data);
      return parsed;
    }
  );

  const timezone = campuses?.[section.campus]?.timezoneId;
  const meetings = React.useMemo(() => {
    const sortedSectionMeetings = [...(section.meetings ?? [])].sort(
      (a, b) => a.beginDate.getDay() - b.beginDate.getDay()
    );
    const parsedMeetings = sortedSectionMeetings.map((meeting) => {
      let startTime = DateTime.fromISO(meeting.beginDateLocal, {
        setZone: true,
      });
      if (timezone) {
        startTime = startTime.setZone(timezone);
      }

      return {
        startTime,
        minutesDuration: meeting.minutesDuration,
        endTime: startTime.plus({ minutes: meeting.minutesDuration }),
      };
    });
    return parsedMeetings;
  }, [section.meetings, timezone]);

  return (
    <div>
      {meetings.map((meeting, i) => {
        const { startTime, endTime } = meeting;

        const timeStr = `${fmtTime(startTime)} - ${fmtTime(endTime)}`;
        const text = `${startTime.weekdayLong}, ${timeStr}`;

        return (
          <div key={i} className={fonts.body2} style={{ fontWeight: "bold" }}>
            {text}
          </div>
        );
      })}
    </div>
  );
};

interface Props {
  section: AugmentedSection;
  ignoreNotes: boolean;
  lastSection: boolean;
}

const SectionAttribute: React.FC<{ label: string }> = ({ label, children }) => {
  return (
    <div>
      <h5>{label}</h5>
      <div className={fonts.body1}>{children}</div>
    </div>
  );
};

function sectionStatusText(section: Section): string {
  if (section.status === "WaitList") {
    return `Waitlist (${section.waitlistTotal})`;
  } else {
    return section.status;
  }
}

export const SectionInfo: React.VFC<Props> = ({
  section,
  ignoreNotes,
  lastSection,
}) => {
  const [expanded, setExpanded] = useState(false);
  const cb = usePageState((s) => s.cb);

  return (
    <div
      className={cx(
        styles.sectionContainer,
        !lastSection && styles.sectionBorder
      )}
    >
      <h4 className={fonts.heading2}>
        {section.type} {section.code}
        {!!section.sectionName && " - " + section.name}
      </h4>

      <div className={styles.attributeContainer}>
        <SectionAttribute label="Registration Number">
          #{section.registrationNumber}
        </SectionAttribute>

        <SectionAttribute label="Status">
          {sectionStatusText(section)}
        </SectionAttribute>

        <SectionAttribute label="Location">
          {section.location ?? "TBA"}
        </SectionAttribute>

        <SectionAttribute label="Campus">
          {section.campus.replace("(Global)", "")}
        </SectionAttribute>

        <SectionAttribute label="Credits">
          {section.minUnits === section.maxUnits
            ? `${section.minUnits}`
            : `${section.minUnits} - ${section.maxUnits}`}
        </SectionAttribute>
      </div>

      <SectionAttribute label="Instructors">
        {section.instructors.map((instructor) => (
          <span key={instructor}>{instructor}</span>
        ))}
      </SectionAttribute>

      {section.notes && !ignoreNotes && (
        <div className={styles.sectionDescription}>{section.notes}</div>
      )}

      {section.meetings && <DateSection section={section} />}

      <div className={styles.utilBar}>
        {!!section.recitations?.length && (
          <button
            className={styles.expandButton}
            onClick={(e) => setExpanded((prev) => !prev)}
          >
            {expanded ? "Hide" : "Show"} Recitations
          </button>
        )}

        <button
          className={styles.wishlistButton}
          onClick={() => cb.addToWishlist(section)}
        >
          Add to Wishlist
        </button>
      </div>

      <div className={styles.recitationBox}>
        {!!section.recitations?.length &&
          expanded &&
          section.recitations.map((recitation, i) => {
            return (
              <SectionInfo
                key={i}
                ignoreNotes={false}
                lastSection={i === section.recitations.length - 1}
                section={{
                  ...recitation,
                  name: recitation.name ?? section.name,
                  sectionName: recitation.name,
                  deptCourseId: section.deptCourseId,
                  subjectCode: section.subjectCode,
                }}
              />
            );
          })}
      </div>
    </div>
  );
};
