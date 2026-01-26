-- Artist members table for @ElementCollection
CREATE TABLE artist_members (
    artist_id UUID NOT NULL REFERENCES artists(id) ON DELETE CASCADE,
    member_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (artist_id, member_name)
);

CREATE INDEX idx_artist_members_artist_id ON artist_members(artist_id);
