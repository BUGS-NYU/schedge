use crate::models::meeting::Meeting;
use crate::seed::SeedData;
use rand::prelude::*;
use std::collections::HashSet;

/// Compute a schedule based on the given meeting times
pub fn schedule_by_department(
    seed_data: &SeedData,
    mut department_ids: Vec<usize>,
) -> Option<Vec<Meeting>> {
    let mut rng = rand::thread_rng();
    for _ in 0..10 {
        let mut departments_added = 0;
        let rand_index = rng.gen::<usize>() % department_ids.len();
        department_ids.rotate_right(rand_index);
        let mut schedule = Vec::new();
        for dept_id in &department_ids {
            let mut course = get_course_from_dept(&schedule, seed_data, *dept_id);
            if course.len() > 0 {
                schedule.append(&mut course);
                departments_added += 1;
            }
        }
        if departments_added == department_ids.len() {
            return Some(schedule);
        }
    }
    None
}

pub fn schedule_by_course_list(
    seed_data: &SeedData,
    mut course_ids: Vec<usize>,
) -> Option<Vec<Meeting>> {
    let mut rng = rand::thread_rng();

    let mut courses_added = 0;
    for _ in 0..3 {
        let rand_index = rng.gen::<usize>() % course_ids.len();
        course_ids.rotate_right(rand_index);
        let mut schedule = Vec::new();
        for id in &course_ids {
            let mut meetings_in_course: Vec<&Meeting> = seed_data
                .meetings
                .iter()
                .filter(|meeting| meeting.course_id == *id)
                .collect();

            let mut course = get_course(&schedule, &mut meetings_in_course);
            if course.len() > 0 {
                schedule.append(&mut course);
                courses_added += 1;
            }
        }
        if courses_added == course_ids.len() {
            return Some(schedule);
        }
    }
    None
}

fn get_course_from_dept(
    schedule: &Vec<Meeting>,
    seed_data: &SeedData,
    dept_id: usize,
) -> Vec<Meeting> {
    let mut courses_in_dept = HashSet::new();
    for (course_id, course) in seed_data.courses.iter().enumerate() {
        if course.department_id == dept_id {
            courses_in_dept.insert(course_id);
        }
    }

    let mut meetings_in_course: Vec<&Meeting> = seed_data
        .meetings
        .iter()
        .filter(|meeting| courses_in_dept.contains(&meeting.course_id))
        .collect();
    get_course(schedule, &mut meetings_in_course)
}

fn get_course(schedule: &Vec<Meeting>, meetings_in_course: &mut Vec<&Meeting>) -> Vec<Meeting> {
    let mut rng = rand::thread_rng();
    meetings_in_course.shuffle(&mut rng);
    for meeting in meetings_in_course {
        // Checks if given meeting fits into schedule
        if schedule
            .iter()
            .all(|sched_meeting| !does_overlap(sched_meeting, meeting))
        {
            let recitation = meeting.recitations.iter().find(|recit| {
                schedule
                    .iter()
                    .all(|sched_meeting| !does_overlap(sched_meeting, recit))
            });

            return match recitation {
                Some(recit) => vec![recit.clone(), meeting.clone()],
                None => vec![meeting.clone()],
            };
        }
    }
    Vec::new()
}

fn does_overlap(m1: &Meeting, m2: &Meeting) -> bool {
    // If the days don't overlap at all (Mon/Wed and Tues/Thurs)
    // Then we can just return false
    if m1.days.0 != m2.days.0
        && m1.days.0 != m2.days.1
        && m1.days.1 != m2.days.0
        && m1.days.1 != m2.days.1
    {
        return false;
    }
    m1.start_time <= m2.end_time && m2.start_time <= m1.end_time
}
