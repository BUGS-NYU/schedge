use crate::algorithm::compute_schedule;
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
    let core_dept = &data.departments["CORE-UA"];
    let dept_data = data.departments.get(&*department);
    if let Some(dept_data) = dept_data {
        let schedule = compute_schedule(&data.meetings, vec![core_dept[0], dept_data[0]])
            .unwrap_or(Vec::new());
        Ok(Json(schedule))
    } else {
        Err(Custom(
            Status::BadRequest,
            format!("No such department: '{}'", department).into(),
        ))
    }
}
