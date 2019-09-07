use crate::models::chrono::{Day, Time};
use crate::models::course::Course;
use crate::models::department::{CSCI_DEPT_ID, EXPOS_DEPT_ID, MATH_DEPT_ID};
use crate::models::meeting::Meeting;

// macro_rules! new_course {
//     ($string:expr, $id:ident) => {
//         Course {
//             name: $string,
//             department_id: $id,
//             prerequisites: Vec::new(),
//         }
//     };
// }

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
            meetings.push(Meeting {
                course_id: course_id as usize,
                days: $days,
                start_time: Time($time1),
                end_time: Time($time2),
                professor: $prof,
            })
        };
    }

    new_course!("Writing The Essay", EXPOS_DEPT_ID);
    new_meeting!((Tues, Thurs), (750, 825), "Joseph Califf");
    new_meeting!((Mon, Wed), (750, 825), "Noelle Liston");
    new_meeting!((Tues, Thurs), (660, 735), "Matthew McClelland");
    new_meeting!((Mon, Wed), (660, 735), "Noelle Liston");

    new_course!("Introduction to Computer Science", CSCI_DEPT_ID);
    new_meeting!((Tues, Thurs), (840, 915), "Anasse Bari");
    new_meeting!((Tues, Thurs), (570, 645), "Amos Bloomberg");
    new_meeting!((Tues, Thurs), (930, 1005), "Teseo Schneider");
    new_meeting!((Mon, Wed), (930, 1005), "Hilbert Locklear");

    new_course!("Calculus I", MATH_DEPT_ID);
    new_meeting!((Mon, Wed), (570, 645), "Wayne Uy");
    new_meeting!((Tues, Thurs), (750, 825), "Selin Kalaycioglu");
    new_meeting!((Tues, Thurs), (1015, 1090), "N/A");
    new_meeting!((Mon, Wed), (840, 915), "Sia Charmaine");
    new_meeting!((Mon, Wed), (750, 825), "Hesam Oveys");

    SeedData { meetings, courses }
}
