-- Keep unmatched Apple Music chart rows and official artwork.
ALTER TABLE chart_entries
    ALTER COLUMN artist_id DROP NOT NULL;

ALTER TABLE chart_entries
    ALTER COLUMN artist_name TYPE VARCHAR(255);

ALTER TABLE chart_entries
    ADD COLUMN artwork_url VARCHAR(512);
