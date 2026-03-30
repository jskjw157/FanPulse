#!/usr/bin/env python3
"""
.claude/ 설정 파일 수정 시 검토 알림 훅

PostToolUse 이벤트에서 Edit|Write 도구 완료 후 실행됩니다.
.claude/ 디렉토리의 .md 또는 .json 파일 수정 시 검토를 권장합니다.
"""
import json
import sys


def main():
    try:
        data = json.load(sys.stdin)
        file_path = data.get('tool_input', {}).get('file_path', '')

        # .claude/ 디렉토리 파일인지 확인
        is_claude_dir = '/.claude/' in file_path or file_path.startswith('.claude/')
        is_config_file = file_path.endswith('.md') or file_path.endswith('.json')

        if is_claude_dir and is_config_file:
            # 파일 유형 판별
            if '/agents/' in file_path:
                file_type = 'Agent'
            elif '/skills/' in file_path:
                file_type = 'Skill'
            elif '/hooks/' in file_path or 'settings' in file_path:
                file_type = 'Hook/Settings'
            else:
                file_type = 'Config'

            print(f"\n.claude/ {file_type} 파일이 수정되었습니다: {file_path}")
            print("config-reviewer 에이전트로 검토를 권장합니다.\n")

    except Exception:
        # 에러 발생해도 non-blocking으로 종료
        pass

    # 항상 exit 0으로 종료 (non-blocking)
    sys.exit(0)


if __name__ == '__main__':
    main()
