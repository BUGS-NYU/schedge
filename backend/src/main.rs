#![feature(proc_macro_hygiene, decl_macro)]

#[macro_use]
extern crate rocket;
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
/// Seed ata to initialize the server with
mod seed;

use rocket_cors::CorsOptions;

#[database("schedge")]
struct DbConn(diesel::PgConnection);

fn main() {
    let default = CorsOptions::default();
    let cors = CorsOptions::to_cors(&default).unwrap();
    rocket::ignite()
        .mount(
            "/schedule-by-deparments",
            routes![response::schedule_using_department],
        )
        .mount(
            "/avaiable-by-completed",
            routes![response::legal_courses_from_completed_courses],
        )
        .attach(cors)
        .launch();
}
