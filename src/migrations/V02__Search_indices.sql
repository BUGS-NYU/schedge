ALTER TABLE courses ADD COLUMN course_vec tsvector GENERATED ALWAYS AS (
   setweight(to_tsvector('english', subject_code || ' ' || dept_course_id), 'A')
   || setweight(to_tsvector('english', name), 'B')
   || setweight(to_tsvector('english', description), 'C')
) STORED;

ALTER TABLE sections ADD COLUMN section_vec tsvector GENERATED ALWAYS AS (
    setweight(to_tsvector('english', registration_number::text), 'D')
) STORED;

CREATE INDEX courses_vector_index ON courses USING GIN (course_vec);
CREATE INDEX sections_vector_index ON sections USING GIN (section_vec);