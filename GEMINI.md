# FanPulse 프로젝트 컨텍스트 (GEMINI.md)

이 문서는 AI 에이전트가 FanPulse 프로젝트를 이해하고 효율적으로 작업하기 위한 핵심 정보를 담고 있습니다.

## 1. 프로젝트 개요

**FanPulse**는 글로벌 K-POP 팬들을 위한 인터랙티브 플랫폼으로, 커뮤니티, 투표, 방송/콘서트 정보, 굿즈/광고 리워드 등의 기능을 제공합니다. 대규모 트래픽 처리를 고려한 마이크로서비스 아키텍처와 이벤트 기반 시스템을 지향합니다.

## 2. 프로젝트 구조

이 저장소는 멀티 모듈 구조로 구성되어 있습니다.

-   **`backend/`**: Kotlin Spring Boot 기반 백엔드 애플리케이션
-   **`FanPulse_AOS/`**: Android (Kotlin, Jetpack Compose) 애플리케이션
-   **`FanPulse_iOS/`**: iOS (Swift, UIKit/MVVM) 애플리케이션
-   **`doc/`**: 기획서, 디자인, DB 설계, API 정의 등 상세 문서
-   **`script/`**: 코드 리뷰, 설정 검증, 크롤링 등을 위한 Python 유틸리티 스크립트
-   **`.agent/` & `.claude/`**: AI 에이전트 설정 및 규칙

## 3. 기술 스택

### 백엔드 (`backend/`)

-   **언어**: Kotlin 1.9.22 (JDK 17)
-   **프레임워크**: Spring Boot 3.2.2
-   **빌드 도구**: Gradle (Kotlin DSL)
-   **DB**: PostgreSQL (JPA), MongoDB, Redis
-   **기타**: Kafka, Spring Cloud Gateway, Docker/Kubernetes, AWS S3
-   **테스트**: JUnit 5, Mockk, Testcontainers

### Android (`FanPulse_AOS/`)

-   **언어**: Kotlin
-   **UI**: Jetpack Compose
-   **빌드 도구**: Gradle (Kotlin DSL)

### iOS (`FanPulse_iOS/`)

-   **언어**: Swift
-   **UI**: UIKit (MVVM 패턴)
-   **패키지 관리**: CocoaPods (추정)

## 4. 개발 및 실행 가이드

### 백엔드 실행

```bash
cd backend
# 테스트 실행
./gradlew test
# 애플리케이션 실행
./gradlew bootRun
```

### Android 빌드

```bash
cd FanPulse_AOS
./gradlew assembleDebug
```

### iOS 설정 (Mac 환경)

```bash
cd FanPulse_iOS
# 의존성 설치 (필요시)
# pod install
open FanPulse.xcodeproj
```

## 5. 개발 컨벤션

### 커밋 메시지 규칙 (Conventional Commits)

팀은 **Conventional Commits v1.0.0**을 엄격히 따릅니다.

-   **형식**: `<type>(<scope>): <description>`
-   **Type**: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`
-   **예시**:
    -   `feat(auth): add JWT refresh token support`
    -   `fix(api): handle null userId`

자세한 내용은 `doc/team_git_commit_convention_conventional_commits.md`를 참조하십시오.

## 6. 주요 문서 위치

작업 전 반드시 관련 문서를 참고하십시오.

-   **기획/기능**: `doc/프로젝트_기획서.md`, `doc/화면_정의서.md`
-   **DB 설계**: `doc/데이터베이스_정의서.md`
-   **API/아키텍처**: `readme.md`, `doc/크롤링.md`
-   **규칙**: `.agent/rules/`, `doc/team_git_commit_convention_conventional_commits.md`

## 7. 유틸리티 스크립트

`script/` 디렉토리에 다양한 자동화 스크립트가 있습니다.

-   `config_validator.py`: 설정 검증
-   `code_review_analyzer.py`: 코드 리뷰 분석
-   `pr_analyzer.py`: PR 분석
