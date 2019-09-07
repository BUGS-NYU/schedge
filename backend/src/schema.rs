table! {
    courses (id) {
        id -> Int4,
        name -> Varchar,
        code -> Varchar,
        department_id -> Int4,
    }
}

table! {
    departments (id) {
        id -> Int4,
        code -> Varchar,
        name -> Varchar,
    }
}

table! {
    meetings (id) {
        id -> Int4,
        days -> Array<Int4>,
        start_time -> Int4,
        end_time -> Int4,
        professor -> Varchar,
        course_id -> Int4,
    }
}

joinable!(courses -> departments (department_id));

allow_tables_to_appear_in_same_query!(
    courses,
    departments,
    meetings,
);
