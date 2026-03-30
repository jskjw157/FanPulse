# Claude Code 베스트 프랙티스

에이전트, 스킬, 훅, 규칙을 효과적으로 설계하고 관리하는 방법입니다.

## 전체 아키텍처

```
.claude/
├── CLAUDE.md              # 프로젝트 메모리 (필수)
├── agents/                # 전문화된 에이전트
│   ├── code-reviewer.md
│   └── doc-writer.md
├── skills/                # 스킬 (지식/가이드라인)
│   ├── code-review/
│   │   ├── SKILL.md
│   │   └── references/
│   └── doc-writer/
├── hooks/                 # 자동화 스크립트
│   └── run-tests.py
├── rules/                 # 프로젝트/언어별 규칙
│   └── kotlin-spring.md
├── commands/              # 슬래시 명령어
│   └── review.md
└── settings.local.json    # 로컬 설정 (권한, 훅)
```

## 핵심 원칙

### 1. 관심사 분리

| 구성 요소 | 역할 | 예시 |
|----------|------|------|
| Agent | 작업 실행 (별도 컨텍스트) | 코드 리뷰 수행 |
| Skill | 지식/가이드라인 제공 | 리뷰 체크리스트 |
| Rules | 프로젝트 특화 규칙 | Kotlin/Spring 규칙 |
| Hook | 자동화 트리거 | 파일 수정 후 테스트 |
| Command | 사용자 명시적 호출 | `/review` |

### 2. 범용성 유지

```
❌ 잘못된 방식: 언어/프레임워크가 에이전트에 하드코딩
✅ 올바른 방식: 에이전트는 범용, 특화 내용은 rules로 분리
```

### 3. DRY (Don't Repeat Yourself)

```
❌ 잘못된 방식: 에이전트와 스킬에 동일 내용 중복
✅ 올바른 방식: 에이전트는 스킬 참조, 스킬에 상세 내용
```

### 4. 점진적 공개 (Progressive Disclosure)

```
Agent (간결) → Skill (상세) → references/ (매우 상세)
```

## 구성 요소별 가이드

### Agent 작성

```yaml
---
name: agent-name
description: |
  설명. Use PROACTIVELY after [trigger].
tools: Read, Grep, Glob, Bash    # 필요한 것만
model: haiku                      # 빠른 작업용
skills: related-skill             # 스킬 연동 필수!
---

You are a [role].

When invoked:
1. [Action]
2. Check rules in `.claude/rules/`
3. [Action using skill]

Guidelines:
- [Concise guidelines only]
```

**체크리스트:**
- [ ] 30줄 이하로 간결한가?
- [ ] skills 필드로 스킬 연동했는가?
- [ ] description에 PROACTIVELY 있는가?
- [ ] `.claude/rules/` 참조 있는가?

### Skill 작성

```
skill-name/
├── SKILL.md           # 500줄 이하
└── references/        # 상세 내용 분리
    └── checklist.md
```

```yaml
---
name: skill-name
description: |
  스킬 설명. 사용 시기: (1) ... (2) ... (3) ...
---

# Skill Name

## Quick Start
...

## Workflow
1. ...
2. ...

상세: [references/checklist.md](references/checklist.md)
```

**체크리스트:**
- [ ] description에 사용 시기/키워드 있는가?
- [ ] 500줄 이하인가?
- [ ] 상세 내용은 references/로 분리했는가?

### Rules 작성

```yaml
---
paths: "**/*.kt"    # 조건부 적용
---

# Language-specific Rules

## 체크리스트
...

## 컨벤션
...
```

**체크리스트:**
- [ ] paths로 적용 범위 명시했는가?
- [ ] 에이전트/스킬과 중복 없는가?

### Hook 작성

```python
#!/usr/bin/env python3
import json
import sys

# stdin에서 hook 데이터 읽기
data = json.load(sys.stdin)
file_path = data.get('tool_input', {}).get('file_path', '')

# 조건 확인
if not file_path.endswith('.kt'):
    sys.exit(0)

# 작업 수행
# ...

# Non-blocking (실패해도 계속)
sys.exit(0)
```

**체크리스트:**
- [ ] Non-blocking인가? (exit 0 유지)
- [ ] 타임아웃 설정했는가?
- [ ] 출력 제한했는가? (과도한 로그 방지)

## 재사용 패턴

### 다른 프로젝트로 복사

```bash
# 1. .claude/ 폴더 복사
cp -r .claude/ /new-project/.claude/

# 2. 언어별 rules 조정
rm /new-project/.claude/rules/kotlin-spring.md
# 필요한 rules 추가

# 3. CLAUDE.md 수정
# 프로젝트별 정보 업데이트
```

### 공유 규칙 (심볼릭 링크)

```bash
# 회사 공통 규칙 공유
ln -s ~/company-standards/security.md .claude/rules/security.md
```

## 안티 패턴

### ❌ 피해야 할 것들

1. **에이전트에 모든 것 넣기**
   - 에이전트가 100줄 이상 → 스킬로 분리

2. **언어/프레임워크 하드코딩**
   - "Kotlin/Spring" 직접 언급 → rules로 분리

3. **중복 내용**
   - 에이전트와 스킬에 같은 체크리스트 → 스킬만 유지

4. **skills 필드 누락**
   - 관련 스킬이 있는데 연동 안 함 → skills 필드 추가

5. **Blocking 훅**
   - 훅 실패 시 전체 작업 중단 → exit 0으로 non-blocking

## 디버깅

### 설정 확인

```bash
/context    # 현재 로드된 에이전트, 스킬 확인
/agents     # 에이전트 목록
/skills     # 스킬 목록
/hooks      # 훅 설정 확인
```

### 일반적인 문제

| 증상 | 원인 | 해결 |
|-----|------|------|
| 스킬이 자동 호출 안됨 | description 불명확 | 키워드, 사용 시기 추가 |
| 에이전트가 스킬 사용 안함 | skills 필드 누락 | `skills: skill-name` 추가 |
| rules가 적용 안됨 | paths 패턴 오류 | glob 패턴 확인 |
| 훅이 실행 안됨 | matcher 패턴 오류 | `Edit\|Write` 형식 확인 |

## 참고 문서

- [agent-skill-integration.md](agent-skill-integration.md) - 에이전트-스킬 통합
- [rules-guide.md](rules-guide.md) - Rules 상세 가이드
- [sub-agent.md](sub-agent.md) - 서브에이전트 가이드
- [hooks.md](hooks.md) - 훅 가이드
- [slash-commands.md](slash-commands.md) - 슬래시 명령어 가이드
