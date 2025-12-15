# 😸 GitHub 프로젝트 관리

## 📌 GitHub Project Milestone Issue 분류

### 1️⃣ 대분류 (Label / Milestone)

-   ✅ **백엔드 (Backend)**
-   ✅ **안드로이드 (Android)**
-   ✅ **iOS**
-   ✅ **인프라 (Infrastructure)**
-   ✅ **크롤링 (Web Crawling & Data Pipeline)**

---

## 📌 각 대분류별 Milestone & Issue 예시

| 대분류       | Milestone 예시              | Issue 예시                          |
| ------------ | --------------------------- | ----------------------------------- |
| **백엔드**   | ✅ API 개발                 | - 사용자 인증 API 구현              |
|              | ✅ DB 설계 & 마이그레이션   | - 데이터베이스 스키마 설계          |
|              | ✅ 실시간 투표 시스템       | - WebSocket 기반 실시간 투표 기능   |
| **안드로이드** | ✅ UI/UX 설계              | - Jetpack Compose 화면 디자인       |
|              | ✅ Retrofit API 연동        | - 백엔드 API 연동                   |
|              | ✅ 회원가입 & 로그인        | - OAuth 로그인 구현                 |
| **iOS**      | ✅ UI/UX 설계               | - UIKit 화면 디자인                 |
|              | ✅ Alamofire API 연동       | - 백엔드 API 연동                   |
|              | ✅ 회원가입 & 로그인        | - OAuth 로그인 구현                 |
| **인프라**   | ✅ CI/CD 구축               | - GitHub Actions + Docker 배포      |
|              | ✅ Kubernetes 클러스터      | - AWS EKS 설정                      |
|              | ✅ 모니터링 & 로깅          | - Prometheus + Grafana 설정         |
| **크롤링**   | ✅ 뉴스 데이터 크롤링       | - Naver 뉴스 크롤링                 |
|              | ✅ 차트 순위 데이터 크롤링  | - Melon, Billboard 순위 데이터 수집 |
|              | ✅ 콘서트 일정 크롤링       | - Ticketmaster API 연동             |

---

## 📌 GitHub Issue 관리 전략

### 1. Milestone 설정

-   `백엔드 API 개발`, `인프라 설정`, `크롤링 데이터 수집` 등 큰 단위로 설정

### 2. Label 체계

**기능별 라벨**:
-   `feature`: 신규 기능 개발
-   `bug`: 버그 수정
-   `enhancement`: 기존 기능 개선
-   `refactor`: 코드 리팩토링
-   `docs`: 문서 작업

**우선순위 라벨**:
-   `high-priority`: 긴급/중요
-   `medium-priority`: 보통
-   `low-priority`: 낮음

**대분류 라벨**:
-   `backend`: 백엔드 관련
-   `android`: 안드로이드 관련
-   `ios`: iOS 관련
-   `infra`: 인프라 관련
-   `crawling`: 크롤링 관련

### 3. 담당자 배정 (Assignee)

-   백엔드, 안드로이드, iOS, 크롤링 담당자를 지정

### 4. 프로젝트 보드 활용

-   `To Do`, `In Progress`, `Done`으로 관리

### 5. Pull Request (PR) 기반 개발

-   Issue 단위로 브랜치 생성 → PR로 관리

### 6. 브랜치 네이밍 컨벤션

**형식**: `{type}/{category}-{feature-name}`

**타입 (type)**:
-   `feature`: 신규 기능
-   `fix`: 버그 수정
-   `refactor`: 리팩토링
-   `docs`: 문서
-   `test`: 테스트

**카테고리 (category)**:
-   `backend`: 백엔드
-   `android`: 안드로이드
-   `ios`: iOS
-   `infra`: 인프라
-   `crawling`: 크롤링

**예시**:
-   `feature/backend-user-auth`: 백엔드 사용자 인증 기능
-   `feature/android-login-ui`: 안드로이드 로그인 UI
-   `feature/ios-oauth-integration`: iOS OAuth 연동
-   `fix/crawling-news-duplicate`: 크롤링 뉴스 중복 버그 수정
-   `refactor/infra-k8s-config`: 인프라 Kubernetes 설정 리팩토링

---

## 📌 추가적으로 고려할 사항

-   ✅ **버그 트래킹** → `bug` 라벨로 긴급 대응
-   ✅ **기능 요청 (Feature Request)** → `enhancement` 라벨 활용
-   ✅ **우선순위 관리** → `high-priority`, `low-priority` 등 추가

---

# GitHub 크롤링 (Web Crawling & Data Pipeline) Milestone & Issue 관리

## 1. 개요

본 문서는 **FanPulse 프로젝트의 크롤링(Web Crawling & Data Pipeline) 작업을 GitHub Issue & Milestone**으로 체계적으로 관리하기 위한 문서입니다. 크롤링해야 할 데이터, 개발 진행 순서, GitHub Issue 생성 예시 등을 포함합니다.

---

## 2. 크롤링 목표 데이터

| 데이터 유형  | 데이터 설명                       | 출처                           | 활용 화면                                |
| ------------ | --------------------------------- | ------------------------------ | ---------------------------------------- |
| 뉴스 데이터  | 최신 K-POP 뉴스 크롤링            | Naver, Google News, Reddit     | H001 (메인 화면), H011 (뉴스 상세)       |
| 차트 순위    | K-POP 실시간 음악 차트            | Melon, Bugs, Billboard         | H001 (메인 화면), H005 (차트 순위)       |
| 콘서트 일정  | K-POP 공연 일정 및 티켓 정보      | Ticketmaster, Interpark        | H007 (콘서트 일정), H015 (상세 공연 정보) |
| 광고 데이터  | 광고 시청을 통한 포인트 적립      | Ktown4u, Weverse Shop          | H008 (광고 참여), H016 (마이페이지)      |

---

## 3. GitHub Milestone 구성

### Milestone: 크롤링 시스템 구축

-   **목표**: 크롤링을 수행하여 FanPulse 데이터베이스에 저장할 수 있도록 구현
-   **기간**: 4주
-   **주요 작업**:
    1. 크롤링 대상 데이터 및 방법 정의
    2. 크롤링 코드 개발
    3. 데이터 정제 및 저장
    4. 스케줄링 및 자동화 구축

---

## 4. GitHub Issue 상세 목록

### [Epic] 뉴스 데이터 크롤링

**설명**: Naver, Google News, Reddit에서 K-POP 뉴스 데이터를 수집하여 데이터베이스에 저장하는 기능을 개발합니다.

-   **Issue 1**: Naver 뉴스 크롤러 개발 (`feature/crawling-naver-news`)
    -   `requests`, `BeautifulSoup` 사용하여 뉴스 목록 수집
    -   기사 제목, 링크, 내용, 작성 날짜 수집
    -   `MongoDB`에 저장

-   **Issue 2**: Google News RSS 크롤러 개발 (`feature/crawling-google-news`)
    -   Google News API 활용
    -   특정 키워드 기반 뉴스 검색

-   **Issue 3**: 뉴스 데이터 정제 및 중복 제거 (`feature/data-cleaning-news`)
    -   동일 기사 중복 제거
    -   자연어 처리로 중요 문장 추출

-   **Issue 4**: 뉴스 크롤러 스케줄링 (`feature/schedule-news-crawler`)
    -   `Celery`, `Airflow` 활용하여 자동 실행 설정

### [Epic] 음악 차트 크롤링

**설명**: Melon, Bugs, Billboard 차트 데이터를 크롤링하여 실시간 랭킹을 제공하는 기능을 개발합니다.

-   **Issue 5**: Melon 차트 크롤링 (`feature/crawling-melon-chart`)
    -   `Selenium` 사용하여 로그인 없이 데이터 수집
    -   차트 순위, 곡 제목, 아티스트명 저장

-   **Issue 6**: Bugs 차트 크롤링 (`feature/crawling-bugs-chart`)
    -   `requests` 및 `BeautifulSoup` 활용

-   **Issue 7**: Billboard API 연동 (`feature/billboard-api-integration`)
    -   `Billboard API` 활용하여 데이터 수집

-   **Issue 8**: 차트 데이터 통합 (`feature/merge-chart-data`)
    -   Melon, Bugs, Billboard 데이터를 하나의 테이블로 정리

### [Epic] 콘서트 일정 크롤링

**설명**: Ticketmaster, Interpark 등에서 K-POP 콘서트 정보를 수집하여 일정 및 티켓 정보를 제공하는 기능을 개발합니다.

-   **Issue 9**: Ticketmaster API 연동 (`feature/ticketmaster-api`)
    -   `Ticketmaster API`를 활용하여 콘서트 일정 크롤링

-   **Issue 10**: Interpark 공연 크롤링 (`feature/crawling-interpark-tickets`)
    -   `Selenium` 및 `BeautifulSoup` 활용하여 콘서트 일정 수집

-   **Issue 11**: 공연 데이터 정제 (`feature/data-cleaning-concerts`)
    -   공연 일정 중복 제거 및 정제

### [Epic] 광고 데이터 크롤링

**설명**: Ktown4u, Weverse Shop 등에서 광고 및 상품 정보를 크롤링하여 사용자들에게 광고 참여 기회를 제공합니다.

-   **Issue 12**: Ktown4u 광고 크롤링 (`feature/crawling-ktown4u-ads`)
    -   Ktown4u에서 특정 이벤트 및 광고 상품 정보 수집

-   **Issue 13**: Weverse Shop 광고 크롤링 (`feature/crawling-weverse-ads`)
    -   Weverse Shop API 활용하여 광고 상품 정보 수집

-   **Issue 14**: 광고 크롤러 스케줄링 (`feature/schedule-ad-crawler`)
    -   `Celery`, `Airflow`를 활용하여 자동화

---

## 5. GitHub Issue 관리 전략

1. **Milestone 설정** → `크롤링 시스템 구축`
2. **Issue 생성** → `뉴스 데이터`, `음악 차트`, `콘서트 일정`, `광고 데이터`로 분류
3. **Label 추가** → `feature`, `bug`, `enhancement` 적용
4. **담당자 배정** → 크롤링 담당 개발자 지정
5. **프로젝트 보드 활용** → `To Do`, `In Progress`, `Done` 구성
6. **Pull Request (PR) 연동** → `fix/crawling-news`, `feature/crawling-melon-chart` 등으로 브랜치 관리

---

## 6. 추가 고려 사항

-   ✅ **API 크롤링 가능 여부 확인** → 불가능한 경우 Selenium 활용
-   ✅ **데이터 저장 구조 설계** → 중복 제거 및 정규화 필요
-   ✅ **트래픽 제한 고려** → `time.sleep()`, `Proxy`, `User-Agent` 설정
-   ✅ **자동화 및 유지보수** → `Celery`, `Airflow`로 스케줄링

---

## 7. 주요 작업 세부 계획

### 1. 크롤링 대상 데이터 및 방법 정의

-   **완료 목표**: 각 Epic별 크롤링 소스 및 도구 선정 (예: API vs Selenium)
-   **작업**: Issue 작성 시 이미 정의 완료

### 2. 크롤링 코드 개발

-   **완료 목표**: Epic별 크롤러 개발 및 단위 테스트
-   **도구**:
    -   requests, BeautifulSoup: 정적 웹 크롤링
    -   Selenium: 동적 웹 크롤링
    -   API: Billboard, Ticketmaster, Weverse Shop

### 3. 데이터 정제 및 저장

-   **완료 목표**: 중복 제거 및 정규화된 데이터를 MongoDB에 저장
-   **작업**: 각 Epic 내 "데이터 정제" Issue 포함
-   **DB 구조** (데이터베이스 정의서 기준):
    -   뉴스 (crawled_news): {title, url, content, source, published_at}
    -   차트 (crawled_charts): {rank, song, artist, chart_source, updated_at}
    -   콘서트 (crawled_concerts): {event_name, artist, venue, date, ticket_link}
    -   광고: {product, price, event_link} (⚠️ DB 테이블 추후 추가 예정)

### 4. 스케줄링 및 자동화 구축

-   **완료 목표**: 모든 크롤러를 Celery로 자동화하고 Airflow로 관리
-   **작업**: 각 Epic 내 "스케줄링" Issue 포함

