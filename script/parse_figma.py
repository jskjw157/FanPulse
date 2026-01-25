#!/usr/bin/env python3
"""
Figma 데이터 파싱 스크립트
대용량 Figma JSON을 파싱하여 주요 정보 추출
"""
import json
import sys
import yaml
from collections import defaultdict

def parse_figma_data(file_path):
    """Figma JSON 파일에서 주요 정보 추출"""
    with open(file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    # 첫 번째 요소에서 텍스트 추출
    if not data or not isinstance(data, list):
        return {"error": "Invalid JSON format"}

    content = data[0].get('text', '')

    # YAML 파싱 시도
    try:
        parsed = yaml.safe_load(content)
    except:
        return {"error": "Failed to parse YAML"}

    result = {
        "metadata": parsed.get('metadata', {}),
        "screens": [],
        "components": defaultdict(int),
        "text_samples": [],
        "screen_tree": []
    }

    # 노드 트리 분석
    nodes = parsed.get('nodes', [])
    if nodes:
        page = nodes[0]  # Page 1
        children = page.get('children', [])

        # 각 최상위 화면 분석
        for child in children:
            screen_info = analyze_screen(child)
            result["screens"].append(screen_info)
            result["screen_tree"].append({
                "id": child.get('id'),
                "name": child.get('name'),
                "type": child.get('type'),
                "child_count": count_children(child)
            })

    # 컴포넌트 통계
    result["components"] = {
        "FRAME": sum(1 for s in result["screens"] for _ in count_by_type(s, "FRAME")),
        "TEXT": sum(1 for s in result["screens"] for _ in count_by_type(s, "TEXT")),
        "IMAGE": sum(1 for s in result["screens"] for _ in count_by_type(s, "IMAGE-SVG")),
        "BUTTON": sum(1 for s in result["screens"] for _ in count_by_type(s, "BUTTON")),
    }

    return result

def analyze_screen(node):
    """화면(프레임) 노드 분석"""
    info = {
        "id": node.get('id'),
        "name": node.get('name'),
        "type": node.get('type'),
        "texts": [],
        "buttons": [],
        "images": 0,
        "structure": {}
    }

    # 재귀적으로 자식 노드 분석
    def traverse(n, depth=0):
        if depth > 10:  # 깊이 제한
            return

        node_type = n.get('type', '')
        node_name = n.get('name', '')

        # 텍스트 수집
        if 'text' in n:
            text = n.get('text', '').strip()
            if text and len(text) < 100:
                info["texts"].append(text)

        # 버튼 감지
        if 'BUTTON' in node_name.upper() or 'BUTTON' in node_type:
            info["buttons"].append(node_name)

        # 이미지 감지
        if 'IMAGE' in node_type or 'IMG' in node_name:
            info["images"] += 1

        # 자식 노드 탐색
        for child in n.get('children', []):
            traverse(child, depth + 1)

    traverse(node)

    # 텍스트 중복 제거
    info["texts"] = list(dict.fromkeys(info["texts"]))[:15]
    info["buttons"] = list(dict.fromkeys(info["buttons"]))[:10]

    return info

def count_children(node):
    """노드의 총 자식 수 계산"""
    count = len(node.get('children', []))
    for child in node.get('children', []):
        count += count_children(child)
    return count

def count_by_type(node, target_type):
    """특정 타입의 노드 개수 계산"""
    results = []
    def traverse(n):
        if n.get('type') == target_type:
            results.append(n)
        for child in n.get('children', []):
            traverse(child)
    traverse(node)
    return results

def main():
    if len(sys.argv) < 2:
        print("Usage: python parse_figma.py <figma_json_file>")
        sys.exit(1)

    file_path = sys.argv[1]
    result = parse_figma_data(file_path)

    # JSON으로 출력
    print(json.dumps(result, ensure_ascii=False, indent=2))

if __name__ == "__main__":
    main()
