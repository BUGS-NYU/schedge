use crate::models::chrono::{Day, Time};
use crate::models::course::Course;
use crate::models::meeting::Meeting;
use std::collections::HashMap;

/// Data is a vector of tuples where the first element is an identifier of the course,
/// and the second is a list of meetings that that course contains.
#[derive(Debug)]
pub struct SeedData {
    /// Meeting numbers -> Lists of meetings/sections
    pub meetings: HashMap<u32, Meeting>,
    /// Course numbers -> Course metadata
    pub courses: HashMap<u32, Course>,
    /// Department codes -> Course Numbers
    pub departments: HashMap<&'static str, Vec<u32>>,
}

#[inline(always)]
pub fn get_seed_data() -> SeedData {
    use Day::*;

    let mut courses = HashMap::new();

    courses.insert(
        0,
        Course {
            name: "Writing The Essay",
            prerequisites: Vec::new(),
        },
    );

    courses.insert(
        1,
        Course {
            name: "Introduction to Programming",
            prerequisites: Vec::new(),
        },
    );

    let mut meetings = HashMap::new();

    meetings.insert(
        2,
        Meeting {
            course_id: 0,
            days: (Tues, Thurs),
            start_time: Time(750),
            end_time: Time(825),
            professor: "Joseph Califf".into(), // This guy is awesome!
        },
    );

    meetings.insert(
        3,
        Meeting {
            course_id: 0,
            days: (Mon, Wed),
            start_time: Time(750),
            end_time: Time(825),
            professor: "Noelle Liston".into(),
        },
    );
    meetings.insert(
        4,
        Meeting {
            course_id: 0,
            days: (Tues, Thurs),
            start_time: Time(660),
            end_time: Time(735),
            professor: "Matthew McClelland".into(),
        },
    );

    meetings.insert(
        5,
        Meeting {
            course_id: 0,
            days: (Mon, Wed),
            start_time: Time(660),
            end_time: Time(735),
            professor: "Noelle Liston".into(),
        },
    );

    meetings.insert(
        6,
        Meeting {
            course_id: 1,
            days: (Tues, Thurs),
            start_time: Time(840),
            end_time: Time(915),
            professor: "Anasse Bari".into(),
        },
    );

    meetings.insert(
        7,
        Meeting {
            course_id: 1,
            days: (Tues, Thurs),
            start_time: Time(570),
            end_time: Time(645),
            professor: "Amos Bloomberg".into(),
        },
    );

    meetings.insert(
        8,
        Meeting {
            course_id: 1,
            days: (Tues, Thurs),
            start_time: Time(930),
            end_time: Time(1005),
            professor: "Teseo Schneider".into(),
        },
    );

    meetings.insert(
        9,
        Meeting {
            course_id: 1,
            days: (Mon, Wed),
            start_time: Time(930),
            end_time: Time(1005),
            professor: "Hilbert Locklear".into(),
        },
    );

    let mut departments = HashMap::new();

    departments.insert("CORE-UA", vec![0]);
    departments.insert("CSCI-UA", vec![1]);

    SeedData {
        meetings,
        courses,
        departments,
    }
}
