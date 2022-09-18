-- This migration transitions Schedge off of hard-coded subject codes,
-- so that new semesters no longer require more programming.

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

CREATE INDEX schools_term_idx ON schools (term);
CREATE INDEX subjects_term_idx ON subjects (term);
