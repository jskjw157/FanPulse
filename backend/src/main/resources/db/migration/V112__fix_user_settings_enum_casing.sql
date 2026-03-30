-- Fix enum casing mismatch between JPA @Enumerated(EnumType.STRING)
-- and DB check constraints/defaults.
--
-- JPA stores Theme/Language enums as 'LIGHT'/'DARK' and 'KO'/'EN'.
-- Original V3 migration constrained values to lowercase ('light'/'dark', 'ko'/'en'),
-- which causes inserts to fail (e.g. during OAuth login default settings creation).

-- 1) Drop constraints (so we can rewrite existing values safely)
ALTER TABLE user_settings
  DROP CONSTRAINT IF EXISTS chk_user_settings_theme;

ALTER TABLE user_settings
  DROP CONSTRAINT IF EXISTS chk_user_settings_language;

-- 2) Normalize existing rows (if any)
UPDATE user_settings
SET theme = CASE lower(theme)
        WHEN 'light' THEN 'LIGHT'
        WHEN 'dark' THEN 'DARK'
        ELSE theme
    END,
    language = CASE lower(language)
        WHEN 'ko' THEN 'KO'
        WHEN 'en' THEN 'EN'
        ELSE language
    END;

-- 3) Align defaults with enum casing
ALTER TABLE user_settings
  ALTER COLUMN theme SET DEFAULT 'LIGHT';

ALTER TABLE user_settings
  ALTER COLUMN language SET DEFAULT 'KO';

-- 4) Recreate constraints to match enum casing

ALTER TABLE user_settings
  ADD CONSTRAINT chk_user_settings_theme CHECK (theme IN ('LIGHT', 'DARK')),
  ADD CONSTRAINT chk_user_settings_language CHECK (language IN ('KO', 'EN'));
