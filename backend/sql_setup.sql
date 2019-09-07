CREATE TABLE courses (
  term int,
  crn int,
  term_description varchar,
  sec_num varchar,
  c_num int,
  c_name varchar,
  sub varchar,
  subject_description varchar,
  schol_code varchar,
  school_name varchar,
  max_cred float,
  min_cred float,
  course_or_section_description varchar,
  section_topic varchar,
  section_type varchar,
  section_type_description varchar,
  class_type varchar,
  class_type_description varchar,
  reg_nums varchar,
  prerequisites_and_notes varchar
);

-- Need to put these in the data directory
\COPY courses FROM 'nyu_courses.csv' DELIMITER ',' CSV;
-- \COPY courses FROM 'nyu_courses_2.csv' DELIMITER ',' CSV;
-- \COPY courses FROM 'nyu_courses_3.csv' DELIMITER ',' CSV;

ALTER TABLE courses DROP COLUMN term_description,
                    DROP COLUMN subject_description,
                    DROP COLUMN school_name,
                    DROP COLUMN course_or_section_description,
                    DROP COLUMN section_type_description,
                    DROP COLUMN class_type_description,
                    DROP COLUMN prerequisites_and_notes;

CREATE TABLE course_meetings (
  term int,
  reg_num int,
  class_meet_num int,
  days varchar,
  times_start time,
  times_end time,
  bldg varchar,
  bldg_desc varchar,
  room varchar
);

-- df = df.replace(r'^\s*$', pd.np.nan, regex=True)
-- df['TimesStart'] = pd.to_datetime(df['TimesStart'], format="%I.%M %p")
-- df['TimesEnd'] = pd.to_datetime(df['TimesEnd'], format="%I.%M %p")
\COPY course_meetings FROM 'nyu_course_meetings.csv' DELIMITER ',' CSV;

