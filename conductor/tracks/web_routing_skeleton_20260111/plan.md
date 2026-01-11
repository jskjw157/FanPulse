# Implementation Plan: Web Frontend Routing & Skeleton Implementation

이 계획은 `conductor/workflow.md`의 TDD 원칙과 `product-guidelines.md`의 디자인 원칙을 준수합니다.

## Phase 1: Routing Foundation & Reference Analysis [checkpoint: 1b15f83]
기본 라우팅 구조를 설정하고 `web/reference` 소스를 분석하여 공통 스타일 가이드를 추출합니다.

- [x] Task: Project Analysis - `web/reference` 소스 코드 분석 및 재사용 가능 컴포넌트 식별
- [x] Task: Setup - 공통 와이어프레임 컴포넌트(Skeleton Card, Header, Wrapper) 구현
- [x] Task: Conductor - User Manual Verification 'Phase 1' (Protocol in workflow.md)

## Phase 2: Content Listing Pages (List-view Skeletons) [checkpoint: 2b550c7]
목록 형태의 정보를 제공하는 페이지들을 구현합니다. TDD 방식에 따라 테스트를 먼저 작성합니다.

- [x] Task: Live List Page - `/live` 경로 구현 9f02b68
    - [x] Sub-task: `/live/page.test.tsx` 작성 (라우팅 및 기본 UI 요소 검증)
    - [x] Sub-task: `/live/page.tsx` 구현 (Live 카드 목록 스켈레톤, Framer Motion 적용)
- [x] Task: Community Page - `/community` 경로 구현 560b978
    - [x] Sub-task: `/community/page.test.tsx` 작성
    - [x] Sub-task: `/community/page.tsx` 구현 (게시글 목록 스켈레톤)
- [x] Task: Chart & Concert Pages - `/chart`, `/concert` 경로 구현 1c3f2ea
    - [x] Sub-task: 각 페이지 테스트 작성
    - [x] Sub-task: 차트 순위 및 공연 일정 목록 스켈레톤 구현
- [x] Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md)

## Phase 3: Detailed View Pages (Detail-view Skeletons) [checkpoint: b06cd3d]
쿼리 파라미터를 포함한 상세 정보를 보여주는 페이지들을 구현합니다.

- [x] Task: News & Post Detail - `/news-detail`, `/post-detail` 경로 구현 04febba
    - [x] Sub-task: 다이나믹 라우팅/쿼리 파라미터 처리 테스트 작성
    - [x] Sub-task: 상세 본문, 메타 정보, 댓글 영역 스켈레톤 구현
- [x] Task: Artist & Live Detail - `/artist-detail`, `/live-detail` 경로 구현 f295349
    - [x] Sub-task: 테스트 작성 및 아티스트 프로필/라이브 플레이어 레이아웃 구현
- [x] Task: Conductor - User Manual Verification 'Phase 3' (Protocol in workflow.md)

## Phase 4: Special Interest & Interactive Pages
투표, 리워드, 멤버십 페이지를 구현합니다.

- [x] Task: Voting Page - `/voting` 경로 구현 7b15f37
    - [x] Sub-task: 투표 참여 UI 테스트 작성 및 구현 (Framer Motion 인터랙션 포함)
- [ ] Task: Membership & Ads - `/membership`, `/ads` 경로 구현
    - [ ] Sub-task: 안내 레이아웃 및 CTA 버튼 스켈레톤 구현
- [ ] Task: Conductor - User Manual Verification 'Phase 4' (Protocol in workflow.md)

## Phase 5: Final Navigation Integration
메인 페이지의 모든 링크가 정상 동작하는지 최종 검증합니다.

- [ ] Task: Integration Test - `app/page.tsx`에서 모든 생성된 페이지로의 네비게이션 테스트
- [ ] Task: Conductor - User Manual Verification 'Phase 5' (Protocol in workflow.md)
