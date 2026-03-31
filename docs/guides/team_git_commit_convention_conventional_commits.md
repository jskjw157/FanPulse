# Team Git Commit Convention (Conventional Commits)

본 문서는 팀 전체가 일관된 커밋 메시지를 작성하기 위해 **Conventional Commits v1.0.0** 규칙을 기반으로 정의한 Git Commit Convention입니다.

- 기준 명세: Conventional Commits v1.0.0
- 목적: 변경 이력의 가독성 향상, 리뷰 효율화, 자동 릴리스/체인지로그/버전 관리 지원

---

## 1. 기본 원칙

1. 모든 커밋 메시지는 **Conventional Commits 포맷**을 따른다.
2. 커밋 제목(Subject)은 **명령형(Imperative)** 으로 작성한다. (예: `add`, `fix`, `update`)
3. 제목은 **간결하게** 작성한다. (권장: 72자 이내)
4. 한 커밋은 **하나의 논리적 변경**을 담는다. (가능하면)
5. Break changes(호환성 파괴)는 반드시 **Breaking Change 표기**를 한다.

---

## 2. 커밋 메시지 포맷

### 2.1 표준 포맷

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

- **type**: 변경 성격(필수)
- **scope**: 변경 범위(선택) — 모듈/패키지/기능 영역 등
- **description**: 변경 요약(필수)
- **body**: 변경 이유/맥락/세부 사항(선택)
- **footer**: 이슈 링크, Breaking Change, 메타 정보(선택)

### 2.2 예시

```
feat(auth): add JWT refresh token support

Add refresh token rotation and persist token family.

Refs: PROJ-123
```

---

## 3. 타입(type) 정의

> 아래 타입 외 사용이 필요한 경우, 팀 합의 후 추가한다.

| Type | 의미 | 사용 예 |
|---|---|---|
| **feat** | 사용자/시스템 기능 추가 | `feat(payments): add subscription flow` |
| **fix** | 버그 수정 | `fix(api): handle null userId` |
| **docs** | 문서 변경 | `docs(readme): add setup instructions` |
| **style** | 포맷팅/세미콜론/린트 등(로직 변경 없음) | `style: apply ktfmt` |
| **refactor** | 리팩토링(기능 변화 없음) | `refactor(auth): simplify token validation` |
| **perf** | 성능 개선 | `perf(db): optimize index for queries` |
| **test** | 테스트 추가/수정 | `test(auth): add refresh token tests` |
| **build** | 빌드 시스템/의존성 변경 | `build: bump kotlin to 2.x` |
| **ci** | CI 설정 변경 | `ci: update GitHub Actions cache` |
| **chore** | 기타 잡무(빌드/런타임 로직 외) | `chore: update .gitignore` |
| **revert** | 되돌리기 | `revert: feat(auth): add refresh token support` |

---

## 4. Description(제목) 작성 규칙

- **명령형**으로 시작한다: `add`, `fix`, `update`, `remove`, `refactor`, `improve`
- 문장 끝에 마침표를 찍지 않는다.
- 가능한 한 "무엇을" 바꿨는지 요약한다.

### 좋은 예

- `feat(auth): add device-based login`
- `fix(api): return 400 on invalid payload`

### 피해야 할 예

- `feat: added new feature` (과거형/모호)
- `fix: bug` (정보 부족)

---

## 5. Body 작성 가이드(선택)

Body는 **왜(Why)** 와 **어떻게(How)** 를 보완하는 용도로 사용한다.

권장 내용:
- 변경 배경/문제
- 접근 방식/트레이드오프
- 호환성/마이그레이션 메모

예시:

```
fix(auth): prevent refresh token reuse

Refresh tokens were not invalidated after rotation.
This change stores token family id and checks reuse attempts.
```

---

## 6. Footer 작성 가이드(이슈/메타)

Footer는 이슈 트래킹 및 Breaking Change 표기 등을 위해 사용한다.

### 6.1 이슈 참조

- Jira/Linear/GitHub 이슈 등 팀 표준 키를 사용한다.

예시:

```
Refs: PROJ-123
Closes: PROJ-124
```

### 6.2 Breaking Change 표기

호환성 파괴 변경은 **반드시** 아래 중 하나로 표기한다.

#### 방식 A: `!` 사용

```
feat(api)!: change authentication header
```

#### 방식 B: Footer 사용

```
feat(api): change authentication header

BREAKING CHANGE: clients must use Authorization: Bearer <token>
```

---

## 7. Revert 커밋

되돌리기 커밋은 `revert:` 타입을 사용한다.

예시:

```
revert: feat(auth): add JWT refresh token support

This reverts commit <hash> due to production regression.
```

---

## 8. 머지 전략과 커밋 규칙

### 8.1 권장

- **Squash merge**를 사용하는 경우, PR 제목 또는 squash 메시지가 본 규칙을 준수하도록 한다.
- **Merge commit**를 허용하는 경우에도, 개별 커밋/merge 메시지 모두 규칙 준수를 권장한다.

### 8.2 PR 제목 규칙(권장)

- PR 제목도 Conventional Commits 포맷을 준수하면 릴리스 자동화에 유리하다.

예시:
- `feat(auth): add refresh token rotation`
- `fix(api): return correct error for invalid input`

---

## 9. 자주 쓰는 예시 모음

```
feat(payments): add subscription cancel endpoint
fix(user): avoid NPE when nickname is empty
refactor(core): extract common error mapper
perf(db): optimize query with composite index
ci: enable concurrency for workflows
chore: update dependencies
```

---

## 10. 도입 체크리스트(옵션)

- [ ] `commitlint` 도입 및 규칙 적용
- [ ] pre-commit 또는 Husky(또는 lefthook)로 로컬 훅 적용
- [ ] PR 템플릿에 커밋/PR 제목 규칙 명시
- [ ] 릴리스 자동화(semantic-release 등) 사용 여부 결정

---

## 11. 참고

- Conventional Commits v1.0.0: https://www.conventionalcommits.org/en/v1.0.0/
- (KR) Conventional Commits v1.0.0: https://www.conventionalcommits.org/ko/v1.0.0/

