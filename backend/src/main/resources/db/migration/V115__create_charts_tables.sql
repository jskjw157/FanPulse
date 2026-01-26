-- Charts Aggregate table
CREATE TABLE charts (
    id UUID PRIMARY KEY,
    chart_type VARCHAR(30) NOT NULL,
    chart_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_chart_type_date UNIQUE (chart_type, chart_date)
);

-- Chart Entries table
CREATE TABLE chart_entries (
    id UUID PRIMARY KEY,
    chart_id UUID NOT NULL REFERENCES charts(id) ON DELETE CASCADE,
    rank INT NOT NULL,
    track_id UUID NOT NULL,
    artist_id UUID NOT NULL,
    track_title VARCHAR(255) NOT NULL,
    artist_name VARCHAR(100) NOT NULL,
    previous_rank INT,
    peak_rank INT NOT NULL,
    weeks_on_chart INT NOT NULL
);

CREATE INDEX idx_chart_entries_chart_id ON chart_entries(chart_id);
CREATE INDEX idx_chart_entries_artist_id ON chart_entries(artist_id);
