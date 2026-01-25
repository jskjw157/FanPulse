#!/usr/bin/env python3
"""
Figma YAML에서 화면 프레임 추출 (YAML 라이브러리 없이)
"""
import re
from pathlib import Path

def extract_frames_from_yaml(yaml_content):
    """YAML 텍스트에서 최상위 프레임만 추출"""
    frames = []

    # 각 프레임 블록 찾기 (2 indent level = 최상위 Frame)
    pattern = r"  - id: '([^']+)'\n    name: ([^\n]+)\n    type: FRAME"

    for match in re.finditer(pattern, yaml_content):
        frame_id = match.group(1)
        frame_name = match.group(2).strip()

        frames.append({
            'id': frame_id,
            'name': frame_name
        })

    return frames

def main():
    project_root = Path(__file__).parent.parent
    figma_yaml_path = project_root / '.figma' / 'figma_data.json'

    # YAML 읽기 (JSON 문자열로 저장되어 있음)
    with open(figma_yaml_path, 'r', encoding='utf-8') as f:
        content = f.read()
        # JSON 문자열 unwrap
        if content.startswith('"') and content.endswith('"'):
            content = content[1:-1].replace('\\n', '\n')

    # 프레임 추출
    frames = extract_frames_from_yaml(content)

    print(f"📋 Figma 최상위 프레임 목록 ({len(frames)}개):")
    print()

    for i, frame in enumerate(frames, 1):
        print(f"{i:2d}. {frame['name']} (ID: {frame['id']})")

    # 결과 JSON으로 저장
    import json
    output_path = project_root / '.figma' / 'frames_list.json'
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(frames, f, ensure_ascii=False, indent=2)

    print(f"\n💾 결과 저장: {output_path}")

    return frames

if __name__ == '__main__':
    main()
