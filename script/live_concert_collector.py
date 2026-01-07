#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
해외에서 핫한 K-Pop 아티스트들의 YouTube 라이브/콘서트 영상 수집 시스템
- 자막 추출 없이 메타데이터만으로 판단
- SQLite DB 저장
"""

import sqlite3
import yt_dlp
from datetime import datetime
import json

# ========================================
# 1. 해외에서 핫한 K-Pop 아티스트 목록 (2024-2025 기준)
# ========================================
HOT_ARTISTS = {
    # 4세대 걸그룹
    "NewJeans": "@NewJeans_official",
    "aespa": "@aespa",
    "IVE": "@IVEstarship",
    "LE SSERAFIM": "@le_sserafim",
    "(G)I-DLE": "@G_I_DLE",
    "ITZY": "@ITZY",
    
    # 3세대 걸그룹
    "BLACKPINK": "@BLACKPINK",
    "TWICE": "@TWICE",
    "Red Velvet": "@RedVelvet",
    
    # 4세대 보이그룹
    "Stray Kids": "@StrayKids",
    "ENHYPEN": "@ENHYPEN",
    "TXT": "@TOMORROW_X_TOGETHER",
    "ATEEZ": "@ATEEZofficial",
    "THE BOYZ": "@the_boyz",
    
    # 3세대 보이그룹
    "BTS": "@bts_bighit",
    "SEVENTEEN": "@pledis17",
    "NCT DREAM": "@NCTDREAM",
    "NCT 127": "@NCTsmtown",
    "EXO": "@weareone.EXO",
    
    # 솔로 아티스트
    "Jungkook": "@JungKook_BigHitEnt",
    "Jimin": "@Jimin_BigHitEnt",
    "V": "@BTS_twt",
    "Lisa": "@lalalalisa_m",
    "Jennie": "@jennierubyjane",
    "Rosé": "@roses_are_rosie",
    
    # 기타 인기 그룹
    "RIIZE": "@RIIZE_official",
    "Kep1er": "@official_kep1er",
    "IU": "@dlwlrma",
}

# ========================================
# 2. DB 스키마 설정
# ========================================
def init_database(db_path="live_concerts.db"):
    """SQLite DB 초기화 및 테이블 생성"""
    
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    # 아티스트 테이블
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS artists (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT UNIQUE NOT NULL,
            channel_username TEXT,
            channel_id TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    
    # 라이브 영상 테이블
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS live_videos (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            artist_id INTEGER,
            video_id TEXT UNIQUE NOT NULL,
            title TEXT NOT NULL,
            url TEXT NOT NULL,
            duration_seconds INTEGER,
            duration_formatted TEXT,
            upload_date TEXT,
            view_count INTEGER,
            like_count INTEGER,
            comment_count INTEGER,
            description TEXT,
            thumbnail_url TEXT,
            
            -- 공연 타입 판단
            is_concert BOOLEAN DEFAULT 0,
            is_live_stream BOOLEAN DEFAULT 0,
            is_festival BOOLEAN DEFAULT 0,
            confidence_score REAL,
            
            -- 메타 정보
            collected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            
            FOREIGN KEY (artist_id) REFERENCES artists(id)
        )
    """)
    
    # 인덱스 생성
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_artist ON live_videos(artist_id)")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_video_id ON live_videos(video_id)")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_upload_date ON live_videos(upload_date)")
    
    conn.commit()
    return conn

# ========================================
# 3. 아티스트 채널에서 라이브 영상 수집
# ========================================
def get_channel_live_videos(channel_username, limit=50):
    """아티스트 채널의 라이브 탭 영상 수집"""
    
    channel_url = f"https://www.youtube.com/{channel_username}/streams"
    
    ydl_opts = {
        'extract_flat': 'in_playlist',
        'skip_download': True,
        'quiet': True,
        'no_warnings': True,
        'playlistend': limit,
    }
    
    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            result = ydl.extract_info(channel_url, download=False)
            
            if not result or 'entries' not in result:
                return []
            
            videos = []
            for entry in result['entries']:
                if entry:
                    videos.append(entry)
            
            return videos
            
    except Exception as e:
        print(f"  ⚠️  오류: {e}")
        return []

# ========================================
# 4. 영상 상세 정보 가져오기
# ========================================
def get_video_details(video_id):
    """개별 영상의 상세 정보 수집"""
    
    url = f"https://www.youtube.com/watch?v={video_id}"
    
    ydl_opts = {
        'skip_download': True,
        'quiet': True,
        'no_warnings': True,
    }
    
    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=False)
            return info
    except Exception as e:
        print(f"    ⚠️  상세정보 추출 실패 ({video_id}): {e}")
        return None

# ========================================
# 5. 라이브 공연 타입 자동 판단 (자막 없이)
# ========================================
def classify_live_video(video_info):
    """
    메타데이터만으로 라이브 영상 타입 판단
    반환: (is_concert, is_live_stream, is_festival, confidence_score)
    """
    
    title = video_info.get('title', '').lower()
    description = video_info.get('description', '').lower()
    duration = video_info.get('duration', 0)
    
    # 키워드 점수 시스템
    score = 0
    is_concert = False
    is_live_stream = False
    is_festival = False
    
    # === 콘서트 키워드 ===
    concert_keywords = [
        'concert', 'tour', 'live at', 'world tour',
        'arena', 'stadium', 'dome', 'hall',
        'show', 'performance at'
    ]
    
    for keyword in concert_keywords:
        if keyword in title or keyword in description[:500]:
            is_concert = True
            score += 3
            break
    
    # === 페스티벌 키워드 ===
    festival_keywords = [
        'festival', 'fest', 'coachella', 'lollapalooza',
        'glastonbury', 'rolling loud', 'bonnaroo'
    ]
    
    for keyword in festival_keywords:
        if keyword in title or keyword in description[:500]:
            is_festival = True
            score += 2
            break
    
    # === 라이브 스트리밍 키워드 ===
    stream_keywords = [
        'live stream', 'livestream', 'premiere',
        'online concert', 'virtual concert',
        'live session', 'live performance from home'
    ]
    
    for keyword in stream_keywords:
        if keyword in title or keyword in description[:300]:
            is_live_stream = True
            score += 2
            break
    
    # === 제외할 키워드 (인터뷰, 비하인드 등) ===
    exclude_keywords = [
        'interview', 'behind the scenes', 'reaction',
        'unboxing', 'vlog', 'documentary', 'trailer'
    ]
    
    for keyword in exclude_keywords:
        if keyword in title:
            score -= 5
            break
    
    # === 영상 길이로 판단 ===
    # 실제 공연: 30분~4시간
    if 1800 <= duration <= 14400:
        score += 2
    # 짧은 클립: 5분 이하 (하이라이트일 가능성)
    elif duration < 300:
        score -= 2
    
    # === Confidence Score 계산 (0~1) ===
    confidence = min(max(score / 10.0, 0.0), 1.0)
    
    return is_concert, is_live_stream, is_festival, confidence

# ========================================
# 6. DB에 데이터 저장
# ========================================
def save_artist_to_db(conn, artist_name, channel_username):
    """아티스트 정보 저장"""
    cursor = conn.cursor()
    
    try:
        cursor.execute("""
            INSERT OR IGNORE INTO artists (name, channel_username)
            VALUES (?, ?)
        """, (artist_name, channel_username))
        
        conn.commit()
        
        # ID 가져오기
        cursor.execute("SELECT id FROM artists WHERE name = ?", (artist_name,))
        return cursor.fetchone()[0]
    
    except Exception as e:
        print(f"  ⚠️  아티스트 저장 실패: {e}")
        return None

def save_video_to_db(conn, artist_id, video_info, classification):
    """라이브 영상 정보 DB에 저장"""
    
    cursor = conn.cursor()
    
    is_concert, is_live_stream, is_festival, confidence = classification
    
    # Duration formatting
    duration_sec = video_info.get('duration', 0)
    hours = duration_sec // 3600
    minutes = (duration_sec % 3600) // 60
    seconds = duration_sec % 60
    
    if hours > 0:
        duration_formatted = f"{hours}:{minutes:02d}:{seconds:02d}"
    else:
        duration_formatted = f"{minutes}:{seconds:02d}"
    
    # Upload date formatting
    upload_date_raw = video_info.get('upload_date', '')
    if upload_date_raw:
        upload_date = f"{upload_date_raw[:4]}-{upload_date_raw[4:6]}-{upload_date_raw[6:]}"
    else:
        upload_date = None
    
    try:
        cursor.execute("""
            INSERT OR REPLACE INTO live_videos (
                artist_id, video_id, title, url,
                duration_seconds, duration_formatted, upload_date,
                view_count, like_count, comment_count,
                description, thumbnail_url,
                is_concert, is_live_stream, is_festival, confidence_score,
                last_updated
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """, (
            artist_id,
            video_info['id'],
            video_info.get('title', ''),
            video_info.get('webpage_url', ''),
            duration_sec,
            duration_formatted,
            upload_date,
            video_info.get('view_count', 0),
            video_info.get('like_count', 0),
            video_info.get('comment_count', 0),
            video_info.get('description', '')[:1000],  # 1000자 제한
            video_info.get('thumbnail', ''),
            is_concert,
            is_live_stream,
            is_festival,
            confidence
        ))
        
        conn.commit()
        return True
        
    except Exception as e:
        print(f"    ⚠️  영상 저장 실패: {e}")
        return False

# ========================================
# 7. 메인 수집 함수
# ========================================
def collect_all_artists_live_videos(db_path="live_concerts.db", limit_per_artist=50):
    """모든 K-Pop 아티스트의 라이브 영상 수집 및 DB 저장"""
    
    print("=" * 70)
    print("🎵 K-Pop 라이브 콘서트 영상 수집 시스템")
    print("=" * 70)
    print(f"📊 대상 K-Pop 아티스트: {len(HOT_ARTISTS)}명")
    print(f"💾 DB 저장 경로: {db_path}")
    print()
    
    # DB 초기화
    conn = init_database(db_path)
    
    total_videos = 0
    total_concerts = 0
    
    for idx, (artist_name, channel_username) in enumerate(HOT_ARTISTS.items(), 1):
        print(f"[{idx}/{len(HOT_ARTISTS)}] {artist_name}")
        print(f"  └─ 채널: {channel_username}")
        
        # 아티스트 DB 저장
        artist_id = save_artist_to_db(conn, artist_name, channel_username)
        if not artist_id:
            continue
        
        # 라이브 탭 영상 목록 가져오기
        print(f"  └─ 라이브 영상 수집 중...", end=" ")
        videos = get_channel_live_videos(channel_username, limit=limit_per_artist)
        print(f"✓ {len(videos)}개 발견")
        
        if not videos:
            print()
            continue
        
        # 각 영상 상세 정보 수집 및 분류
        concert_count = 0
        for video in videos:
            video_id = video.get('id')
            if not video_id:
                continue
            
            # 상세 정보 가져오기
            video_info = get_video_details(video_id)
            if not video_info:
                continue
            
            # 라이브 타입 분류
            classification = classify_live_video(video_info)
            is_concert, is_live_stream, is_festival, confidence = classification
            
            # Confidence 0.3 이상만 저장
            if confidence >= 0.3:
                save_video_to_db(conn, artist_id, video_info, classification)
                total_videos += 1
                
                if is_concert or is_festival:
                    concert_count += 1
                    total_concerts += 1
        
        print(f"  └─ 저장 완료: {concert_count}개 콘서트/페스티벌 영상")
        print()
    
    conn.close()
    
    print("=" * 70)
    print("✅ 수집 완료!")
    print(f"📊 총 저장된 영상: {total_videos}개")
    print(f"🎤 콘서트/페스티벌: {total_concerts}개")
    print("=" * 70)

# ========================================
# 8. DB 조회 헬퍼 함수들
# ========================================
def query_concerts_by_artist(db_path, artist_name):
    """특정 아티스트의 콘서트 영상 조회"""
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT v.title, v.url, v.duration_formatted, v.view_count, v.upload_date
        FROM live_videos v
        JOIN artists a ON v.artist_id = a.id
        WHERE a.name = ? AND (v.is_concert = 1 OR v.is_festival = 1)
        ORDER BY v.upload_date DESC
    """, (artist_name,))
    
    results = cursor.fetchall()
    conn.close()
    
    return results

def get_top_viewed_concerts(db_path, limit=10):
    """조회수 높은 콘서트 영상 TOP N"""
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT a.name, v.title, v.view_count, v.url
        FROM live_videos v
        JOIN artists a ON v.artist_id = a.id
        WHERE v.is_concert = 1
        ORDER BY v.view_count DESC
        LIMIT ?
    """, (limit,))
    
    results = cursor.fetchall()
    conn.close()
    
    return results

# ========================================
# 9. 메인 실행
# ========================================
if __name__ == "__main__":
    
    # 라이브 영상 수집 시작
    collect_all_artists_live_videos(
        db_path="live_concerts.db",
        limit_per_artist=30  # 아티스트당 최근 30개
    )
    
    # 샘플 쿼리
    print("\n📺 조회수 TOP 10 콘서트:")
    print("-" * 70)
    top_concerts = get_top_viewed_concerts("live_concerts.db", limit=10)
    for idx, (artist, title, views, url) in enumerate(top_concerts, 1):
        print(f"{idx}. {artist} - {title[:50]}...")
        print(f"   조회수: {views:,} | {url}")
        print()
