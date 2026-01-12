-- K-Pop 아티스트 및 YouTube 채널 시드 데이터
-- 크롤러가 수집할 채널 목록 (live_concert_collector.py HOT_ARTISTS 기반)

-- 아티스트 시드 (이미 존재하는 아티스트는 건너뜀)
INSERT INTO artists (id, name, created_at)
SELECT gen_random_uuid(), artist_name, NOW()
FROM (VALUES
    -- 4세대 걸그룹
    ('NewJeans'),
    ('aespa'),
    ('IVE'),
    ('LE SSERAFIM'),
    ('(G)I-DLE'),
    ('ITZY'),

    -- 3세대 걸그룹
    ('BLACKPINK'),
    ('TWICE'),
    ('Red Velvet'),

    -- 4세대 보이그룹
    ('Stray Kids'),
    ('ENHYPEN'),
    ('TXT'),
    ('ATEEZ'),
    ('THE BOYZ'),

    -- 3세대 보이그룹
    ('SEVENTEEN'),
    ('NCT DREAM'),
    ('NCT 127'),
    ('EXO'),

    -- 솔로 아티스트
    ('IU'),

    -- 기타 인기 그룹
    ('RIIZE'),
    ('Kep1er'),

    -- 소속사 통합 채널 (BTS 등 포함)
    ('HYBE LABELS')
) AS seed(artist_name)
WHERE NOT EXISTS (SELECT 1 FROM artists WHERE name = seed.artist_name);

-- YouTube 채널 시드 (이미 존재하는 채널은 활성화만)
INSERT INTO artist_channels (id, artist_id, platform, channel_handle, is_active, created_at)
SELECT
    gen_random_uuid(),
    a.id,
    'YOUTUBE',
    c.channel_handle,
    true,
    NOW()
FROM (VALUES
    -- 4세대 걸그룹
    ('NewJeans', '@NewJeans_official'),
    ('aespa', '@aespa'),
    ('IVE', '@IVEstarship'),
    ('LE SSERAFIM', '@le_sserafim'),
    ('(G)I-DLE', '@G_I_DLE'),
    ('ITZY', '@ITZY'),

    -- 3세대 걸그룹
    ('BLACKPINK', '@BLACKPINK'),
    ('TWICE', '@TWICE'),
    ('Red Velvet', '@RedVelvet'),

    -- 4세대 보이그룹
    ('Stray Kids', '@StrayKids'),
    ('ENHYPEN', '@ENHYPEN'),
    ('TXT', '@TOMORROW_X_TOGETHER'),
    ('ATEEZ', '@ATEEZofficial'),
    ('THE BOYZ', '@the_boyz'),

    -- 3세대 보이그룹
    ('SEVENTEEN', '@pledis17'),
    ('NCT DREAM', '@NCTDREAM'),
    ('NCT 127', '@NCTsmtown'),
    ('EXO', '@weareone.EXO'),

    -- 솔로 아티스트
    ('IU', '@dlwlrma'),

    -- 기타 인기 그룹
    ('RIIZE', '@RIIZE_official'),
    ('Kep1er', '@official_kep1er'),

    -- 소속사 통합 채널 (BTS, TXT, ENHYPEN, SEVENTEEN 등 포함)
    ('HYBE LABELS', '@HYBELABELS')
) AS c(artist_name, channel_handle)
JOIN artists a ON a.name = c.artist_name
ON CONFLICT (platform, channel_handle) DO UPDATE SET
    is_active = true;
