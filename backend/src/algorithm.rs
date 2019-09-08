use crate::models::meeting::Meeting;
use crate::seed::SeedData;
use rand::prelude::*;

/// Compute a schedule based on the given meeting times
pub fn schedule_by_department(
    seed_data: &SeedData,
    mut department_ids: Vec<usize>,
) -> Option<Vec<Meeting>> {
    let mut rng = rand::thread_rng();

    for _ in 0..3 {
        let rand_index = rng.gen::<usize>() % department_ids.len();
        department_ids.rotate_right(rand_index);
        let mut schedule = Vec::new();
        for dept_id in &department_ids {
            match get_course_from_dept(&schedule, seed_data, *dept_id) {
                Some(course) => schedule.push(course),
                None => break,
            };
        }
        if schedule.len() == department_ids.len() {
            return Some(schedule);
        }
    }
    None
}

fn get_course_from_dept(
    schedule: &Vec<Meeting>,
    seed_data: &SeedData,
    dept_id: usize,
) -> Option<Meeting> {
    let (course_id, _course) = seed_data
        .courses
        .iter()
        .enumerate()
        .find(|(_, course)| course.department_id == dept_id)
        .unwrap();
    let mut meetings_in_course: Vec<&Meeting> = seed_data
        .meetings
        .iter()
        .filter(|meeting| meeting.course_id == course_id)
        .collect();
    let mut rng = rand::thread_rng();
    meetings_in_course.shuffle(&mut rng);
    for meeting in meetings_in_course {
        // Checks if given meeting fits into schedule
        if schedule
            .iter()
            .all(|sched_meeting| !does_overlap(sched_meeting, meeting))
        {
            return Some(meeting.clone());
        }
    }
    None
}

fn does_overlap(m1: &Meeting, m2: &Meeting) -> bool {
    // If the days don't overlap at all (Mon/Wed and Tues/Thurs)
    // Then we can just return true
    if m1.days.0 != m2.days.0
        && m1.days.0 != m2.days.1
        && m1.days.1 != m2.days.0
        && m1.days.1 != m2.days.1
    {
        return true;
    }
    m1.start_time <= m2.end_time && m2.start_time <= m1.end_time
}
