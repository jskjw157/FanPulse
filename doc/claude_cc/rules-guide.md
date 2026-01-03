# Rules 가이드

`.claude/rules/` 디렉토리를 사용하여 프로젝트별 규칙을 관리하는 방법을 설명합니다.

## 개요

Rules는 프로젝트별 또는 언어별 특화 규칙을 분리하여 관리하는 기능입니다.
에이전트와 스킬을 범용으로 유지하면서, 프로젝트 특화 내용은 rules에서 관리합니다.

## 디렉토리 구조

```
.claude/
├── CLAUDE.md              # 메인 프로젝트 지침
├── agents/                # 범용 에이전트
├── skills/                # 범용 스킬
└── rules/                 # 프로젝트/언어별 규칙
    ├── kotlin-spring.md   # Kotlin/Spring 규칙
    ├── react.md           # React 규칙
    └── security.md        # 보안 규칙
```

## 규칙 파일 작성법

### 기본 형식

```markdown
---
paths: "**/*.kt"    # 선택: 특정 파일에만 적용
---

# 규칙 제목

## 섹션 1
...

## 섹션 2
...
```

### paths 패턴 (조건부 적용)

| 패턴 | 매칭 대상 |
|-----|----------|
| `**/*.kt` | 모든 Kotlin 파일 |
| `**/*.ts` | 모든 TypeScript 파일 |
| `src/api/**/*.ts` | src/api 하위 TypeScript 파일 |
| `**/*.{ts,tsx}` | .ts와 .tsx 파일 모두 |
| `{src,lib}/**/*.ts` | src와 lib 하위 TypeScript 파일 |

### paths가 없는 경우

`paths` 필드가 없으면 모든 파일에 무조건 적용됩니다.

## 실제 예시

### kotlin-spring.md

```markdown
---
paths: "**/*.kt"
---

# Kotlin/Spring Boot Project Rules

## Code Review 추가 체크리스트

### Spring Boot 특화
- [ ] `@Transactional` 누락 여부
- [ ] Repository 메서드 네이밍 컨벤션
- [ ] DTO/Entity 분리 확인
- [ ] `@Valid` 어노테이션 누락 여부

### Exception 처리
- [ ] `BusinessException(ErrorCode)` 패턴 사용

### Response 패턴
- [ ] `ApiResponse<T>` wrapper 사용 여부

## Documentation (KDoc)

### KDoc 작성법
\`\`\`kotlin
/**
 * 사용자 정보를 조회합니다.
 *
 * @param userId 조회할 사용자 ID
 * @return 사용자 정보
 * @throws UserNotFoundException 사용자가 존재하지 않는 경우
 */
fun getUser(userId: Long): User
\`\`\`

## Naming Conventions

- Entity: `User`, `Order`
- Repository: `UserRepository`
- Service: `UserService` (interface), `UserServiceImpl`
- Controller: `UserController`
- DTO: `UserRequest`, `UserResponse`
```

### react-typescript.md

```markdown
---
paths: "**/*.{ts,tsx}"
---

# React/TypeScript Rules

## Code Review 체크리스트

- [ ] Props 타입 정의 (interface 사용)
- [ ] useEffect 의존성 배열 확인
- [ ] 불필요한 re-render 방지 (useMemo, useCallback)
- [ ] 에러 바운더리 적용

## Documentation (JSDoc/TSDoc)

\`\`\`typescript
/**
 * 사용자 프로필을 표시하는 컴포넌트
 * @param props - 컴포넌트 props
 * @param props.userId - 사용자 ID
 * @returns 사용자 프로필 컴포넌트
 */
export const UserProfile: React.FC<UserProfileProps> = ({ userId }) => {
  // ...
}
\`\`\`
```

### security.md (무조건 적용)

```markdown
# Security Rules

이 규칙은 모든 파일에 적용됩니다.

## 금지 사항

- [ ] 하드코딩된 시크릿/API 키 금지
- [ ] .env 파일 커밋 금지
- [ ] SQL 직접 문자열 연결 금지

## 필수 사항

- [ ] 사용자 입력 검증 필수
- [ ] Parameterized query 사용
- [ ] HTTPS 강제
```

## 범용화 패턴

### 목표

에이전트/스킬은 범용으로 유지하고, 언어/프레임워크 특화 내용은 rules로 분리합니다.

### Before (특화된 에이전트)

```yaml
# ❌ 언어에 종속됨
---
name: code-reviewer
---

You are a Kotlin/Spring Boot code reviewer.

## Kotlin/Spring 체크리스트
- @Transactional 확인
- ApiResponse<T> 사용
```

### After (범용 에이전트 + rules)

```yaml
# ✅ 범용
---
name: code-reviewer
---

You are a senior code reviewer.

When invoked:
1. Run git diff
2. Check project rules in `.claude/rules/`
3. Report findings
```

```markdown
# .claude/rules/kotlin-spring.md
---
paths: "**/*.kt"
---

## Kotlin/Spring 체크리스트
- @Transactional 확인
- ApiResponse<T> 사용
```

## 다른 프로젝트에서 재사용

### Kotlin 프로젝트

```bash
# .claude/ 폴더 복사 후
# rules/kotlin-spring.md 유지
```

### JavaScript 프로젝트

```bash
# .claude/ 폴더 복사 후
# rules/kotlin-spring.md 삭제
# rules/javascript.md 추가
```

### 멀티 언어 프로젝트

```bash
# 여러 규칙 파일 공존 가능
.claude/rules/
├── kotlin-spring.md    # **/*.kt 에만 적용
├── typescript.md       # **/*.ts 에만 적용
└── security.md         # 모든 파일에 적용
```

## 우선순위

1. `.claude/CLAUDE.md` - 메인 프로젝트 지침
2. `.claude/rules/*.md` - 모듈식 규칙 (동일 우선순위)
3. `~/.claude/CLAUDE.md` - 사용자 레벨 지침

## 체크리스트

규칙 파일 생성 시:

- [ ] `paths` 필드로 적용 범위를 명확히 했는가?
- [ ] 에이전트/스킬에서 중복된 내용을 제거했는가?
- [ ] 다른 프로젝트에서 재사용 가능한 구조인가?
