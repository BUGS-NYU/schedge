CREATE TABLE courses (
  id SERIAL PRIMARY KEY,
  name VARCHAR NOT NULL,
  code VARCHAR NOT NULL,
  department_id INTEGER REFERENCES departments(id) NOT NULL
)
