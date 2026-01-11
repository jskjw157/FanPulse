# Specification: Web Frontend Routing & Skeleton Implementation

## 1. Overview
웹 프론트엔드 프로젝트(`web`)의 메인 페이지에 존재하는 다양한 링크(`View All`, 상세 보기 등)에 대응하는 실제 페이지 파일들을 생성하고, 각 페이지의 목적에 맞는 와이어프레임 수준의 스켈레톤 UI를 구현합니다. 이를 통해 사용자 네비게이션 흐름을 완성하고 추후 상세 구현을 위한 기반을 마련합니다.

## 2. Functional Requirements
### 2.1. Page Creation
다음 경로에 해당하는 Next.js Page 컴포넌트(`page.tsx`)를 생성합니다.
1.  **News:** `/news-detail` (Query Parameter `id` 처리)
2.  **Live:** `/live` (목록), `/live-detail` (상세)
3.  **Community:** `/community` (목록), `/post-detail` (Query Parameter `id` 처리, 상세)
4.  **Chart:** `/chart` (전체 순위 목록)
5.  **Artist:** `/artist-detail` (Query Parameter `artist` 처리, 프로필 및 정보)
6.  **Voting:** `/voting` (투표 목록 및 참여)
7.  **Concert/Events:** `/concert` (공연 일정 및 티켓팅)
8.  **Rewards/Ads:** `/ads` (포인트 적립)
9.  **Membership:** `/membership` (VIP 멤버십 안내)

### 2.2. Skeleton UI Implementation
*   단순한 "준비 중" 텍스트가 아닌, 페이지 성격에 맞는 **와이어프레임 수준의 UI**를 구현합니다.
*   **목록 페이지 (`/live`, `/community`, `/chart`, `/concert`, `/voting`):**
    *   검색/필터 영역, 리스트 아이템(썸네일, 제목 등)의 반복 패턴을 더미 데이터로 구성.
*   **상세 페이지 (`/news-detail`, `/live-detail`, `/post-detail`, `/artist-detail`):**
    *   제목, 본문, 메타 정보(작성자, 날짜), 댓글 영역, 관련 콘텐츠 영역 등을 구획화하여 배치.
*   **기타 (`/ads`, `/membership`):**
    *   서비스 안내 문구, 주요 액션 버튼(CTA), 카드형 레이아웃 배치.

## 3. Non-Functional Requirements
*   **Framework:** Next.js 16 (App Router) 구조를 준수합니다.
*   **Styling:** Tailwind CSS 4를 사용하여 스타일링하며, 기존 디자인 시스템(색상, 폰트 등)을 따릅니다.
*   **Responsiveness:** 모바일 및 데스크탑 환경에 대응하는 반응형 레이아웃을 적용합니다.
*   **Navigation:** 모든 페이지는 기존 `MainLayout` (헤더, 사이드바 등)이 적용된 상태로 렌더링되어야 합니다.
*   **Reference Alignment:** `web/reference/` 폴더의 목업 소스를 디자인 및 로직의 표준으로 삼아 리팩토링합니다.

## 4. Acceptance Criteria
*   메인 페이지(`app/page.tsx`)의 모든 링크를 클릭했을 때 404 에러 없이 해당 페이지로 이동해야 합니다.
*   `/news-detail?id=1`과 같이 쿼리 스트링이 포함된 링크로 이동 시 에러가 발생하지 않아야 합니다.
*   각 페이지는 빈 화면이 아닌, 해당 기능의 레이아웃을 짐작할 수 있는 스켈레톤 UI가 렌더링되어야 합니다.
