#!/usr/bin/env python3
"""
Hook: Kotlin 파일 수정 후 자동 테스트 실행
Event: PostToolUse (Edit|Write)
"""
import json
import sys
import subprocess
import os

def main():
    # stdin에서 hook 데이터 읽기
    try:
        data = json.load(sys.stdin)
    except json.JSONDecodeError:
        sys.exit(0)

    # 수정된 파일 경로 확인
    file_path = data.get('tool_input', {}).get('file_path', '')

    # .kt 파일이 아니면 스킵
    if not file_path.endswith('.kt'):
        sys.exit(0)

    # src/main 또는 src/test 경로만 대상
    normalized = file_path.replace('\\', '/')
    if '/src/main/' not in normalized and '/src/test/' not in normalized:
        sys.exit(0)

    print(f"[Hook] Kotlin 파일 변경: {os.path.basename(file_path)}")
    print("[Hook] 테스트 실행 중...")

    # 프로젝트 디렉토리
    project_dir = os.environ.get('CLAUDE_PROJECT_DIR', os.getcwd())

    # Windows vs Unix
    gradlew = 'gradlew.bat' if os.name == 'nt' else './gradlew'

    try:
        result = subprocess.run(
            [gradlew, ':string_registry_meta:test', '--quiet'],
            cwd=project_dir,
            capture_output=True,
            text=True,
            timeout=300
        )

        if result.returncode == 0:
            print("[Hook] 테스트 통과!")
        else:
            print("[Hook] 테스트 실패:", file=sys.stderr)
            # 마지막 1000자만 출력
            output = result.stdout + result.stderr
            print(output[-1000:] if len(output) > 1000 else output)
    except subprocess.TimeoutExpired:
        print("[Hook] 테스트 타임아웃 (5분)", file=sys.stderr)
    except Exception as e:
        print(f"[Hook] 테스트 실행 오류: {e}", file=sys.stderr)

    # 항상 exit 0 - 작업 계속 진행
    sys.exit(0)

if __name__ == '__main__':
    main()
