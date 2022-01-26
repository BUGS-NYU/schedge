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
  subject             varchar                     NOT NULL,
  dept_course_id      varchar                     NOT NULL,
  term_id             integer                     NOT NULL,
  description         varchar,
  description_vec     TSVECTOR,
  PRIMARY KEY (id)
);

CREATE TABLE instructors (
  id                  SERIAL                      NOT NULL,
  name                varchar                     NOT NULL,
  school              varchar                     NOT NULL,
  subject             varchar                     NOT NULL,
  rmp_rating          real,
  rmp_tid             integer,
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
  begin_date          timestamp with time zone        NOT NULL,
  end_date            timestamp with time zone        NOT NULL,
  duration            integer                         NOT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX epochs_term_idx ON epochs (term_id);
CREATE INDEX courses_epoch_idx ON courses (epoch);
CREATE INDEX sections_registration_number_idx ON sections (registration_number);
CREATE INDEX sections_course_id_idx ON courses (course_id);
CREATE INDEX meetings_section_id_idx ON meetings (section_id);

CREATE INDEX sections_associated_with_idx ON sections (associated_with);
CREATE INDEX instructors_teaching_idx ON is_teaching_section (instructor_id);
CREATE INDEX sections_taught_idx ON is_teaching_section (section_id);
