# Specification: Complete Remaining Web Routes Implementation

## 1. Overview
웹 프론트엔드의 잔여 페이지 라우팅과 UI를 모두 구현하여, 사용자가 접근 가능한 모든 경로에서 완성도 높은 화면을 제공합니다. `web/reference`의 디자인과 데이터를 사용하여 즉시 시연 가능한 수준으로 개발합니다.

## 2. Functional Requirements
다음 경로에 해당하는 Next.js 페이지를 구현하고, 실제 목업 데이터를 적용합니다.

### 2.1. Auth & User
*   **/login**: 로그인 및 회원가입 폼 (소셜 로그인 버튼 포함).
*   **/mypage**: 프로필, 활동 통계, VIP 상태 표시.
*   **/settings**: 앱 설정 및 계정 관리.

### 2.2. Utilities
*   **/search**: 검색어 입력창, 최근 검색어, 인기 검색어.
*   **/notifications**: 알림 목록 (읽음/안읽음 처리 UI).
*   **/support**: 자주 묻는 질문(FAQ) 및 문의하기.
*   **/error**: 커스텀 404/500 에러 페이지.

### 2.3. Personalization & Library
*   **/favorites**: 즐겨찾는 아티스트/콘텐츠 목록.
*   **/saved**: 저장한 게시글 및 뉴스 목록.
*   **/tickets**: 예매한 티켓 목록 및 QR 코드 UI.

### 2.4. Content & Interaction
*   **/concert-detail**: 콘서트 상세 정보, 예매 버튼.
*   **/notice-detail**: 공지사항 상세 내용.
*   **/post-create**: 게시글 작성 에디터 (이미지 업로드 UI 포함).

## 3. Non-Functional Requirements
*   **Data**: 모든 페이지는 `web/reference`의 로직을 참고하여 실제와 유사한 더미 데이터와 `readdy.ai` 이미지를 사용합니다.
*   **Layout**:
    *   `/login`: 헤더/푸터 없는 단독 레이아웃 (또는 최소 레이아웃).
    *   그 외: `MainLayout` (헤더, 네비게이션 포함) 적용.
*   **Interaction**: 버튼 클릭, 탭 전환 등의 UI 인터랙션이 `Framer Motion` 등으로 부드럽게 동작해야 합니다.

## 4. Acceptance Criteria
*   13개의 모든 신규 경로가 정상적으로 렌더링되어야 합니다.
*   각 페이지 내의 링크(예: 검색 결과 클릭 -> 상세 페이지)가 올바르게 연결되어야 합니다.
*   린트 에러 없이 빌드가 성공해야 합니다.
