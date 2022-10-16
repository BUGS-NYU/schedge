CREATE TABLE schedge_meta (
  id                  SERIAL                        NOT NULL UNIQUE,
  created_at          timestamp WITHOUT TIME ZONE   NOT NULL DEFAULT NOW(),
  updated_at          timestamp WITHOUT TIME ZONE   NOT NULL,
  name                varchar                       NOT NULL UNIQUE,
  value               varchar                       NOT NULL,
  PRIMARY KEY (id)
);

INSERT  INTO schedge_meta (updated_at, name,       value)
        VALUES            (NOW(),      'version',  '1');

CREATE TABLE courses (
  id                  SERIAL                      NOT NULL UNIQUE,
  term                varchar                     NOT NULL,
  name                varchar                     NOT NULL,
  subject_code        varchar                     NOT NULL,
  dept_course_id      varchar                     NOT NULL,
  description         varchar                     NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE sections (
  id                  SERIAL                          NOT NULL UNIQUE,
  registration_number int                             NOT NULL,

  course_id           int REFERENCES courses(id)
                          ON DELETE CASCADE           NOT NULL,

  section_code        varchar                         NOT NULL,
  section_type        varchar                         NOT NULL,
  section_status      varchar                         NOT NULL,
  min_units           float                           NOT NULL,
  max_units           float                           NOT NULL,
  campus              varchar                         NOT NULL,
  notes               varchar                         NOT NULL,
  instruction_mode    varchar                         NOT NULL,
  grading             varchar                         NOT NULL,
  instructors         varchar[]                       NOT NULL,

  associated_with     int REFERENCES sections(id),
  location            varchar,
  waitlist_total      int,

  PRIMARY KEY (id)
);

CREATE TABLE meetings (
  id                  SERIAL                          NOT NULL UNIQUE,
  section_id          int REFERENCES sections(id)
                      ON DELETE CASCADE               NOT NULL,
  begin_date          timestamp WITHOUT TIME ZONE     NOT NULL,
  end_date            timestamp WITHOUT TIME ZONE     NOT NULL,
  duration            int                             NOT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX courses_term_idx ON courses (term);
CREATE INDEX sections_course_id_idx ON sections (course_id);
CREATE INDEX meetings_section_id_idx ON meetings (section_id);

CREATE INDEX sections_registration_number_idx ON sections (registration_number);
CREATE INDEX sections_associated_with_idx ON sections (associated_with);
