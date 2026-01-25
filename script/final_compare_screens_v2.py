#!/usr/bin/env python3
"""
화면 정의서 ↔ Figma 디자인 비교 (v2 - 개선된 매칭)
"""
import json
import re
from pathlib import Path

def extract_screens_from_spec(spec_path):
    """화면_정의서.md에서 화면 목록 추출"""
    screens = {}

    with open(spec_path, 'r', encoding='utf-8') as f:
        content = f.read()

    table_match = re.search(r'## 2\. 화면 목록\s+\|.*?\|\s*\n\|(.*?)\n\n', content, re.DOTALL)
    if table_match:
        table_content = table_match.group(0)
        for line in table_content.split('\n'):
            match = re.match(r'\|\s*(H\d{3}(?:-\d)?)\s*\|\s*([^|]+)\s*\|\s*([^|]+)\s*\|\s*([^|]+)\s*\|', line)
            if match:
                screen_id = match.group(1).strip()
                screen_name = match.group(2).strip()
                path = match.group(3).strip()

                screens[screen_id] = {
                    'id': screen_id,
                    'name': screen_name,
                    'path': path
                }

    return screens

def extract_figma_frames(figma_json_path):
    """Figma JSON에서 최상위 프레임 추출"""
    with open(figma_json_path, 'r', encoding='utf-8') as f:
        content = f.read()

    yaml_content = json.loads(content)
    lines = yaml_content.split('\n')

    frames = {}
    current_id = None
    current_name = None

    for line in lines:
        if line.startswith("      - id: '"):
            match = re.match(r"      - id: '([^']+)'", line)
            if match:
                current_id = match.group(1)

        elif current_id and line.startswith("        name: "):
            current_name = line.replace("        name: ", "").strip()

        elif current_id and current_name and line.startswith("        type: "):
            type_value = line.replace("        type: ", "").strip()
            if type_value == "FRAME":
                frames[current_name] = {
                    'id': current_id,
                    'name': current_name
                }
            current_id = None
            current_name = None

    return frames

def path_to_screen_key(path):
    """경로를 화면 키로 변환"""
    # `/login` → `login`
    # `/news/:id` → `news-detail`
    # `/community` → `community`
    # `/support/faq` → `support-faq`
    # `(overlay)` → `menu`

    if path == '(overlay)':
        return 'menu'

    # 슬래시 제거, :id 부분을 -detail로 변환
    key = path.strip('/')

    # :id 패턴을 -detail로 변환
    if '/:id' in key or ':id' in key:
        key = key.replace('/:id', '').replace(':id', '')
        # 마지막 부분이 이미 detail이 아니면 추가
        if not key.endswith('-detail'):
            key = f"{key}-detail"

    # /를 -로 변환
    key = key.replace('/', '-')

    return key

def normalize_name(name):
    """화면명 정규화"""
    return re.sub(r'[^a-z0-9가-힣]', '', name.lower())

def compare_screens(spec_screens, figma_frames):
    """화면 정의서 ↔ Figma 비교"""
    results = {
        'matched': [],
        'potential_matches': [],
        'missing_in_figma': [],
        'extra_in_figma': []
    }

    matched_figma = set()

    for screen_id, screen in spec_screens.items():
        screen_name = screen['name']
        screen_path = screen['path']
        screen_key = path_to_screen_key(screen_path)

        matched = False

        # 1. 경로 키 매칭 (최우선)
        for frame_name, frame in figma_frames.items():
            if normalize_name(screen_key) == normalize_name(frame_name):
                results['matched'].append({
                    'screen_id': screen_id,
                    'screen_name': screen_name,
                    'screen_path': screen_path,
                    'figma_frame': frame_name,
                    'figma_id': frame['id'],
                    'match_type': 'path_key'
                })
                matched_figma.add(frame_name)
                matched = True
                break

        # 2. 경로 부분 매칭
        if not matched:
            path_parts = screen_path.strip('/').split('/')
            for frame_name, frame in figma_frames.items():
                if frame_name in matched_figma:
                    continue

                # 경로 일부가 프레임명에 포함되는지
                for part in path_parts:
                    part_clean = part.split(':')[0]  # :id 제거
                    if part_clean and normalize_name(part_clean) in normalize_name(frame_name):
                        results['potential_matches'].append({
                            'screen_id': screen_id,
                            'screen_name': screen_name,
                            'screen_path': screen_path,
                            'figma_frame': frame_name,
                            'figma_id': frame['id'],
                            'match_type': 'path_partial'
                        })
                        matched_figma.add(frame_name)
                        matched = True
                        break

                if matched:
                    break

        # 3. 화면명 매칭
        if not matched:
            for frame_name, frame in figma_frames.items():
                if frame_name in matched_figma:
                    continue

                norm_screen = normalize_name(screen_name)
                norm_frame = normalize_name(frame_name)

                if norm_screen in norm_frame or norm_frame in norm_screen:
                    results['potential_matches'].append({
                        'screen_id': screen_id,
                        'screen_name': screen_name,
                        'screen_path': screen_path,
                        'figma_frame': frame_name,
                        'figma_id': frame['id'],
                        'match_type': 'name_similarity'
                    })
                    matched_figma.add(frame_name)
                    matched = True
                    break

        # 매칭 실패
        if not matched:
            results['missing_in_figma'].append({
                'screen_id': screen_id,
                'screen_name': screen_name,
                'screen_path': screen_path
            })

    # Figma에만 있는 프레임
    for frame_name, frame in figma_frames.items():
        if frame_name not in matched_figma:
            results['extra_in_figma'].append({
                'figma_frame': frame_name,
                'figma_id': frame['id']
            })

    return results

def print_results(results, spec_screens, figma_frames):
    """결과 출력"""
    print("\n" + "="*70)
    print(" 화면 정의서 ↔ Figma 디자인 비교 결과")
    print("="*70 + "\n")

    # 완전 매칭
    if results['matched']:
        print(f"✅ 완전 매칭: {len(results['matched'])}개")
        for item in results['matched']:
            print(f"  {item['screen_id']:7s} {item['screen_name']:25s} ↔ {item['figma_frame']}")

    # 잠재적 매칭
    if results['potential_matches']:
        print(f"\n⚠️  잠재적 매칭: {len(results['potential_matches'])}개")
        for item in results['potential_matches']:
            print(f"  {item['screen_id']:7s} {item['screen_name']:25s} ≈ {item['figma_frame']}")

    # Figma에 없음
    if results['missing_in_figma']:
        print(f"\n❌ Figma에 없음: {len(results['missing_in_figma'])}개")
        for item in results['missing_in_figma']:
            print(f"  {item['screen_id']:7s} {item['screen_name']:25s} ({item['screen_path']})")

    # 화면 정의서에 없음
    if results['extra_in_figma']:
        print(f"\n🆕 화면 정의서에 없음: {len(results['extra_in_figma'])}개")
        for item in results['extra_in_figma']:
            print(f"  {item['figma_frame']} (ID: {item['figma_id']})")

    # 통계
    print(f"\n" + "="*70)
    print(f"📊 통계")
    print(f"  화면 정의서 총 화면 수: {len(spec_screens)}개")
    print(f"  Figma 총 프레임 수: {len(figma_frames)}개")

    total_matched = len(results['matched']) + len(results['potential_matches'])
    match_rate = total_matched / len(spec_screens) * 100 if spec_screens else 0

    print(f"  매칭률: {total_matched}/{len(spec_screens)} ({match_rate:.1f}%)")
    print("="*70 + "\n")

def main():
    project_root = Path(__file__).parent.parent
    spec_path = project_root / 'doc' / '화면_정의서.md'
    figma_json_path = project_root / '.figma' / 'figma_data.json'

    print("📖 화면 정의서 파싱...")
    spec_screens = extract_screens_from_spec(spec_path)
    print(f"  → {len(spec_screens)}개 화면 발견")

    print("🎨 Figma 데이터 파싱...")
    figma_frames = extract_figma_frames(figma_json_path)
    print(f"  → {len(figma_frames)}개 프레임 발견")

    results = compare_screens(spec_screens, figma_frames)
    print_results(results, spec_screens, figma_frames)

    output_path = project_root / '.figma' / 'comparison_result.json'
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(results, f, ensure_ascii=False, indent=2)

    print(f"💾 상세 결과 저장: {output_path}")

    return 0

if __name__ == '__main__':
    exit(main())
