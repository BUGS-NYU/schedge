CREATE TABLE instructors (
  id                  integer                     NOT NULL,
  name                varchar(64)                 NOT NULL,
  school              varchar(4)                  NOT NULL,
  subject             varchar(6)                  NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE is_teaching_section (
  id                  integer                     NOT NULL,
  instructor_id       integer                     NOT NULL,
  section_id          integer                     NOT NULL,
  PRIMARY KEY (id)
);


