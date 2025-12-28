---
name: github-issues
description: "기획 문서 기반 GitHub 이슈/마일스톤 생성 스킬. 화면 정의서, 기획서를 분석하여 이슈와 마일스톤을 자동 생성합니다. 사용 시기: (1) 새 프로젝트 초기 이슈 생성 (2) 새 화면/기능 추가 시 이슈 생성 (3) 마일스톤 설정 (4) /github-issues 호출 시"
---

# GitHub 이슈/마일스톤 생성

## 개요

FanPulse 기획 문서(화면 정의서, 프로젝트 기획서)를 분석하여 GitHub 이슈와 마일스톤을 생성합니다.

## 워크플로우

```
1. 문서 분석 → 2. 플랫폼/범위 확인 → 3. 이슈 생성 → 4. 마일스톤 연결
```

## 생성 모드

### 1. 화면별 이슈 생성

화면 정의서의 각 화면(H001, H002 등)을 기반으로 이슈 생성

```
/github-issues --mode screen --platform web
/github-issues --mode screen --platform backend
```

### 2. 기능별 이슈 생성

프로젝트 기획서의 핵심 기능을 기반으로 이슈 생성

```
/github-issues --mode feature
```

### 3. 전체 생성

```
/github-issues --all
```

## 플랫폼 라벨

| 라벨 | 설명 |
|------|------|
| `web` | 웹 프론트엔드 (React/Next.js 등) |
| `android` | Android 앱 |
| `ios` | iOS 앱 |
| `backend` | 백엔드 API |
| `devops` | 인프라/배포 |

## 이슈 생성 규칙

### 화면별 이슈 형식

```markdown
## 📋 화면 정보
- **화면 ID**: {screen_id}
- **화면명**: {screen_name}
- **경로**: {path}

## 📌 설명
{description}

## ✅ 구현 요구사항
### UI 컴포넌트
- [ ] {component_1}
- [ ] {component_2}

### 기능
- [ ] {feature_1}
- [ ] {feature_2}

## 🔗 관련 이슈
- #{related_issue}
```

### 기능별 이슈 형식

```markdown
## 📌 기능 설명
{feature_description}

## ✅ 구현 요구사항
- [ ] {requirement_1}
- [ ] {requirement_2}

## 📋 관련 화면
- {screen_id}: {screen_name}
```

## 마일스톤 구조

| 마일스톤 | 설명 | 포함 기능 |
|----------|------|-----------|
| MVP | 최소 기능 제품 | 로그인, 메인, 라이브, 뉴스 |
| v1.0 | 정식 출시 | 커뮤니티, 투표, 차트 |
| v1.1 | 기능 확장 | 멤버십, 리워드, 콘서트 |

## gh CLI 명령어

### 이슈 생성

```bash
gh issue create --repo {owner}/{repo} \
  --title "[{platform}] {screen_name} ({screen_id})" \
  --label "{platform},feature" \
  --milestone "{milestone}" \
  --body "$(cat <<'EOF'
{issue_body}
EOF
)"
```

### 마일스톤 생성

```bash
gh api repos/{owner}/{repo}/milestones \
  --method POST \
  -f title="{milestone_name}" \
  -f description="{description}"
```

### 라벨 생성

```bash
gh label create {label_name} --color {color} --description "{desc}"
```

## 실행 전 확인사항

1. `gh auth status`로 GitHub 인증 확인
2. 저장소 접근 권한 확인
3. 기존 이슈/마일스톤 중복 확인

## 문서 경로

| 문서 | 경로 |
|------|------|
| 화면 정의서 | `docs/화면_정의서.md` |
| 프로젝트 기획서 | `docs/프로젝트_기획서.md` |
| MVP 화면 정의서 | `docs/mvp/mvp_화면_정의서.md` |

## 상세 템플릿

이슈 템플릿 상세는 `references/issue_templates.md` 참조
