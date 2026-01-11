#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
수집된 라이브 콘서트 DB 조회/분석 유틸리티
"""

import sqlite3
import json
from datetime import datetime

DB_PATH = "live_concerts.db"

# ========================================
# 조회 함수들
# ========================================

def show_statistics():
    """전체 통계 출력"""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    print("\n" + "=" * 70)
    print("📊 라이브 콘서트 DB 통계")
    print("=" * 70)
    
    # 아티스트 수
    cursor.execute("SELECT COUNT(*) FROM artists")
    artist_count = cursor.fetchone()[0]
    print(f"🎤 등록된 아티스트: {artist_count}명")
    
    # 총 영상 수
    cursor.execute("SELECT COUNT(*) FROM live_videos")
    total_videos = cursor.fetchone()[0]
    print(f"📹 총 영상 수: {total_videos}개")
    
    # 콘서트 영상 수
    cursor.execute("SELECT COUNT(*) FROM live_videos WHERE is_concert = 1")
    concert_count = cursor.fetchone()[0]
    print(f"🎸 콘서트 영상: {concert_count}개")
    
    # 라이브 스트리밍 수
    cursor.execute("SELECT COUNT(*) FROM live_videos WHERE is_live_stream = 1")
    stream_count = cursor.fetchone()[0]
    print(f"📡 라이브 스트리밍: {stream_count}개")
    
    # 페스티벌 영상 수
    cursor.execute("SELECT COUNT(*) FROM live_videos WHERE is_festival = 1")
    festival_count = cursor.fetchone()[0]
    print(f"🎪 페스티벌: {festival_count}개")
    
    # 총 조회수
    cursor.execute("SELECT SUM(view_count) FROM live_videos")
    total_views = cursor.fetchone()[0] or 0
    print(f"👀 총 조회수: {total_views:,}")
    
    print("=" * 70)
    
    conn.close()

def show_top_artists(limit=10):
    """영상 많은 아티스트 TOP N"""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    print(f"\n🏆 영상 많은 아티스트 TOP {limit}")
    print("-" * 70)
    
    cursor.execute("""
        SELECT a.name, COUNT(v.id) as video_count
        FROM artists a
        LEFT JOIN live_videos v ON a.id = v.artist_id
        GROUP BY a.id
        ORDER BY video_count DESC
        LIMIT ?
    """, (limit,))
    
    for idx, (artist_name, count) in enumerate(cursor.fetchall(), 1):
        print(f"{idx:2d}. {artist_name:25s} - {count:3d}개")
    
    conn.close()

def show_recent_concerts(limit=20):
    """최근 업로드된 콘서트 영상"""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    print(f"\n🆕 최근 업로드된 콘서트 TOP {limit}")
    print("-" * 70)
    
    cursor.execute("""
        SELECT a.name, v.title, v.upload_date, v.duration_formatted, v.url
        FROM live_videos v
        JOIN artists a ON v.artist_id = a.id
        WHERE v.is_concert = 1 OR v.is_festival = 1
        ORDER BY v.upload_date DESC
        LIMIT ?
    """, (limit,))
    
    for idx, (artist, title, date, duration, url) in enumerate(cursor.fetchall(), 1):
        print(f"{idx:2d}. [{date}] {artist}")
        print(f"    {title[:60]}")
        print(f"    ⏱️  {duration} | 🔗 {url}")
        print()
    
    conn.close()

def show_top_viewed(limit=10):
    """조회수 높은 영상 TOP N"""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    print(f"\n🔥 조회수 TOP {limit}")
    print("-" * 70)
    
    cursor.execute("""
        SELECT a.name, v.title, v.view_count, v.upload_date, v.url
        FROM live_videos v
        JOIN artists a ON v.artist_id = a.id
        ORDER BY v.view_count DESC
        LIMIT ?
    """, (limit,))
    
    for idx, (artist, title, views, date, url) in enumerate(cursor.fetchall(), 1):
        print(f"{idx:2d}. {artist} ({date})")
        print(f"    {title[:60]}")
        print(f"    👀 {views:,} views | 🔗 {url}")
        print()
    
    conn.close()

def search_by_artist(artist_name):
    """특정 아티스트 검색"""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    print(f"\n🔍 '{artist_name}' 검색 결과")
    print("-" * 70)
    
    cursor.execute("""
        SELECT v.title, v.upload_date, v.duration_formatted, 
               v.view_count, v.is_concert, v.is_live_stream, 
               v.is_festival, v.url
        FROM live_videos v
        JOIN artists a ON v.artist_id = a.id
        WHERE a.name LIKE ?
        ORDER BY v.upload_date DESC
    """, (f"%{artist_name}%",))
    
    results = cursor.fetchall()
    
    if not results:
        print("검색 결과 없음")
    else:
        for idx, (title, date, duration, views, concert, stream, festival, url) in enumerate(results, 1):
            type_tags = []
            if concert: type_tags.append("🎸콘서트")
            if stream: type_tags.append("📡스트리밍")
            if festival: type_tags.append("🎪페스티벌")
            
            print(f"{idx:2d}. [{date}] {' '.join(type_tags)}")
            print(f"    {title[:60]}")
            print(f"    ⏱️  {duration} | 👀 {views:,} | 🔗 {url}")
            print()
    
    conn.close()

def export_to_json(output_file="concerts_export.json"):
    """전체 데이터 JSON으로 내보내기"""
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT a.name as artist_name,
               v.video_id, v.title, v.url,
               v.duration_formatted, v.upload_date,
               v.view_count, v.like_count,
               v.is_concert, v.is_live_stream, v.is_festival,
               v.confidence_score
        FROM live_videos v
        JOIN artists a ON v.artist_id = a.id
        ORDER BY v.upload_date DESC
    """)
    
    data = [dict(row) for row in cursor.fetchall()]
    
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    
    print(f"✅ {len(data)}개 영상을 {output_file}에 저장했습니다.")
    
    conn.close()

# ========================================
# 메인 메뉴
# ========================================

def main_menu():
    """대화형 메뉴"""
    
    while True:
        print("\n" + "=" * 70)
        print("🎵 라이브 콘서트 DB 조회 메뉴")
        print("=" * 70)
        print("1. 전체 통계 보기")
        print("2. 영상 많은 아티스트 TOP 10")
        print("3. 최근 업로드된 콘서트")
        print("4. 조회수 TOP 10")
        print("5. 아티스트 검색")
        print("6. JSON 파일로 내보내기")
        print("0. 종료")
        print()
        
        choice = input("선택하세요: ").strip()
        
        if choice == "1":
            show_statistics()
        
        elif choice == "2":
            show_top_artists(10)
        
        elif choice == "3":
            show_recent_concerts(20)
        
        elif choice == "4":
            show_top_viewed(10)
        
        elif choice == "5":
            artist_name = input("아티스트 이름: ").strip()
            search_by_artist(artist_name)
        
        elif choice == "6":
            filename = input("파일명 (기본: concerts_export.json): ").strip()
            if not filename:
                filename = "concerts_export.json"
            export_to_json(filename)
        
        elif choice == "0":
            print("\n👋 프로그램을 종료합니다.")
            break
        
        else:
            print("⚠️  잘못된 선택입니다.")

if __name__ == "__main__":
    import os
    
    if not os.path.exists(DB_PATH):
        print(f"⚠️  DB 파일이 없습니다: {DB_PATH}")
        print("먼저 live_concert_collector.py를 실행해주세요.")
    else:
        main_menu()
