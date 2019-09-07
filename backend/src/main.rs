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

use crate::models::department::{get_departments, Department};
use rocket_contrib::json::Json;

#[database("schedge")]
struct DbConn(diesel::PgConnection);

#[get("/")]
fn departments_index(conn: DbConn) -> Json<Vec<Department>> {
    Json(get_departments(&conn))
}

fn main() {
    rocket::ignite()
        .mount("/departments", routes![departments_index])
        .attach(DbConn::fairing())
        .launch();
}
