# GitHub 이슈 템플릿

## 1. 화면별 이슈 템플릿

### 프론트엔드 (web/android/ios)

```markdown
## 📋 화면 정보
- **화면 ID**: H002-1
- **화면명**: 회원가입
- **경로**: `/signup`

## 📌 설명
신규 사용자 회원가입 화면. 이메일, 비밀번호, 닉네임 입력 및 약관 동의

## ✅ 구현 요구사항
### UI 컴포넌트
- [ ] 이메일 입력 필드 (유효성 검사)
- [ ] 비밀번호 입력 필드 (강도 표시기)
- [ ] 비밀번호 확인 필드
- [ ] 닉네임 입력 필드
- [ ] 약관 동의 체크박스 (전체 동의, 개별 동의)
- [ ] 회원가입 버튼
- [ ] 로그인 화면 이동 링크

### 기능
- [ ] 이메일 중복 확인
- [ ] 닉네임 중복 확인
- [ ] 비밀번호 강도 검증
- [ ] 약관 동의 필수 항목 검사

## 🔗 관련 이슈
- #31 [Android] 로그인/회원가입 화면 (H002)
```

### 백엔드

```markdown
## 📋 API 정보
- **관련 화면**: H002-1 (회원가입)
- **엔드포인트**: POST /api/v1/auth/signup

## 📌 설명
회원가입 API. 이메일/비밀번호 기반 신규 사용자 등록

## ✅ 구현 요구사항
### API 스펙
- [ ] Request Body 정의 (email, password, nickname)
- [ ] Response 정의 (user, token)
- [ ] 에러 코드 정의

### 비즈니스 로직
- [ ] 이메일 중복 검사
- [ ] 비밀번호 해싱 (bcrypt)
- [ ] JWT 토큰 발급
- [ ] 이메일 인증 발송 (선택)

### 데이터베이스
- [ ] users 테이블 INSERT
- [ ] auth_tokens 테이블 INSERT

## 🔗 관련 이슈
- #32 [Web] 회원가입 화면 (H002-1)
```

### DevOps

```markdown
## 📋 인프라 정보
- **관련 기능**: 인증 서비스
- **환경**: Production / Staging

## 📌 설명
인증 서비스 배포 파이프라인 구축

## ✅ 구현 요구사항
- [ ] Dockerfile 작성
- [ ] K8s Deployment 매니페스트
- [ ] GitHub Actions 워크플로우
- [ ] 환경 변수 설정 (Secret Manager)

## 🔗 관련 이슈
- #33 [Backend] 회원가입 API
```

## 2. 기능별 이슈 템플릿

```markdown
## 📌 기능 설명
팬 참여형 투표 시스템 - 글로벌 팬들이 참여할 수 있는 투표 기능

## ✅ 구현 요구사항
- [ ] 투표 생성/관리
- [ ] 투표 참여 (중복 방지)
- [ ] 실시간 결과 집계
- [ ] 랭킹 시스템

## 📋 관련 화면
- H004: 투표 페이지

## 📋 관련 테이블
- polls: 투표 진행 테이블
- vote_options: 투표 옵션 테이블
- votes: 투표 기록 테이블
```

## 3. 라벨 정의

| 라벨 | 색상 | 설명 |
|------|------|------|
| `web` | #1D76DB | 웹 프론트엔드 |
| `android` | #3DDC84 | Android 앱 |
| `ios` | #000000 | iOS 앱 |
| `backend` | #D93F0B | 백엔드 API |
| `devops` | #7057FF | 인프라/배포 |
| `feature` | #0E8A16 | 새 기능 |
| `bug` | #D73A4A | 버그 수정 |
| `enhancement` | #A2EEEF | 기능 개선 |

## 4. 마일스톤별 화면 매핑

### MVP
| 화면 ID | 화면명 | 우선순위 |
|---------|--------|----------|
| H001 | 메인 화면 | P0 |
| H002 | 로그인 | P0 |
| H002-1 | 회원가입 | P0 |
| H006 | 라이브 & 이벤트 | P0 |
| H011 | 뉴스 상세 | P0 |
| H016 | 마이페이지 | P1 |
| H018 | 검색 화면 | P1 |

### v1.0
| 화면 ID | 화면명 | 우선순위 |
|---------|--------|----------|
| H003 | 팬 커뮤니티 | P0 |
| H004 | 투표 페이지 | P0 |
| H005 | 차트 순위 | P1 |
| H012 | 상세 게시글 | P0 |
| H013 | 게시글 작성 | P0 |
| H014 | 아티스트 상세 | P1 |

### v1.1
| 화면 ID | 화면명 | 우선순위 |
|---------|--------|----------|
| H007 | 콘서트 일정 | P0 |
| H008 | 광고 & 리워드 | P0 |
| H009 | 팬 멤버십 | P0 |
| H015 | 상세 공연 정보 | P1 |
| H019 | 라이브 상세 | P1 |
| H022 | 예매 내역 | P1 |

## 5. gh CLI 예시

### 라벨 일괄 생성

```bash
gh label create web --color 1D76DB --description "웹 프론트엔드"
gh label create android --color 3DDC84 --description "Android 앱"
gh label create ios --color 000000 --description "iOS 앱"
gh label create backend --color D93F0B --description "백엔드 API"
gh label create devops --color 7057FF --description "인프라/배포"
gh label create feature --color 0E8A16 --description "새 기능"
```

### 마일스톤 생성

```bash
gh api repos/{owner}/{repo}/milestones --method POST \
  -f title="MVP" \
  -f description="최소 기능 제품 - 로그인, 메인, 라이브, 뉴스"

gh api repos/{owner}/{repo}/milestones --method POST \
  -f title="v1.0" \
  -f description="정식 출시 - 커뮤니티, 투표, 차트"

gh api repos/{owner}/{repo}/milestones --method POST \
  -f title="v1.1" \
  -f description="기능 확장 - 멤버십, 리워드, 콘서트"
```
