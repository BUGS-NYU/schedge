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
  name                varchar(128)                NOT NULL,
  name_vec            TSVECTOR                    NOT NULL,
  school              varchar(4)                  NOt NULL,
  subject             varchar(8)                  NOT NULL, -- could probably be lower
  dept_course_id      varchar(6)                  NOT NULL,
  term_id             integer                     NOT NULL,
  description         text,
  description_vec     TSVECTOR,
  PRIMARY KEY (id)
);

CREATE TABLE instructors (
  id                  SERIAL                      NOT NULL,
  name                varchar(64)                 NOT NULL,
  school              varchar(4)                  NOT NULL,
  subject             varchar(6)                  NOT NULL,
  rmp_rating          real,
  rmp_tid             integer,
  PRIMARY KEY (id)
);


CREATE TABLE sections (
  id                  SERIAL                      NOT NULL,
  registration_number integer                     NOT NULL,
  course_id           int REFERENCES courses(id)
                          ON DELETE CASCADE       NOT NULL,
  section_code        varchar(5)                  NOT NULL,
  section_type        integer                     NOT NULL,
  section_status      integer                     NOT NULL,
  associated_with     integer REFERENCES sections(id),

  waitlist_total      integer,
  name                text,
  name_vec            TSVECTOR,
  min_units           float,
  max_units           float,
  campus              varchar(100),
  notes               text,
  notes_vec           TSVECTOR,
  instruction_mode    varchar(32),
  grading             varchar(48),
  location            varchar(128),
  prerequisites       text,
  prereqs_vec         TSVECTOR,

  PRIMARY KEY (id)
);

CREATE TABLE is_teaching_section (
  id                  SERIAL                      NOT NULL,
  instructor_id       integer                     NOT NULL,
  section_id          integer                     NOT NULL,
  instructor_name     varchar(64)                 NOT NULL,
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

CREATE TABLE reviews (
  id                  SERIAL                      NOT NULL,
  review_id           int REFERENCES instructors(id)
                          ON DELETE CASCADE       NOT NULL,
  review                text                      NOT NULL,
  PRIMARY KEY (id)
);



CREATE INDEX sections_associated_with ON sections (associated_with);
CREATE INDEX instructors_teaching_idx ON is_teaching_section (instructor_id);
CREATE INDEX sections_taught_idx ON is_teaching_section (section_id);