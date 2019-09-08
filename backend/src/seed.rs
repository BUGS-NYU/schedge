use crate::models::chrono::{Day, Time};
use crate::models::course::Course;
use crate::models::department::*;
use crate::models::meeting::Meeting;

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

    let mut course_id = -1;
    let mut courses = Vec::new();
    let mut meetings = Vec::new();

    /*
     Careful on sequencing these. All meetings for the course should
     follow new_course!
    */
    macro_rules! new_course {
        ($string:expr, $id:ident) => {
            courses.push(Course {
                name: $string,
                department_id: $id,
                prerequisites: Vec::new(),
            });
            course_id += 1
        };
    }

    macro_rules! new_meeting {
        ($days:expr, ($time1:expr, $time2:expr), $prof:expr) => {
            new_meeting!($days, ($time1, $time2), $prof, Vec::new(), "N/A")
        };
        ($days:expr, ($time1:expr, $time2:expr), $prof:expr, $location:expr) => {
            new_meeting!($days, ($time1, $time2), $prof, Vec::new(), $location)
        };
        ($days:expr, ($time1:expr, $time2:expr), $prof:expr, $recitations:expr, $location:expr) => {
            meetings.push(Meeting {
                course_id: course_id as usize,
                days: $days,
                start_time: Time(($time1 % 100) + ($time1 / 100 * 60)),
                end_time: Time(($time2 % 100) + ($time2 / 100 * 60)),
                recitations: $recitations,
                professor: $prof,
                location: $location,
            });
        };
    }

    macro_rules! new_recitation {
        ($day:expr, ($time1:expr, $time2:expr), $professor:expr) => {
            new_recitation!($day, ($time1, $time2), $professor, "N/A")
        };
        ($day:expr, ($time1:expr, $time2:expr), $professor:expr, $location:expr) => {
            Meeting {
                days: ($day, $day),
                start_time: Time(($time1 % 100) + ($time1 / 100 * 60)),
                end_time: Time(($time2 % 100) + ($time2 / 100 * 60)),
                professor: $professor,
                recitations: Vec::new(),
                location: $location,
                course_id: course_id as usize,
            }
        };
    }

    new_course!("Writing The Essay", EXPOS_DEPT_ID);
    new_meeting!((Mon, Wed), (1230, 1345), "Noelle Liston", "N/A");
    new_meeting!((Tues, Thurs), (1230, 1345), "Joseph Califf", "N/A");
    new_meeting!((Tues, Thurs), (1100, 1215), "Matthew McClelland", "N/A");
    new_meeting!((Mon, Wed), (1100, 1215), "Noelle Liston", "N/A");

    new_course!("Introduction to Computer Programming", CSCI_DEPT_ID);
    new_meeting!(
        (Tues, Thurs),
        (1230, 1345),
        "Nathan Hull",
        "Warren Weaver Hall: Room 202"
    );
    new_meeting!(
        (Tues, Thurs),
        (1100, 1215),
        "Craig Kapp, Julie Lizardo",
        "60 5th Ave: Room 150"
    );
    new_meeting!(
        (Mon, Wed),
        (930, 1045),
        "David Gochfeld",
        "Warren Weaver Hall: Room 317"
    );
    new_meeting!(
        (Mon, Wed),
        (330, 445),
        "Dakota Hernandez",
        "Warren Weaver Hall: Room 317"
    );
    new_meeting!((Mon, Wed), (1230, 1345), "Deena Engel", "Meyer Hall: 122");
    new_meeting!(
        (Tue, Thu),
        (930, 1045),
        "Shaheer Haroon",
        "Warren Weaver Hall: 317"
    );
    new_meeting!(
        (Tue, Thu),
        (1530, 1645),
        "Saadia Lgarch",
        "Warren Weaver Hall: 517"
    );
    new_meeting!(
        (Tue, Thu),
        (800, 915),
        "Na''im Tyson",
        "Warren Weaver Hall: 201"
    );
    new_meeting!(
        (Mon, Wed),
        (1400, 1515),
        "Adam Meyers",
        "Warren Weaver Hall: 201"
    );
    new_meeting!(
        (Tue, Thu),
        (800, 915),
        "Michell Cardona",
        "Warren Weaver Hall: 312"
    );

    new_course!(
        "Introduction to Computer Science",
        CSCI_DEPT_ID,
        vec![course_id]
    );
    new_meeting!(
        (Mon, Wed),
        (1530, 1645),
        "Hilbert Locklear",
        "Warren Weaver Hall: Room 102"
    );
    new_meeting!(
        (Tues, Thurs),
        (1400, 1515),
        "Anasse Bari",
        "Warren Weaver Hall: Room 101"
    );
    new_meeting!(
        (Tues, Thurs),
        (930, 1045),
        "Amos Bloomberg",
        "60 5th Avenue: Room 110"
    );
    new_meeting!(
        (Tues, Thurs),
        (1530, 1645),
        "Teseo Schneider",
        "25 W 4th St: Room C-20"
    );

    new_course!("Calculus I", MATH_DEPT_ID);
    new_meeting!(
        (Mon, Wed),
        (930, 1045),
        "Wayne Uy",
        vec![
            new_recitation!(Fri, (800, 915), "Yue Huang"),
            new_recitation!(Fri, (930, 1045), "Yue Huang"),
            new_recitation!(Fri, (800, 915), "Zhimeng Wang"),
            new_recitation!(Fri, (930, 1045), "Zhimeng Wang"),
        ],
        "N/A"
    );
    new_meeting!(
        (Tues, Thurs),
        (1230, 1345),
        "Selin Kalaycioglu",
        vec![
            new_recitation!(Fri, (1100, 1215), "Damilola Dauda"),
            new_recitation!(Fri, (1230, 1345), "Damilola Dauda"),
            new_recitation!(Fri, (1100, 1215), "Haoyu Wang"),
            new_recitation!(Fri, (1230, 1345), "Haoyu Wang")
        ],
        "N/A"
    );
    new_meeting!(
        (Tues, Thurs),
        (1655, 1810),
        "N/A",
        vec![
            new_recitation!(Fri, (800, 915), "Masato Takigawa"),
            new_recitation!(Fri, (930, 1045), "Masato Takigawa"),
            new_recitation!(Fri, (800, 915), "Xiangjia Kong"),
            new_recitation!(Fri, (930, 1045), "Xiangjia Kong"),
        ],
        "N/A"
    );
    new_meeting!(
        (Mon, Wed),
        (1400, 1515),
        "Sia Charmaine",
        vec![
            new_recitation!(Fri, (1100, 1215), "Jun Hyuk"),
            new_recitation!(Fri, (1230, 1345), "Jun Hyuk"),
            new_recitation!(Fri, (1000, 1215), "Karl Dessenne"),
            new_recitation!(Fri, (1230, 1345), "Karl Dessenne"),
        ],
        "N/A"
    );
    new_meeting!(
        (Mon, Wed),
        (1230, 1345),
        "Hesam Oveys",
        vec![
            new_recitation!(Tues, (800, 915), "Dong Hyun Seo"),
            new_recitation!(Thurs, (800, 1345), "Dong Hyun Seo"),
            new_recitation!(Tues, (800, 915), "Yi Shan"),
            new_recitation!(Thurs, (800, 915), "Yi Shan"),
        ],
        "N/A"
    );

    new_course!(
        "Quantitative Reasoning: Elementary Statistics",
        CORE_DEPT_ID
    );
    new_meeting!(
        (Tues, Thurs),
        (1530, 1645),
        "Charmaine Sia",
        vec![
            new_recitation!(Fri, (930, 1045), "Jialu Xie"),
            new_recitation!(Fri, (1100, 1215), "Jialu Xie"),
            new_recitation!(Fri, (800, 915), "Gabriel Jose Labrousse"),
            new_recitation!(Fri, (930, 1045), "Gabriel Jose Labrousse"),
        ],
        "N/A"
    );

    new_course!("Cultures & Contexts: Italy", CORE_DEPT_ID);
    new_meeting!(
        (Mon, Wed),
        (1100, 1215),
        "Rebecca Falkoff",
        vec![
            new_recitation!(Fri, (800, 915), "Emily Antenucci"),
            new_recitation!(Fri, (930, 1045), "Emily Antenucci"),
            new_recitation!(Fri, (1100, 1215), "Emily Antenucci"),
            new_recitation!(Fri, (1230, 1345), "Emily Antenucci"),
        ],
        "Silver: Room 512"
    );

    new_course!("Creative Writing: Intro Fiction & Poetry", CRWRI_DEPT_ID);
    new_meeting!(
        (Mon, Wed),
        (930, 1045),
        "Michele Filgate",
        vec![],
        "Bobst LL148"
    );
    new_meeting!(
        (Mon, Wed),
        (1655, 1810),
        "Angelo Nikolopoulos",
        vec![],
        "Bobst LL140"
    );
    new_meeting!(
        (Mon, Wed),
        (1230, 1345),
        "Bernard Ferguson",
        vec![],
        "Bobst LL148"
    );

    SeedData { meetings, courses }
}
