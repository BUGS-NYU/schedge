#![feature(proc_macro_hygiene, decl_macro)]

#[macro_use]
extern crate rocket;
extern crate reqwest;
#[macro_use]
extern crate rocket_contrib;
#[macro_use]
extern crate serde;
#[macro_use]
extern crate diesel;
extern crate rand;

/// Code that handles building a schedule
mod algorithm;
/// Structs, Enums, etc. used to model information the algorithm acts on
mod models;
/// Code that handles dispatching http requests & sending off a response
mod response;
/// Schema data. Deprecated (?)
mod schema;
/// Utilities for scraping Albert.
mod scraping;
/// Seed data to initialize the server with
mod seed;

use rocket_contrib::serve::StaticFiles;
use rocket_cors::CorsOptions;

#[database("schedge")]
struct DbConn(diesel::PgConnection);

fn main() {
    let default = CorsOptions::default();
    let cors = CorsOptions::to_cors(&default).unwrap();
    rocket::ignite()
        .mount(
            "/schedule-by-departments",
            routes![response::schedule_using_department],
        )
        .mount(
            "/available-by-completed",
            routes![response::legal_courses_from_completed_courses],
        )
        .mount(
            "/schedule-by-course-list",
            routes![response::schedule_using_course_list],
        )
        .mount("/courses", routes![response::all_intro_courses])
        .mount("/public", StaticFiles::from("static"))
        .attach(cors)
        .launch();
}
