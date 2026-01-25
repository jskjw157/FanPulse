#!/usr/bin/env python3
"""
화면 정의서와 Figma 디자인 비교 스크립트
"""
import json
import sys
import re
from pathlib import Path

def extract_screens_from_spec(spec_path):
    """화면_정의서.md에서 화면 목록 추출"""
    screens = {}

    with open(spec_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # 화면 목록 테이블 파싱 (## 2. 화면 목록 섹션)
    table_match = re.search(r'## 2\. 화면 목록\s+\|.*?\|\s*\n\|(.*?)\n\n', content, re.DOTALL)
    if table_match:
        table_content = table_match.group(0)
        for line in table_content.split('\n'):
            # | H001    | 메인 화면             | `/`                     | 최신 뉴스, 인기 게시글, 차트 순위 표시 |
            match = re.match(r'\|\s*(H\d{3}(?:-\d)?)\s*\|\s*([^|]+)\s*\|\s*([^|]+)\s*\|\s*([^|]+)\s*\|', line)
            if match:
                screen_id = match.group(1).strip()
                screen_name = match.group(2).strip()
                path = match.group(3).strip()
                description = match.group(4).strip()

                screens[screen_id] = {
                    'id': screen_id,
                    'name': screen_name,
                    'path': path,
                    'description': description
                }

    return screens

def extract_frames_from_figma(figma_json_path):
    """Figma JSON에서 프레임 목록 추출"""
    frames = {}

    with open(figma_json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    def traverse_nodes(node, depth=0):
        """노드 트리 순회"""
        if node.get('type') == 'FRAME':
            name = node.get('name', '')
            frame_id = node.get('id', '')

            # 화면 ID 패턴 추출 (예: "H001-메인화면" 또는 "/main")
            frames[name] = {
                'id': frame_id,
                'name': name,
                'type': node.get('type'),
            }

        # 자식 노드 순회
        if 'children' in node:
            for child in node['children']:
                traverse_nodes(child, depth + 1)

    # 문서 전체 순회
    if 'document' in data:
        traverse_nodes(data['document'])

    return frames

def compare_screens(spec_screens, figma_frames):
    """화면 정의서와 Figma 비교"""
    results = {
        'matched': [],
        'missing_in_figma': [],
        'missing_in_spec': [],
        'potential_matches': []
    }

    # 화면 정의서에 있지만 Figma에 없는 것
    for screen_id, screen in spec_screens.items():
        screen_name = screen['name']

        # Figma에서 매칭 시도
        matched = False
        for frame_name, frame in figma_frames.items():
            # 1. 화면 ID 직접 매칭 (예: "H001")
            if screen_id in frame_name:
                results['matched'].append({
                    'screen_id': screen_id,
                    'screen_name': screen_name,
                    'figma_frame': frame_name,
                    'match_type': 'id_match'
                })
                matched = True
                break

            # 2. 화면명 부분 매칭 (예: "메인 화면" in "메인화면")
            clean_screen_name = screen_name.replace(' ', '')
            clean_frame_name = frame_name.replace(' ', '')
            if clean_screen_name in clean_frame_name or clean_frame_name in clean_screen_name:
                results['potential_matches'].append({
                    'screen_id': screen_id,
                    'screen_name': screen_name,
                    'figma_frame': frame_name,
                    'match_type': 'name_similarity'
                })
                matched = True
                break

        if not matched:
            results['missing_in_figma'].append({
                'screen_id': screen_id,
                'screen_name': screen_name,
                'path': screen['path']
            })

    # Figma에 있지만 화면 정의서에 없는 것 (역방향 체크)
    matched_frames = set()
    for match in results['matched'] + results['potential_matches']:
        matched_frames.add(match['figma_frame'])

    for frame_name in figma_frames.keys():
        if frame_name not in matched_frames:
            # H로 시작하는 프레임만 체크 (화면으로 간주)
            if 'H0' in frame_name or '/' in frame_name:
                results['missing_in_spec'].append({
                    'figma_frame': frame_name
                })

    return results

def print_results(results):
    """결과 출력"""
    print("\n=== 화면 정의서 ↔ Figma 비교 결과 ===\n")

    print(f"✅ 매칭됨: {len(results['matched'])}개")
    for item in results['matched']:
        print(f"  - {item['screen_id']} ({item['screen_name']}) → Figma: {item['figma_frame']}")

    print(f"\n⚠️  잠재적 매칭 (확인 필요): {len(results['potential_matches'])}개")
    for item in results['potential_matches']:
        print(f"  - {item['screen_id']} ({item['screen_name']}) ≈ Figma: {item['figma_frame']}")

    print(f"\n❌ Figma에 없음 (화면 정의서에만 존재): {len(results['missing_in_figma'])}개")
    for item in results['missing_in_figma']:
        print(f"  - {item['screen_id']} {item['screen_name']} ({item['path']})")

    print(f"\n🆕 화면 정의서에 없음 (Figma에만 존재): {len(results['missing_in_spec'])}개")
    for item in results['missing_in_spec']:
        print(f"  - Figma: {item['figma_frame']}")

    # 통계
    total_spec = len(results['matched']) + len(results['potential_matches']) + len(results['missing_in_figma'])
    total_figma_screens = len(results['matched']) + len(results['potential_matches']) + len(results['missing_in_spec'])

    print(f"\n📊 통계")
    print(f"  화면 정의서 총 화면 수: {total_spec}개")
    print(f"  Figma 총 화면 프레임 수: {total_figma_screens}개")
    print(f"  매칭률: {len(results['matched']) / total_spec * 100:.1f}%")

def main():
    # 경로 설정
    project_root = Path(__file__).parent.parent
    spec_path = project_root / 'doc' / '화면_정의서.md'
    figma_json_path = project_root / '.figma' / 'figma_data.json'

    # Figma JSON이 없으면 먼저 다운로드 필요
    if not figma_json_path.exists():
        print(f"❌ Figma 데이터가 없습니다: {figma_json_path}")
        print("먼저 Figma 데이터를 다운로드하세요.")
        return 1

    # 화면 정의서 파싱
    print("📖 화면 정의서 파싱 중...")
    spec_screens = extract_screens_from_spec(spec_path)
    print(f"  → {len(spec_screens)}개 화면 발견")

    # Figma 데이터 파싱
    print("🎨 Figma 데이터 파싱 중...")
    figma_frames = extract_frames_from_figma(figma_json_path)
    print(f"  → {len(figma_frames)}개 프레임 발견")

    # 비교
    results = compare_screens(spec_screens, figma_frames)

    # 결과 출력
    print_results(results)

    # JSON 저장
    output_path = project_root / '.figma' / 'comparison_result.json'
    output_path.parent.mkdir(exist_ok=True)
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    print(f"\n💾 상세 결과 저장: {output_path}")

    return 0

if __name__ == '__main__':
    sys.exit(main())
