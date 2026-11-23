-- Run once against the identity database. Existing accounts are kept intact;
-- the NOT VALID constraints still protect every newly inserted or updated row.
BEGIN;
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name text;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name text;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_first_name_present;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_last_name_present;
ALTER TABLE users
    ADD CONSTRAINT users_first_name_present
    CHECK (first_name IS NOT NULL AND length(btrim(first_name)) BETWEEN 1 AND 100) NOT VALID;
ALTER TABLE users
    ADD CONSTRAINT users_last_name_present
    CHECK (last_name IS NOT NULL AND length(btrim(last_name)) BETWEEN 1 AND 100) NOT VALID;
COMMIT;
