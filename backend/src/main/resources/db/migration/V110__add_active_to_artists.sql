-- Add active column to artists table
ALTER TABLE artists ADD COLUMN active BOOLEAN NOT NULL DEFAULT true;
