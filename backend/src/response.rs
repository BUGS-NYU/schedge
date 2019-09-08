use crate::algorithm::compute_schedule;
use crate::models::department::str_to_department_id;
use crate::models::meeting::MeetingOutput;
use rocket::http::Status;
use rocket::response::status::Custom;
use rocket_contrib::json::Json;

#[get("/<department>")]
pub fn schedule_using_department(
    mut department: String,
) -> Result<Json<Vec<MeetingOutput>>, Custom<String>> {
    let data = crate::seed::get_seed_data(); // TODO make this into a stored state
                                             // TODO Move core out of seed data, into core seed data

    department.make_ascii_uppercase();
    let expos_id = str_to_department_id("EXPOS-UA").unwrap();
    let major_dept_id = str_to_department_id(&department);
    if let Some(major_id) = major_dept_id {
        let schedule = compute_schedule(&data, vec![expos_id, major_id]).unwrap_or(Vec::new());
        let schedule = schedule
            .into_iter()
            .map(|raw_meeting| raw_meeting.to_output(&data.courses[raw_meeting.course_id].name))
            .collect();
        Ok(Json(schedule))
    } else {
        Err(Custom(
            Status::BadRequest,
            format!("No such department: '{}'", department.clone()).into(),
        ))
    }
}

#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct FormInput {
    course_requests: Vec<u32>,
    completed_courses: Vec<u32>,
}

#[post("/sophomores", data = "<form>")]
pub fn schedule_using_course_list(
    form: Json<FormInput>,
) -> Result<Json<Vec<MeetingOutput>>, Custom<String>> {
    Err(Custom(Status::BadRequest, "Ooooooof".into()))
}
