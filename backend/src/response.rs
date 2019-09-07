use crate::algorithm::compute_schedule;
use crate::models::department::string_to_department_id;
use crate::models::meeting::Meeting;
use rocket::http::Status;
use rocket::response::status::Custom;
use rocket_contrib::json::Json;

#[get("/<department>")]
pub fn schedule_using_department(
    mut department: String,
) -> Result<Json<Vec<Meeting>>, Custom<String>> {
    let data = crate::seed::get_seed_data(); // TODO make this into a fairing
                                             // TODO Move core out of seed data, into core seed data

    department.make_ascii_uppercase();
    let expos_id = string_to_department_id("EXPOS-UA".into()).unwrap();
    let major_dept_id = string_to_department_id(&department);
    if let Some(major_id) = major_dept_id {
        let schedule = compute_schedule(&data, vec![expos_id, major_id]).unwrap_or(Vec::new());
        Ok(Json(schedule))
    } else {
        Err(Custom(
            Status::BadRequest,
            format!("No such department: '{}'", department.clone()).into(),
        ))
    }
}
