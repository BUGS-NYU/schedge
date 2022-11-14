ALTER TABLE courses ADD COLUMN meta_vector tsvector GENERATED ALWAYS
    AS(to_tsvector('english', subject_code || ' ' || dept_course_id)) STORED;

ALTER TABLE courses ADD COLUMN name_vector tsvector GENERATED ALWAYS
    AS(to_tsvector('english', name)) STORED;

ALTER TABLE courses ADD COLUMN description_vector tsvector GENERATED ALWAYS
    AS(to_tsvector('english', description)) STORED;

ALTER TABLE sections ADD COLUMN meta_vector tsvector GENERATED ALWAYS
    AS(to_tsvector('english', registration_number::text)) STORED;

ALTER TABLE sections ADD COLUMN notes_vector tsvector GENERATED ALWAYS
    AS(to_tsvector('english', notes)) STORED;

CREATE INDEX courses_meta_vector_index ON courses USING GIN (meta_vector);
CREATE INDEX courses_name_vector_index ON courses USING GIN (name_vector);
CREATE INDEX courses_description_vector_index ON courses USING GIN (description_vector);
CREATE INDEX sections_meta_vector_index ON sections USING GIN (meta_vector);
CREATE INDEX sections_notes_vector_index ON sections USING GIN (notes_vector);