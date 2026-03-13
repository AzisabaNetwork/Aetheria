-- Migration: add hologram_uuid nullable UUID to portals table
BEGIN;
ALTER TABLE portals ADD COLUMN IF NOT EXISTS hologram_uuid UUID NULL;
COMMIT;

-- Recommended: backup DB before applying this migration
-- pg_dump -Fc -f vanilife_backup.dump --dbname=<db-connection-string>
