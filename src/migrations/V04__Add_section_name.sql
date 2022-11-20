-- Nullable, since not all sections have a name
ALTER TABLE sections ADD COLUMN name varchar;
