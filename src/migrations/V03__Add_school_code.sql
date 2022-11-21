-- Nullable to allow for previous column values which don't exist
ALTER TABLE schools ADD COLUMN code varchar;
