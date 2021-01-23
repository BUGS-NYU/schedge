CREATE TABLE epochs (
  id                  SERIAL                        NOT NULL UNIQUE,
  started_at          timestamp WITHOUT TIME ZONE   NOT NULL UNIQUE,
  completed_at        timestamp WITHOUT TIME ZONE   UNIQUE,
  term_id             integer                       NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE courses (
  id                  SERIAL                      NOT NULL,
  epoch               int REFERENCES epochs(id)
                          ON DELETE CASCADE       NOT NULL,
  name                varchar                     NOT NULL,
  name_vec            TSVECTOR                    NOT NULL,
  school              varchar                     NOt NULL,
  subject             varchar                     NOT NULL, -- could probably be lower
  dept_course_id      varchar                     NOT NULL,
  term_id             integer                     NOT NULL,
  description         varchar,
  description_vec     TSVECTOR,
  PRIMARY KEY (id)
);

CREATE TABLE instructors (
  id                  SERIAL                      NOT NULL,
  name                varchar                     NOT NULL,
  name_vec            TSVECTOR,
  school              varchar                     NOT NULL,
  subject             varchar                     NOT NULL,
  rmp_rating          real,
  rmp_tid             integer,
  PRIMARY KEY (id)
);

CREATE TABLE ratings (
  id                  SERIAL                      NOT NULL,
  instructor_id       int REFERENCES instructors(id)
                        ON DELETE CASCADE         NOT NULL,
  comment             varchar                    NOT NULL,
  rmp_rating          real,
  PRIMARY KEY (id)
);

CREATE TABLE sections (
  id                  SERIAL                      NOT NULL,
  registration_number integer                     NOT NULL,
  course_id           int REFERENCES courses(id)
                          ON DELETE CASCADE       NOT NULL,
  section_code        varchar                     NOT NULL,
  section_type        integer                     NOT NULL,
  section_status      integer                     NOT NULL,
  associated_with     integer REFERENCES sections(id),

  waitlist_total      integer,
  name                varchar,
  name_vec            TSVECTOR,
  min_units           float,
  max_units           float,
  campus              varchar,
  notes               varchar,
  notes_vec           TSVECTOR,
  instruction_mode    varchar,
  grading             varchar,
  location            varchar,
  prerequisites       varchar,
  prereqs_vec         TSVECTOR,

  PRIMARY KEY (id)
);

CREATE TABLE is_teaching_section (
  id                  SERIAL                      NOT NULL,
  instructor_id       integer                     NOT NULL,
  section_id          integer                     NOT NULL,
  instructor_name     varchar                     NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE meetings (
  id                  SERIAL                          NOT NULL,
  section_id          int REFERENCES sections(id)
                      ON DELETE CASCADE               NOT NULL,
  begin_date          timestamp                       NOT NULL,
  end_date            timestamp                       NOT NULL,
  duration            bigint                          NOT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX sections_associated_with ON sections (associated_with);
CREATE INDEX instructors_teaching_idx ON is_teaching_section (instructor_id);
CREATE INDEX sections_taught_idx ON is_teaching_section (section_id);
CREATE INDEX instructor_idx ON ratings(instructor_id);