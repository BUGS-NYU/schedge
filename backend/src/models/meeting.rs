use super::chrono::{Day, Time};

#[derive(Clone, Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct MeetingOutput {
    /// Course name
    pub course_name: &'static str,
    /// The days this meeting happens.
    pub days: (Day, Day),
    /// The start time of this meeting.
    pub start_time: Time,
    /// The end time of this meeting.
    pub end_time: Time,
    /// The professor
    pub professor: &'static str,
    /// Location
    pub location: &'static str,
}

#[derive(Clone, Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct Meeting {
    /// The days this meeting happens.
    pub days: (Day, Day),
    /// The start time of this meeting.
    pub start_time: Time,
    /// The end time of this meeting.
    pub end_time: Time,
    /// The course id.
    pub course_id: usize,
    /// The professor.
    pub professor: &'static str,
    /// Recitations
    pub recitations: Vec<Meeting>,
    /// Location
    pub location: &'static str,
}

impl Meeting {
    pub fn to_output(&self, course_name: &'static str) -> MeetingOutput {
        MeetingOutput {
            course_name,
            days: self.days,
            start_time: self.start_time,
            end_time: self.end_time,
            professor: self.professor,
            location: self.location,
        }
    }
}
