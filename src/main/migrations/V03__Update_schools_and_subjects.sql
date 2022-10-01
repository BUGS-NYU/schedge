ALTER TABLE schools DROP COLUMN code;

ALTER TABLE subjects DROP COLUMN school;

ALTER TABLE subjects
  ADD COLUMN school integer NOT NULL
  REFERENCES schools(id)
  ON DELETE CASCADE;
