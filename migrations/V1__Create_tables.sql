CREATE TABLE public.courses (
  id                  SERIAL        NOT NULL UNIQUE,
  name                varchar(128)  NOT NULL,
  school              varchar(4)    NOt NULL,
  subject             varchar(6)    NOT NULL,
  dept_course_id      varchar(6)    NOT NULL,
  term_id             integer       NOT NULL,
  PRIMARY KEY (term_id, school, subject, dept_course_id)
);

CREATE TABLE public.sections (
  id                  SERIAL                      NOT NULL UNIQUE,
  registration_number integer                     NOT NULL,
  course_id           int REFERENCES courses(id)
                          ON DELETE CASCADE       NOT NULL,
  section_code        varchar(5)                  NOT NULL,
  instructor          text                        NOT NULL,
  section_type        integer                     NOT NULL,
  section_status      integer                     NOT NULL,
  associated_with     integer REFERENCES sections(id),

  waitlist_total      integer,
  section_name        varchar(128),
  min_units           float,
  max_units           float,
  campus              varchar(100),
  description         text,
  instruction_mode    varchar(32),
  grading             varchar(25),
  room_number         varchar(64),
  prerequisites       varchar,

  PRIMARY KEY (course_id, section_code)
);

CREATE TABLE public.meetings (
  id                  SERIAL                          NOT NULL,
  section_id          int REFERENCES sections(id)
                      ON DELETE CASCADE               NOT NULL,
  begin_date          timestamp without time zone     NOT NULL,
  end_date            timestamp without time zone     NOT NULL,
  duration            bigint                          NOT NULL,
  PRIMARY KEY (id)
);


CREATE UNIQUE INDEX course_idx ON courses (id);
CREATE UNIQUE INDEX section_idx ON sections (id);
CREATE INDEX sections_associated_with ON sections (associated_with);