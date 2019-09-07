use crate::models::chrono::{Day, Time};
use crate::models::meeting::Meeting;
use std::collections::HashMap;

/// Data is a vector of tuples where the first element is an identifier of the course,
/// and the second is a list of meetings that that course contains.
#[derive(Debug)]
pub struct SeedData(pub Vec<(String, Vec<Meeting>)>);

/// Data is a HashMap where the keys are identifiers of a course, and the values
/// are a list of meetings that that course contains.
#[allow(dead_code)]
#[derive(Debug)]
pub struct CSVData(pub HashMap<String, Vec<Meeting>>);

#[allow(dead_code)]
pub fn get_csv_data() -> CSVData {
    CSVData(HashMap::new())
}

pub fn get_seed_data() -> SeedData {
    use Day::*;

    let writing_the_essay_meetings = vec![
        Meeting {
            crn: 1,
            days: (Tues, Thurs),
            start_time: Time(750),
            end_time: Time(825),
            // professor: "Joseph Califf".into(), // This guy is awesome!
        },
        Meeting {
            crn: 2,
            days: (Mon, Wed),
            start_time: Time(750),
            end_time: Time(825),
            // professor: "Noelle Liston".into(),
        },
        Meeting {
            crn: 3,
            days: (Tues, Thurs),
            start_time: Time(660),
            end_time: Time(735),
            // professor: "Matthew McClelland".into(),
        },
        Meeting {
            crn: 4,
            days: (Mon, Wed),
            start_time: Time(660),
            end_time: Time(735),
            // professor: "Noelle Liston".into(),
        },
    ];
    let intro_cs_meetings = vec![
        Meeting {
            crn: 5,
            days: (Tues, Thurs),
            start_time: Time(840),
            end_time: Time(915),
            // professor: "Anasse Bari".into(),
        },
        Meeting {
            crn: 6,
            days: (Tues, Thurs),
            start_time: Time(570),
            end_time: Time(645),
            // professor: "Amos Bloomberg".into(),
        },
        Meeting {
            crn: 7,
            days: (Tues, Thurs),
            start_time: Time(930),
            end_time: Time(1005),
            // professor: "Teseo Schneider".into(),
        },
        Meeting {
            crn: 8,
            days: (Mon, Wed),
            start_time: Time(930),
            end_time: Time(1005),
            // professor: "Hilbert Locklear".into(),
        },
    ];
    SeedData(vec![
        ("WTE 001".into(), writing_the_essay_meetings),
        ("CS-UA 001".into(), intro_cs_meetings),
    ])
}
