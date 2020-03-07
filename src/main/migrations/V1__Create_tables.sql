CREATE TABLE epochs (
  id                  integer                       NOT NULL UNIQUE,
  started_at          timestamp                     NOT NULL UNIQUE,
  completed_at        timestamp                     UNIQUE,
  term_id             integer                       NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE courses (
  id                  integer                     NOT NULL,
  epoch               int REFERENCES epochs(id)
                          ON DELETE CASCADE       NOT NULL,
  name                varchar(128)                NOT NULL,
  school              varchar(4)                  NOt NULL,
  subject             varchar(6)                  NOT NULL,
  dept_course_id      varchar(6)                  NOT NULL,
  term_id             integer                     NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE instructors (
  id                  integer                     NOT NULL,
  name                varchar(64)                 NOT NULL,
  school              varchar(4)                  NOT NULL,
  subject             varchar(6)                  NOT NULL,
  rmp_rating          real,
  rmp_tid             integer,
  PRIMARY KEY (id)
);

CREATE TABLE sections (
  id                  integer                     NOT NULL,
  registration_number integer                     NOT NULL,
  course_id           int REFERENCES courses(id)
                          ON DELETE CASCADE       NOT NULL,
  section_code        varchar(5)                  NOT NULL,
  section_type        integer                     NOT NULL,
  section_status      integer                     NOT NULL,
  associated_with     integer REFERENCES sections(id),

  waitlist_total      integer,
  name                text,
  min_units           float,
  max_units           float,
  campus              varchar(100),
  description         text,
  notes               text,
  instruction_mode    varchar(32),
  grading             varchar(48),
  location            varchar(128),
  prerequisites       varchar,

  PRIMARY KEY (id)
);

CREATE TABLE is_teaching_section (
  id                  integer                     NOT NULL,
  instructor_id       integer                     NOT NULL,
  section_id          integer                     NOT NULL,
  instructor_name     varchar(64)                 NOT NULL,
  PRIMARY KEY (id)
);

-- CREATE TABLE is_teaching_subject (
--   id                  integer                     NOT NULL,
--   instructor_id       integer                     NOT NULL,
--   school              varchar(4)                  NOT NULL,
--   subject             varchar(6)                  NOT NULL,
--   PRIMARY KEY (id)
-- );

CREATE TABLE meetings (
  id                  integer                         NOT NULL,
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