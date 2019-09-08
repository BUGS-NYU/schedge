use crate::algorithm;
use crate::models::course::CourseOutput;
use crate::models::department::str_to_department_id;
use crate::models::meeting::MeetingOutput;
use crate::seed::get_seed_data;
use rocket::http::Status;
use rocket::response::status::Custom;
use rocket_contrib::json::Json;
use std::collections::HashMap;

#[get("/<department>")]
pub fn schedule_using_department(
    mut department: String,
) -> Result<Json<Vec<MeetingOutput>>, Custom<String>> {
    let data = get_seed_data(); // TODO make this into a stored state
                                // TODO Move core out of seed data, into core seed data

    department.make_ascii_uppercase();
    let expos_id = str_to_department_id("EXPOS-UA").unwrap();
    let major_dept_id = str_to_department_id(&department);
    if let Some(major_id) = major_dept_id {
        let schedule = algorithm::schedule_by_department(&data, vec![expos_id, major_id])
            .unwrap_or(Vec::new());
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

#[get("/")]
pub fn all_courses() -> Json<Vec<CourseOutput>> {
    let seed = get_seed_data();

    let courses = seed
        .courses
        .into_iter()
        .enumerate()
        .map(|(id, course)| course.to_output(id))
        .collect();
    Json(courses)
}

#[post("/", data = "<completed_courses>")]
pub fn legal_courses_from_completed_courses(
    completed_courses: Json<Vec<usize>>,
) -> Json<Vec<CourseOutput>> {
    let seed = get_seed_data();

    let mut eligible_courses: HashMap<_, _> = seed.courses.into_iter().enumerate().collect();

    for i in &*completed_courses {
        eligible_courses = eligible_courses
            .into_iter()
            .filter(|(id, course)| course.prerequisites.contains(i) && id != i)
            .collect();
    }

    let courses = eligible_courses
        .into_iter()
        .map(|(id, course)| course.as_output(id))
        .collect();

    Json(courses)
}

// #[post("/", data = "<form>")]
// pub fn schedule_using_course_list(
//     form: Json<FormInput>,
// ) -> Result<Json<Vec<MeetingOutput>>, Custom<String>> {
//     Err(Custom(Status::BadRequest, "Ooooooof".into()))
// }
