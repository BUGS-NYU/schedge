UPDATE schedge_meta SET
  updated_at = NOW(),
  value = '2'
WHERE name = 'version';

CREATE INDEX courses_term_idx ON courses (term);

CREATE TABLE schools (
  id                  SERIAL                      NOT NULL UNIQUE,
  term                varchar                     NOT NULL,
  code                varchar                     NOT NULL,
  name                varchar                     NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE subjects (
  id                  SERIAL                      NOT NULL UNIQUE,
  term                varchar                     NOT NULL,
  code                varchar                     NOT NULL,
  name                varchar                     NOT NULL,
  PRIMARY KEY (id)
);
