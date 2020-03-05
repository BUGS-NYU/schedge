# Database Schema
This document discusses the thing underpinning this entire project: the database.

### The View from 10,000 Feet
Our schema tries to hold course and instructor data; that means we have a course
table for courses, a section table for sections in those courses, and a meetings
table for times that each of those sections meets. Since scraping this data can
take a long time, and we don't want the end user to see an invalid state, we use
epochs to state what version of the data we're looking at, as well as whether or not
that data is valid.

### The Actual Schema
Below is the actual schema, annotated with invariants that we hold when accessing
the database:

```sql
CREATE TABLE epochs ( -- Epochs are time intervals when we were scraping
  id                integer     NOT NULL UNIQUE,
  started_at        timestamp   NOT NULL UNIQUE,
  completed_at      timestamp   UNIQUE, -- When an epoch is completed, it will
                                        -- set this to a non-null value, and
                                        -- all of its data should be served
  term_id           integer     NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE courses (
  id                integer                     NOT NULL,
  epoch             int REFERENCES epochs(id)
                        ON DELETE CASCADE       NOT NULL,
  name              varchar(128)                NOT NULL,
  school            varchar(4)                  NOt NULL, -- School code: e.g. UA
  subject           varchar(6)                  NOT NULL, -- Subject code: e.g. CSCI
  dept_course_id    varchar(6)                  NOT NULL, -- i.e. the 102 in CSCI-UA 102 Data Structures
  term_id           integer                     NOT NULL, -- The term id, which is the value of term.getId()
                                                          -- where term is an instance of nyu.Term
  PRIMARY KEY (id)
);

CREATE TABLE instructors (
  id                  integer                     NOT NULL,
  name                varchar(64)                 NOT NULL,
  school              varchar(4)                  NOT NULL,
  subject             varchar(6)                  NOT NULL,
  rmp_rating          real,
  rmp_tid             integer, -- The teacher id in RateMyProfessor for this instructor.
  PRIMARY KEY (id)
);

CREATE TABLE sections ( -- Sections, i.e. lecture/recitation
  id                  integer                     NOT NULL,
  registration_number integer                     NOT NULL,
  course_id           int REFERENCES courses(id)
                          ON DELETE CASCADE       NOT NULL,
  section_code        varchar(5)                  NOT NULL,

  -- More fields here

  section_type        integer                     NOT NULL,
  prerequisites       varchar,

  PRIMARY KEY (id)
);

CREATE TABLE is_teaching_section ( -- Whether or not a professor is teaching a section.
  id                  integer                     NOT NULL,
  instructor_id       integer                     NOT NULL,
  section_id          integer                     NOT NULL,
  instructor_name     varchar(64)                 NOT NULL, -- This reduces load times
                                                            -- by allowing us to
                                                            -- skip reading the instructors
                                                            -- table when selecting sections.
  PRIMARY KEY (id)
);

CREATE TABLE meetings ( -- Times a section meets
  id                  integer                         NOT NULL,
  section_id          int REFERENCES sections(id)
                      ON DELETE CASCADE               NOT NULL,
  begin_date          timestamp                       NOT NULL,
  end_date            timestamp                       NOT NULL,
  duration            bigint                          NOT NULL,
  PRIMARY KEY (id)
);


-- These indices improve performance. Or so we hope.
CREATE INDEX sections_associated_with ON sections (associated_with);
CREATE INDEX instructors_teaching_idx ON is_teaching_section (instructor_id);
CREATE INDEX sections_taught_idx ON is_teaching_section (section_id);
```


