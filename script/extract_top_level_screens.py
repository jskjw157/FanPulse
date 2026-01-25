#!/usr/bin/env python3
"""
Figma YAML에서 최상위 화면 프레임만 추출
"""
import re
import json
from pathlib import Path

def extract_top_level_frames(yaml_content):
    """최상위 레벨 (children 하위) 프레임만 추출"""
    frames = []

    # Pattern: 4칸 들여쓰기 + id + name + type: FRAME
    # children:\n      - id: 'xxx'\n        name: login\n        type: FRAME
    lines = yaml_content.split('\n')

    current_frame = None
    for i, line in enumerate(lines):
        # 4칸 들여쓰기의 id (children 바로 아래)
        if line.startswith("      - id: '"):
            match = re.match(r"      - id: '([^']+)'", line)
            if match:
                frame_id = match.group(1)
                current_frame = {'id': frame_id}

        # 8칸 들여쓰기의 name
        elif current_frame and line.startswith("        name: "):
            name = line.replace("        name: ", "").strip()
            current_frame['name'] = name

        # 8칸 들여쓰기의 type
        elif current_frame and line.startswith("        type: "):
            type_value = line.replace("        type: ", "").strip()
            current_frame['type'] = type_value

            # FRAME이면 저장
            if type_value == 'FRAME':
                frames.append(current_frame)

            current_frame = None  # 초기화

    return frames

def main():
    project_root = Path(__file__).parent.parent
    figma_yaml_path = project_root / '.figma' / 'figma_data.json'

    # 파일 읽기
    with open(figma_yaml_path, 'r', encoding='utf-8') as f:
        content = f.read()
        # JSON 문자열 unwrap
        if content.startswith('"') and content.endswith('"'):
            content = content[1:-1].replace('\\n', '\n')

    # 최상위 프레임 추출
    frames = extract_top_level_frames(content)

    print(f"\n📋 Figma 최상위 화면 프레임 ({len(frames)}개):\n")

    for i, frame in enumerate(frames, 1):
        print(f"{i:2d}. {frame['name']} (ID: {frame['id']})")

    # JSON 저장
    output_path = project_root / '.figma' / 'top_level_frames.json'
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(frames, f, ensure_ascii=False, indent=2)

    print(f"\n💾 결과 저장: {output_path}")

    return frames

if __name__ == '__main__':
    main()
