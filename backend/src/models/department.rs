use serde::{Deserialize, Serialize};

#[derive(Debug, Queryable, Serialize, Deserialize)]
pub struct Department {
    id: i32,
    code: String,
    name: String,
}

pub fn string_to_department_id(string: &str) -> Option<usize> {
    match string {
        "CORE-UA" => Some(0),
        "CSCI-UA" => Some(1),
        "MATH-UA" => Some(2),
        _ => None,
    }
}
