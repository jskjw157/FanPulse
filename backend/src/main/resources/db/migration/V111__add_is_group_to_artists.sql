-- Add is_group column to artists table
ALTER TABLE artists ADD COLUMN is_group BOOLEAN NOT NULL DEFAULT false;
