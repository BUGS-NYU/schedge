use serde::{Deserialize, Serialize};

#[derive(Debug, Queryable, Serialize, Deserialize)]
pub struct Department {
    id: i32,
    code: String,
    name: String,
}

pub static EXPOS_DEPT_ID: usize = 0;
pub static CSCI_DEPT_ID: usize = 1;
pub static MATH_DEPT_ID: usize = 2;
pub static CORE_DEPT_ID: usize = 3;
pub static CRWRI_DEPT_ID: usize = 4;

pub fn str_to_department_id(string: &str) -> Option<usize> {
    match string {
        "EXPOS-UA" => Some(EXPOS_DEPT_ID),
        "CSCI-UA" => Some(CSCI_DEPT_ID),
        "MATH-UA" => Some(MATH_DEPT_ID),
        "CORE-UA" => Some(CORE_DEPT_ID),
        "CRWRI-UA" => Some(CRWRI_DEPT_ID),
        _ => None,
    }
}
