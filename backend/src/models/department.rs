use crate::schema::departments;
use diesel::RunQueryDsl;
use rocket_contrib::databases::diesel;
use serde::{Deserialize, Serialize};

#[derive(Debug, Queryable, Serialize, Deserialize)]
pub struct Department {
    id: i32,
    code: String,
    name: String,
}

pub fn get_departments(conn: &diesel::PgConnection) -> Vec<Department> {
    departments::table
        .load::<Department>(conn)
        .expect("Cannot load departments")
}
