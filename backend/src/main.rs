#![feature(proc_macro_hygiene, decl_macro)]

#[macro_use]
extern crate rocket;
#[macro_use]
extern crate rocket_contrib;
extern crate serde;
#[macro_use]
extern crate diesel;

mod models;
mod schema;
mod seed;

use crate::models::department::{get_departments, Department};
use crate::models::meeting::Meeting;
use rocket_contrib::json::Json;

#[database("schedge")]
struct DbConn(diesel::PgConnection);

#[get("/")]
fn departments_index(conn: DbConn) -> Json<Vec<Department>> {
    Json(get_departments(&conn))
}

fn main() {
    // rocket::ignite()
    //     .mount("/departments", routes![departments_index])
    //     .attach(DbConn::fairing())
    //     .launch();
    let data = seed::get_seed_data();
    let schedule = compute_schedule(data.writing_the_essay_meetings, data.intro_cs_meetings);
    println!("{:?}", schedule);
}

fn compute_schedule(
    wte_meetings: Vec<Meeting>,
    intro_course_meetings: Vec<Meeting>,
) -> Option<Vec<Meeting>> {
    for wte_meeting in &wte_meetings {
        for intro_course_meeting in &intro_course_meetings {
            if !does_overlap(intro_course_meeting, wte_meeting) {
                return Some(vec![intro_course_meeting.clone(), wte_meeting.clone()]);
            }
        }
    }
    None
}

fn does_overlap(m1: &Meeting, m2: &Meeting) -> bool {
    // If the days don't overlap at all (Mon/Wed and Tues/Thurs)
    // Then we can just return true
    if m1.days.0 != m2.days.0
        && m1.days.0 != m2.days.1
        && m1.days.1 != m2.days.0
        && m1.days.1 != m2.days.1
    {
        return true;
    }
    m1.start_time <= m2.end_time && m2.start_time <= m1.end_time
}
