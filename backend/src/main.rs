#![feature(proc_macro_hygiene, decl_macro)]

#[macro_use]
extern crate rocket;
#[macro_use]
extern crate rocket_contrib;
#[macro_use]
extern crate serde;
#[macro_use]
extern crate diesel;

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

#[database("schedge")]
struct DbConn(diesel::PgConnection);

fn main() {
    rocket::ignite()
        .mount("/schedule", routes![response::schedule_using_department])
        .launch();
}
