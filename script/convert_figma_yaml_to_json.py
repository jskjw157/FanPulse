#!/usr/bin/env python3
"""Figma YAML을 JSON으로 변환"""
import json
import yaml
from pathlib import Path

project_root = Path(__file__).parent.parent
yaml_path = project_root / '.figma' / 'figma_data.json'
json_path = project_root / '.figma' / 'figma_parsed.json'

# YAML 로드
with open(yaml_path, 'r', encoding='utf-8') as f:
    # JSON 문자열로 읽은 다음 YAML로 파싱
    content = f.read().strip('"').replace('\\n', '\n')
    data = yaml.safe_load(content)

# JSON으로 저장
with open(json_path, 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(f"✅ YAML → JSON 변환 완료: {json_path}")
print(f"📊 노드 개수: {len(data.get('nodes', []))}개")
