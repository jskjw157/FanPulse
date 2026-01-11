# Implementation Plan: Complete Remaining Web Routes

이 계획은 TDD 원칙을 준수하며, 유사한 성격의 페이지들을 그룹화하여 진행합니다.

## Phase 1: Auth & User Personalization
사용자 식별 및 개인화와 관련된 페이지들을 구현합니다.

- [x] Task: Login Page - `/login` 구현 (Layout 제외 처리 확인)
    - [x] Sub-task: `/login/page.test.tsx` 및 `/login/page.tsx` 구현
- [x] Task: My Page & Settings - `/mypage`, `/settings` 구현
    - [x] Sub-task: 각 페이지 테스트 및 구현 (프로필, 설정 UI)
- [x] Task: User Library - `/favorites`, `/saved`, `/tickets` 구현
    - [x] Sub-task: 각 페이지 테스트 및 구현 (목록형 UI)
- [x] Task: Conductor - User Manual Verification 'Phase 1' (Protocol in workflow.md)

- [x] Task: Search & Notifications - `/search`, `/notifications` 구현
    - [x] Sub-task: 검색 UI 및 알림 리스트 구현
- [x] Task: Support & Error - `/support`, `/error` 구현
    - [x] Sub-task: FAQ 아코디언 및 에러 페이지 구현
- [x] Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md)

## Phase 3: Content Details & Creation
콘텐츠 상세 및 작성 페이지를 구현합니다.

- [x] Task: Detail Pages - `/concert-detail`, `/notice-detail` 구현
    - [x] Sub-task: 상세 정보 뷰 구현
- [x] Task: Creation - `/post-create` 구현
    - [x] Sub-task: 에디터 UI 구현
- [x] Task: Conductor - User Manual Verification 'Phase 3' (Protocol in workflow.md)

## Phase 4: Final Integration
전체 링크 연결 상태를 점검합니다.

- [x] Task: Full Navigation Test - 모든 페이지 간의 링크 연결 검증 (Integration Test 추가)
- [x] Task: Conductor - User Manual Verification 'Phase 4' (Protocol in workflow.md)
