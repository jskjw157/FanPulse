# Agent-Skill 통합 가이드

에이전트와 스킬의 관계, 올바른 구조, 중복 방지 방법을 설명합니다.

## 핵심 개념

### Agent vs Skill

| 구분 | Agent | Skill |
|-----|-------|-------|
| 위치 | `.claude/agents/*.md` | `.claude/skills/*/SKILL.md` |
| 호출 방식 | 자동 위임 또는 명시적 호출 | 자동 발견 또는 에이전트가 로드 |
| 역할 | 독립적인 컨텍스트에서 작업 수행 | 지식/가이드라인 제공 |
| 구조 | 단일 .md 파일 | 디렉토리 (SKILL.md + references/) |

### 올바른 통합 패턴

```
에이전트 (간결) ──skills: skill-name──▶ 스킬 (상세)
     │                                      │
     └── 최소한의 시스템 프롬프트            └── 워크플로우, 체크리스트, 가이드라인
```

## 에이전트 작성법

### 필수 필드

```yaml
---
name: agent-name           # 소문자, 하이픈
description: |             # 언제 사용할지 명시
  설명. Use PROACTIVELY after [trigger condition].
tools: Read, Grep, Glob    # 필요한 도구만
model: haiku               # haiku/sonnet/opus/inherit
skills: related-skill      # 연관 스킬 참조 ← 핵심!
---
```

### 시스템 프롬프트 (간결하게)

```markdown
You are a [role].

When invoked:

1. [First action]
2. [Second action]
3. Analyze using the [skill-name] skill
4. Check project rules in `.claude/rules/` if available

Guidelines:

- [Guideline 1]
- [Guideline 2]
```

## 스킬 작성법

### 디렉토리 구조

```
.claude/skills/my-skill/
├── SKILL.md              # 필수: 메인 스킬 정의
└── references/           # 선택: 상세 가이드
    ├── checklist.md
    └── examples.md
```

### SKILL.md 구조

```yaml
---
name: my-skill
description: |
  스킬 설명. 사용 시기: (1) ... (2) ... (3) ...
---

# Skill Name

## Quick Start
...

## Workflow
...

## Guidelines
...

상세 가이드: [references/checklist.md](references/checklist.md)
```

## 잘못된 패턴 vs 올바른 패턴

### ❌ 잘못된 패턴: 중복

```
agent/code-reviewer.md (73줄)
├── 리뷰 체크리스트 포함
├── 출력 형식 포함
└── 프로젝트 규칙 포함

skills/code-review/SKILL.md (90줄)
├── 리뷰 체크리스트 포함 ← 중복!
├── 출력 형식 포함 ← 중복!
└── 프로젝트 규칙 포함 ← 중복!
```

### ✅ 올바른 패턴: 분리

```
agent/code-reviewer.md (27줄)
├── skills: code-review ← 스킬 참조
└── 간결한 시스템 프롬프트

skills/code-review/SKILL.md (78줄)
├── 상세 체크리스트
├── 출력 형식
└── references/checklist.md
```

## 실제 예시

### doc-writer 에이전트 (올바른 예)

```yaml
---
name: doc-writer
description: |
  코드 문서화 전문 agent. Use PROACTIVELY after writing code.
tools: Read, Edit, Grep, Glob
model: haiku
skills: doc-writer    # ← 스킬 연동
---

You are a documentation specialist.

When invoked:

1. Read the target file(s)
2. Detect language and determine appropriate doc style
3. Apply documentation priority rules (see doc-writer skill)
4. Check project-specific rules in `.claude/rules/`
5. Add documentation comments
```

### code-reviewer 에이전트 (올바른 예)

```yaml
---
name: code-reviewer
description: |
  코드 리뷰 전문가 agent. Use PROACTIVELY after writing code.
tools: Read, Grep, Glob, Bash
model: haiku
skills: code-review   # ← 스킬 연동
---

You are a senior code reviewer.

When invoked:

1. Run `git diff HEAD` to identify changed files
2. Analyze changes against the review checklist (see code-review skill)
3. Check project-specific rules in `.claude/rules/`
4. Report findings in structured format
```

## 체크리스트

에이전트 생성 시:

- [ ] `skills` 필드로 관련 스킬 연동했는가?
- [ ] 에이전트에 스킬과 중복된 내용이 없는가?
- [ ] 시스템 프롬프트가 30줄 이하로 간결한가?
- [ ] `.claude/rules/` 참조를 포함했는가?
- [ ] description에 "PROACTIVELY" 또는 호출 조건이 있는가?
