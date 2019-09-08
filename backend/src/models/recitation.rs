use crate::models::chrono::{Day, Time};

#[derive(Clone, Debug, Serialize)]
pub struct Recitation {
    /// The days this meeting happens.
    pub day: Day,
    /// The start time of this meeting.
    pub start_time: Time,
    /// The end time of this meeting.
    pub end_time: Time,
    /// Professor
    pub professor: &'static str,
    /// Location
    pub location: &'static str,
}
