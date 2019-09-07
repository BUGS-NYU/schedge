CREATE TABLE meetings (
  id SERIAL PRIMARY KEY,
  days INTEGER[] NOT NULL,
  start_time INTEGER NOT NULL,
  end_time INTEGER NOT NULL,
  professor VARCHAR NOT NULL,
  course_id INTEGER NOT NULL
);
