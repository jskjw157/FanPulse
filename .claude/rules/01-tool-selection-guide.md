# 도구 선택 가이드 (Tool Selection Guide)

프로젝트에 설치된 agents, skills, commands를 적절히 활용하기 위한 가이드입니다.

## 핵심 원칙

> ⚠️ **필수**: 아래 원칙을 반드시 준수해야 합니다.

### 1. Agent-First (에이전트 우선)

**모든 비단순 작업은 반드시 Agent를 통해 수행합니다.**

| 작업 유형 | 직접 처리 | Agent 사용 |
|----------|----------|------------|
| 단일 파일 읽기/수정 | ✅ | - |
| 코드 분석/리뷰 | ❌ | ✅ `code-reviewer`, `kotlin-analyzer` |
| 아키텍처 설계 | ❌ | ✅ `backend-architect`, `database-architect` |
| 문서화 | ❌ | ✅ `doc-writer`, `documentation-expert` |
| 테스트 실행/분석 | ❌ | ✅ `test-orchestrator` |
| 코드베이스 탐색 | ❌ | ✅ `Explore` agent |
| 진행 상황 체크 | ❌ | ✅ `progress-analyzer` |

**왜 Agent를 사용해야 하는가?**
- 토큰 효율성: Agent가 자체 컨텍스트에서 작업 후 요약만 반환
- 전문성: 각 Agent는 특화된 도메인 지식 보유
- 병렬 처리: 여러 Agent 동시 실행으로 처리 시간 단축

### 2. Parallel-First (병렬 우선)

**독립적인 Agent는 반드시 동시에 호출합니다.**

```
❌ 잘못된 예 (순차 실행):
1. code-reviewer 호출 → 완료 대기
2. kotlin-analyzer 호출 → 완료 대기
3. doc-writer 호출 → 완료 대기

✅ 올바른 예 (병렬 실행):
동시에 호출: code-reviewer + kotlin-analyzer + doc-writer
→ 모든 결과를 한 번에 수집
```

**병렬 실행 판단 기준:**
- 의존성 없음: 한 Agent의 결과가 다른 Agent의 입력이 아닌 경우
- 동일 자원 미사용: 같은 파일을 동시에 수정하지 않는 경우

### 3. Script-First (데이터 처리)

`00-script-first.md` 규칙 우선 적용 - 데이터 수집/정제는 스크립트로.

### 4. Context-Manager 활용

**장기 작업 또는 복잡한 멀티 에이전트 워크플로우 시:**
- `context-manager` agent를 사용하여 컨텍스트 보존
- 여러 세션에 걸친 작업 조정
- 복잡한 프로젝트의 상태 추적

---

## 작업 유형별 도구 선택 테이블

### 코드 개발

| 작업 | 권장 도구 | 병렬 가능 | 비고 |
|------|----------|----------|------|
| Kotlin 코드 작성 | (자동) run-tests-on-kotlin hook | - | .kt 수정 시 자동 실행 |
| 새 API 엔드포인트 | `backend-architect` | - | 설계 먼저 |
| DB 스키마 변경 | `database-architect` | - | 마이그레이션 계획 필요 |
| 새 기능 개발 | `/feature-dev:feature-dev` (plugin) | - | 코드베이스 이해 포함 |
| 프론트엔드 UI | `/frontend-design:frontend-design` (plugin) | - | 고품질 UI 생성 |

### 코드 분석/리뷰

| 작업 | 권장 도구 | 병렬 가능 | 비고 |
|------|----------|----------|------|
| Kotlin 정적 분석 | `kotlin-analyzer` | O | ktlint/detekt 실행 |
| 코드 리뷰 | `code-reviewer` | O | agent 기반 |
| PR 리뷰 | `/code-review:code-review` (plugin) | - | PR 전용 리뷰 |
| 아키텍처 검증 | `backend-architect` | O | |
| 전체 분석 | `/workflow:full-analysis` | - | 7개 에이전트 병렬 |

### 문서화

| 작업 | 권장 도구 | 병렬 가능 | 비고 |
|------|----------|----------|------|
| KDoc/주석 추가 | `doc-writer` | O | |
| 기술 문서 작성 | `documentation-expert` | O | |
| 사용자 가이드 | `technical-writer` | O | |
| 문서 정합성 검증 | `/validate-docs` | - | 화면-DB-API 정합성 |

### 기획/설계

| 작업 | 권장 도구 | 병렬 가능 | 비고 |
|------|----------|----------|------|
| DDD 설계 | `/ddd-planning` | - | Bounded Context, Aggregate |
| GitHub 이슈 생성 | `/github-issues` | - | 기획서 기반 |
| 기능 기획 | `/feature-planner` | - | |

### 테스트

| 작업 | 권장 도구 | 병렬 가능 | 비고 |
|------|----------|----------|------|
| Unit/Integration 실행 | `test-orchestrator` | - | ./gradlew test |
| E2E 테스트 계획 | `playwright-test-planner` | - | |
| E2E 테스트 생성 | `playwright-test-generator` | - | |
| E2E 테스트 디버깅 | `playwright-test-healer` | - | 실패 테스트 수정 |

### 진행 관리

| 작업 | 권장 도구 | 병렬 가능 | 비고 |
|------|----------|----------|------|
| 진행률 확인 | `progress-analyzer` | O | 기획서 대비 |
| 진행 상황 체크 | `/workflow:progress-check` | - | 3개 에이전트 병렬 |
| 컨텍스트 관리 | `context-manager` | - | 장기 작업 시 |

### Git/버전 관리

| 작업 | 권장 도구 | 병렬 가능 | 비고 |
|------|----------|----------|------|
| 커밋 생성 | `/commit-commands:commit` (plugin) | - | 자동 메시지 생성 |
| 커밋 + PR | `/commit-commands:commit-push-pr` (plugin) | - | 한 번에 처리 |
| 삭제된 브랜치 정리 | `/commit-commands:clean_gone` (plugin) | - | [gone] 브랜치 제거 |

### 설정/도구 관리

| 작업 | 권장 도구 | 병렬 가능 | 비고 |
|------|----------|----------|------|
| .claude/ 파일 수정 후 | `config-reviewer` | - | hook이 알림 |
| 새 Skill 생성 | `/skill-creator` | - | |
| 새 Agent 생성 | `/subagent-creator` | - | |
| 새 Hook 생성 | `/hook-creator` 또는 `/hookify:hookify` | - | hookify는 대화 분석 |
| Hook 목록 확인 | `/hookify:list` (plugin) | - | |
| 심층 분석 | `/ultra-think` | - | 복잡한 문제 해결 |

---

## 자동 실행 (Hooks)

| 트리거 | Hook | 동작 |
|--------|------|------|
| `.kt` 파일 수정 | `run-tests-on-kotlin.py` | 자동 테스트 실행 |
| `.claude/` 파일 수정 | `config-review-notify.py` | 검토 권장 알림 |

---

## 병렬 실행 권장 조합

### 코드 리뷰 시

```
동시 실행: code-reviewer + kotlin-analyzer + doc-writer
```

### 진행 상황 점검 시

```
동시 실행: progress-analyzer + code-reviewer + backend-architect
```

### 전체 분석 시

```
/workflow:full-analysis 사용 (7개 에이전트 자동 병렬)
```

---

## 빠른 참조

### 자주 사용하는 조합

| 상황 | 명령어 |
|------|--------|
| 새 기능 시작 | `/workflow:feature-dev [기능설명]` |
| 주간 점검 | `/workflow:progress-check W4` |
| 전체 건강도 | `/workflow:full-analysis` |
| DDD 설계 | `/ddd-planning` |
| 문서 검증 | `/validate-docs` |
| 심층 분석 | `/ultra-think [문제설명]` |

### Senior 역할별 스킬

| 역할 | 스킬 | 용도 |
|------|------|------|
| 백엔드 | `/senior-backend` | API, DB, 보안 |
| 프론트 | `/senior-frontend` | React, Next.js |
| 풀스택 | `/senior-fullstack` | 전체 스택 |
| DevOps | `/senior-devops` | CI/CD, 인프라 |

---

## 전체 도구 인벤토리

### Agents (17개)

| Agent | 용도 |
|-------|------|
| `backend-architect` | API/아키텍처 설계 |
| `code-reviewer` | 코드 품질 리뷰 |
| `command-expert` | CLI 명령 전문가 |
| `config-reviewer` | Claude 설정 검증 |
| `context-manager` | 컨텍스트 관리 (장기 작업) |
| `database-architect` | DB 설계 |
| `documentation-expert` | 문서화 전문 |
| `doc-writer` | KDoc, README 작성 |
| `kotlin-analyzer` | Kotlin 정적 분석 |
| `playwright-test-planner` | E2E 테스트 계획 |
| `playwright-test-generator` | E2E 테스트 생성 |
| `playwright-test-healer` | E2E 테스트 디버깅 |
| `progress-analyzer` | 진행률 분석 |
| `technical-writer` | 기술 문서 작성 |
| `test-orchestrator` | 테스트 실행/분석 |

### Skills (20개)

| Skill | 용도 |
|-------|------|
| `/ddd-planning` | DDD 기획/설계 |
| `/github-issues` | GitHub 이슈 생성 |
| `/validate-docs` | 문서 정합성 검증 |
| `/feature-planner` | 기능 기획 |
| `/code-review` | 코드 리뷰 |
| `/doc-writer` | 문서 작성 |
| `/senior-backend` | 백엔드 개발 |
| `/senior-frontend` | 프론트엔드 개발 |
| `/senior-fullstack` | 풀스택 개발 |
| `/senior-devops` | DevOps |
| `/skill-creator` | Skill 생성 |
| `/subagent-creator` | Agent 생성 |
| `/hook-creator` | Hook 생성 |
| `/slash-command-creator` | 슬래시 명령 생성 |
| `/algorithmic-art` | p5.js 알고리즘 아트 |
| `/frontend-design` | 프론트엔드 디자인 |
| `/claude-config-reviewer` | Claude 설정 검토 |
| `/developer-growth-analysis` | 개발자 성장 분석 |

### Commands (워크플로우)

| Command | 용도 |
|---------|------|
| `/workflow:feature-dev` | 기능 개발 (설계-리뷰-문서화) |
| `/workflow:full-analysis` | 전체 분석 (7개 에이전트 병렬) |
| `/workflow:progress-check` | 진행 상황 체크 |
| `/workflow:test-suite` | 테스트 스위트 분석/실행 |
| `/ultra-think` | 심층 분석 |
| `/architecture-review` | 아키텍처 리뷰 |
| `/explain-code` | 코드 설명 |
| `/all-tools` | 사용 가능한 도구 표시 |

---

## Plugins (claude-plugins-official)

외부 플러그인으로 설치된 도구들입니다.

### commit-commands

| Skill | 용도 | 사용 시기 |
|-------|------|----------|
| `/commit-commands:commit` | Git 커밋 생성 | 변경사항 커밋 시 |
| `/commit-commands:commit-push-pr` | 커밋 + 푸시 + PR 생성 | 기능 완료 후 PR 생성 |
| `/commit-commands:clean_gone` | [gone] 브랜치 정리 | 원격 삭제된 로컬 브랜치 정리 |

### code-review

| Skill | 용도 | 사용 시기 |
|-------|------|----------|
| `/code-review:code-review` | PR 코드 리뷰 | Pull Request 리뷰 시 |

### feature-dev

| Skill | 용도 | 사용 시기 |
|-------|------|----------|
| `/feature-dev:feature-dev` | 기능 개발 가이드 | 새 기능 구현 시 (코드베이스 이해 포함) |

### hookify

| Skill | 용도 | 사용 시기 |
|-------|------|----------|
| `/hookify:hookify` | Hook 자동 생성 | 대화 분석해서 방지할 동작 훅 생성 |
| `/hookify:list` | Hook 목록 조회 | 현재 설정된 훅 확인 |
| `/hookify:configure` | Hook 설정 | 훅 활성화/비활성화 |
| `/hookify:help` | Hookify 도움말 | 사용법 안내 |
| `/hookify:writing-rules` | Hook 규칙 작성 가이드 | 커스텀 훅 규칙 작성 시 |

### frontend-design

| Skill | 용도 | 사용 시기 |
|-------|------|----------|
| `/frontend-design:frontend-design` | 고품질 프론트엔드 UI | 웹 컴포넌트, 페이지 생성 시 |

---

## 도구 선택 요약 (Quick Decision)

| 하고 싶은 것 | 권장 도구 |
|-------------|----------|
| **커밋하기** | `/commit-commands:commit` |
| **PR 만들기** | `/commit-commands:commit-push-pr` |
| **PR 리뷰하기** | `/code-review:code-review` |
| **새 기능 개발** | `/feature-dev:feature-dev` 또는 `/workflow:feature-dev` |
| **프론트엔드 UI** | `/frontend-design:frontend-design` |
| **Hook 만들기** | `/hookify:hookify` 또는 `/hook-creator` |
| **Kotlin 분석** | `kotlin-analyzer` (agent) |
| **전체 점검** | `/workflow:full-analysis` |
| **DDD 설계** | `/ddd-planning` |
| **문서 검증** | `/validate-docs` |
