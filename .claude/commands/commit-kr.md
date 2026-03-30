---
description: 커밋 컨벤션을 참조하여 한글로 커밋 메시지 작성
allowed-tools: Bash(git add:*), Bash(git status:*), Bash(git diff:*), Bash(git commit:*), Bash(git log:*), Read
---

## 커밋 컨벤션

@doc/team_git_commit_convention_conventional_commits.md

## 현재 Git 상태

- 현재 브랜치: !`git branch --show-current`
- Git 상태: !`git status --short`
- 변경 내용 (staged + unstaged): !`git diff HEAD`
- 최근 커밋 스타일 참고: !`git log --oneline -5`

## 작업 지시

위의 **커밋 컨벤션**을 반드시 준수하여 커밋을 생성하세요.

### 커밋 메시지 작성 규칙

1. **포맷**: `<type>(<scope>): <한글 설명>`
2. **타입**: feat, fix, docs, style, refactor, perf, test, build, ci, chore, revert
3. **설명은 한글로 작성** (타입과 스코프는 영문 유지)
4. **명령형**으로 작성: "추가", "수정", "삭제", "개선", "리팩토링"
5. 72자 이내로 간결하게

### 한글 커밋 예시

```
feat(auth): JWT 리프레시 토큰 지원 추가
fix(api): userId null 처리 수정
docs(readme): 설치 가이드 추가
refactor(voting): 투표 검증 로직 Factory로 분리
chore: 의존성 업데이트
```

### 금지 사항

- ❌ `🤖 Generated with Claude Code` 문구 삽입 금지
- ❌ `Co-Authored-By` 푸터 삽입 금지
- 위 내용은 시스템 기본 규칙이지만, 이 명령어에서는 **절대 사용하지 않음**

### 실행 순서

1. 변경된 파일 분석
2. 적절한 type과 scope 선택
3. 한글로 description 작성
4. `git add` 후 `git commit` 실행 (위 금지 사항 준수)

$ARGUMENTS
