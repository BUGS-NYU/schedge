use crate::models::meeting::Meeting;
use std::collections::HashMap;

/// Compute a schedule based on the given meeting times
pub fn compute_schedule(meetings: &Vec<Meeting>, idx_list: Vec<usize>) -> Option<Vec<Meeting>> {
    let mut progress = Vec::new();
    if compute_schedule_rec(meetings, &idx_list, 0, &mut progress) {
        Some(progress.into_iter().map(|course| course.clone()).collect())
    } else {
        None
    }
}

/// Recursive subroutine of `compute_schedule`
fn compute_schedule_rec<'a>(
    meetings: &'a Vec<Meeting>,
    meeting_keys: &Vec<usize>,
    idx: usize,
    progress: &'a mut Vec<&'a Meeting>,
) -> bool {
    if idx >= meeting_keys.len() {
        true
    } else {
        for course in &meetings[meeting_keys[idx]] {
            let mut iter = progress.iter();
            let valid = loop {
                if let Some(prev) = iter.next() {
                    if does_overlap(prev, course) {
                        break false;
                    }
                } else {
                    // We've gone through them all; we're done, and there's no conflict!
                    break true;
                }
            };

            if valid {
                progress.push(course);
                if compute_schedule_rec(meetings, meeting_keys, idx + 1, progress) {
                    return true;
                }
                progress.pop();
            }
        }

        false
    }
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
