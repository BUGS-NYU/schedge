ALTER TABLE schools DROP COLUMN code;

ALTER TABLE subjects DROP COLUMN school;

ALTER TABLE subjects
  ADD COLUMN school integer
  REFERENCES schools(id)
  ON DELETE CASCADE;
