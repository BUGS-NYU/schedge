use crate::models::chrono::{Day, Time};
use crate::models::course::Course;
use crate::models::department::{CSCI_DEPT_ID, EXPOS_DEPT_ID, MATH_DEPT_ID};
use crate::models::meeting::Meeting;
use std::collections::HashMap;

/// Data is a vector of tuples where the first element is an identifier of the course,
/// and the second is a list of meetings that that course contains.
#[derive(Debug)]
pub struct SeedData {
    /// Meeting numbers -> Lists of meetings/sections
    pub meetings: Vec<Meeting>,
    /// Course numbers -> Course metadata
    pub courses: Vec<Course>,
}

#[inline(always)]
pub fn get_seed_data() -> SeedData {
    use Day::*;

    let courses = vec![
        Course {
            name: "Writing The Essay",
            department_id: EXPOS_DEPT_ID,
            prerequisites: Vec::new(),
        },
        Course {
            name: "Introduction to Computer Science",
            department_id: CSCI_DEPT_ID,
            prerequisites: Vec::new(),
        },
        Course {
            name: "Calculus I",
            department_id: MATH_DEPT_ID,
            prerequisites: Vec::new(),
        },
    ];

    let meetings = vec![
        Meeting {
            course_id: 0,
            days: (Tues, Thurs),
            start_time: Time(750),
            end_time: Time(825),
            professor: "Joseph Califf".into(), // This guy is awesome!
        },
        Meeting {
            course_id: 0,
            days: (Mon, Wed),
            start_time: Time(750),
            end_time: Time(825),
            professor: "Noelle Liston".into(),
        },
        Meeting {
            course_id: 0,
            days: (Tues, Thurs),
            start_time: Time(660),
            end_time: Time(735),
            professor: "Matthew McClelland".into(),
        },
        Meeting {
            course_id: 0,
            days: (Mon, Wed),
            start_time: Time(660),
            end_time: Time(735),
            professor: "Noelle Liston".into(),
        },
        Meeting {
            course_id: 1,
            days: (Tues, Thurs),
            start_time: Time(840),
            end_time: Time(915),
            professor: "Anasse Bari".into(),
        },
        Meeting {
            course_id: 1,
            days: (Tues, Thurs),
            start_time: Time(570),
            end_time: Time(645),
            professor: "Amos Bloomberg".into(),
        },
        Meeting {
            course_id: 1,
            days: (Tues, Thurs),
            start_time: Time(930),
            end_time: Time(1005),
            professor: "Teseo Schneider".into(),
        },
        Meeting {
            course_id: 1,
            days: (Mon, Wed),
            start_time: Time(930),
            end_time: Time(1005),
            professor: "Hilbert Locklear".into(),
        },
        Meeting {
            course_id: 2,
            days: (Mon, Wed),
            start_time: Time(570),
            end_time: Time(645),
            professor: "Wayne Uy".into(),
        },
        Meeting {
            course_id: 2,
            days: (Tues, Thurs),
            start_time: Time(750),
            end_time: Time(825),
            professor: "Selin Kalaycioglu".into(),
        },
        Meeting {
            course_id: 2,
            days: (Tues, Thurs),
            start_time: Time(1015),
            end_time: Time(1090),
            professor: "N/A".into(),
        },
        Meeting {
            course_id: 2,
            days: (Mon, Wed),
            start_time: Time(840),
            end_time: Time(915),
            professor: "Sia Charmaine".into(),
        },
        Meeting {
            course_id: 2,
            days: (Mon, Wed),
            start_time: Time(750),
            end_time: Time(825),
            professor: "Hesam Oveys".into(),
        },
    ];

    SeedData { meetings, courses }
}
