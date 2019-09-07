#![feature(proc_macro_hygiene, decl_macro)]

#[macro_use]
extern crate rocket;
#[macro_use]
extern crate rocket_contrib;
#[macro_use]
extern crate serde;
#[macro_use]
extern crate diesel;

/// Structs, Enums, etc. used to model information the algorithm acts on
mod models;
/// Schema data. Deprecated (?)
mod schema;
/// Seed ata to initialize the server with
mod seed;

// use crate::models::department::{get_departments, Department};
use crate::models::meeting::Meeting;
use rocket_contrib::json::Json;

#[database("schedge")]
struct DbConn(diesel::PgConnection);

// #[get("/")]
// fn departments_index(conn: DbConn) -> Json<Vec<Department>> {
//     Json(get_departments(&conn))
// }

#[get("/<_department>")]
fn schedule_using_department(_department: String) -> Json<Vec<Meeting>> {
    let data = seed::get_seed_data(); // TODO make this into a fairing
                                      // TODO Move core out of seed data, into core seed data
    let schedule = compute_schedule(data.0).unwrap_or(Vec::new());
    Json(schedule)
}

fn main() {
    rocket::ignite()
        .mount("/schedule", routes![schedule_using_department])
        // .attach(DbConn::fairing())
        .launch();
}

/// Compute a schedule based on the given meeting times
fn compute_schedule(meetings: Vec<(String, Vec<Meeting>)>) -> Option<Vec<Meeting>> {
    let mut progress = Vec::new();
    if compute_schedule_rec(&meetings, 0, &mut progress) {
        Some(progress)
    } else {
        None
    }
}

/// Recursive subroutine of `compute_schedule`
fn compute_schedule_rec(
    meetings: &Vec<(String, Vec<Meeting>)>,
    idx: usize,
    progress: &mut Vec<Meeting>,
) -> bool {
    if idx >= meetings.len() {
        true
    } else {
        for course in &meetings[idx].1 {
            let mut iter = progress.iter();
            let valid = loop {
                if let Some(prev) = iter.next() {
                    if does_overlap(prev, course) {
                        break false;
                    }
                } else {
                    // We've gone through them all; we're done, and there's no conflict!
                    break true;
                }
            };

            if valid {
                progress.push(*course);
                if compute_schedule_rec(meetings, idx + 1, progress) {
                    return true;
                }
                progress.pop();
            }
        }

        false
    }
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
