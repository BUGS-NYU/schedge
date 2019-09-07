use super::chrono::{Day, Time};

#[derive(Clone, Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct Meeting {
    /// Course registration number. Uniquely identifies this meeting.
    pub crn: u32,
    /// The days this meeting happens.
    pub days: (Day, Day),
    /// The start time of this meeting.
    pub start_time: Time,
    /// The end time of this meeting.
    pub end_time: Time,
    /// The professor
    pub professor: String,
    // /// The course id
    // pub course_id: u32,
}
