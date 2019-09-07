use crate::algorithm::compute_schedule;
use crate::models::meeting::Meeting;
use rocket_contrib::json::Json;

#[get("/<department>")]
pub fn schedule_using_department(mut department: String) -> Json<Vec<Meeting>> {
    let data = crate::seed::get_seed_data(); // TODO make this into a fairing
                                             // TODO Move core out of seed data, into core seed data

    department.make_ascii_uppercase();
    let core = &data.departments["CORE-UA"];
    let schedule = compute_schedule(
        &data.meetings,
        vec![core[0], data.departments[&*department][0]],
    )
    .unwrap_or(Vec::new());
    Json(schedule)
}
