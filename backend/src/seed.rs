use crate::models::meeting::Meeting;

#[derive(Debug)]
pub struct SeedData {
    pub writing_the_essay_meetings: Vec<Meeting>,
    pub intro_cs_meetings: Vec<Meeting>,
}

pub fn get_seed_data() -> SeedData {
    let writing_the_essay_meetings = vec![
        Meeting {
            days: (1, 3),
            start_time: 750,
            end_time: 825,
            professor: "Joseph Califf".into(),
        },
        Meeting {
            days: (0, 2),
            start_time: 750,
            end_time: 825,
            professor: "Noelle Liston".into(),
        },
        Meeting {
            days: (1, 3),
            start_time: 660,
            end_time: 735,
            professor: "Matthew McClelland".into(),
        },
        Meeting {
            days: (0, 2),
            start_time: 660,
            end_time: 735,
            professor: "Noelle Liston".into(),
        },
    ];
    let intro_cs_meetings = vec![
        Meeting {
            days: (1, 3),
            start_time: 840,
            end_time: 915,
            professor: "Anasse Bari".into(),
        },
        Meeting {
            days: (1, 3),
            start_time: 570,
            end_time: 645,
            professor: "Amos Bloomberg".into(),
        },
        Meeting {
            days: (1, 3),
            start_time: 930,
            end_time: 1005,
            professor: "Teseo Schneider".into(),
        },
        Meeting {
            days: (0, 2),
            start_time: 930,
            end_time: 1005,
            professor: "Hilbert Locklear".into(),
        },
    ];
    SeedData {
        writing_the_essay_meetings,
        intro_cs_meetings,
    }
}
