# 😀 FanPulse

글로벌 K-POP 팬들을 위한 인터랙티브 플랫폼
- notion: https://www.notion.so/oduckprogrammer/FanPulse-1979da8ee78480939281ee7ebf60256e?source=copy_link

## 📌 프로젝트 개요

**FanPulse**는 글로벌 K-POP 팬들을 위한 인터랙티브 플랫폼으로, 팬들은 방송 이벤트, 콘서트, 투표, 광고 참여 등의 기능을 활용할 수 있습니다.

### 주요 목표

-   전 세계 K-POP 팬들을 위한 통합 커뮤니티 플랫폼 제공
-   실시간 투표 및 차트 정보 제공
-   라이브 스트리밍 및 콘서트 정보 연계
-   팬 참여형 수익화 모델 구축

---

## 🎯 핵심 기능

### 1. 팬 커뮤니티 & 소셜 피드

-   아티스트별 팬 페이지 개설
-   팬들이 게시물을 공유하고 댓글, 좋아요, 공유 가능
-   실시간 트렌드 분석 (AI 기반 추천)

### 2. 방송 & 콘서트 연계 기능

-   Mnet 및 제휴 방송과 연계한 라이브 스트리밍 시청 기능
-   콘서트 티켓 예매 및 가상 콘서트 서비스 (VR/AR 연동 가능)

### 3. 팬 참여형 투표 시스템

-   글로벌 팬들이 참여할 수 있는 투표 기능
-   투표 랭킹 시스템 (예: 인기상, 주간 랭킹 등)
-   블록체인 기반 공정한 투표 시스템

### 4. 광고 및 수익화 기능

-   팬들이 광고를 시청하면 포인트 적립 후 굿즈 구매 가능
-   광고 참여형 이벤트 (예: 특정 브랜드 협찬 투표)

### 5. 팬덤 멤버십

-   VIP 멤버십 도입 (구독형 모델)
-   특별한 콘텐츠 제공 (비하인드 영상, 굿즈 할인 등)

---

## 🛠️ 기술 스택

### 백엔드

| 구성 요소       | 사용 기술                               |
| --------------- | --------------------------------------- |
| 프로그래밍 언어 | Kotlin                                  |
| 프레임워크      | Spring Boot                             |
| API Gateway     | Spring Cloud Gateway / Kong API Gateway |
| 인증 & 보안     | OAuth 2.0 / JWT                         |
| 데이터베이스    | PostgreSQL + Hibernate                  |
| NoSQL           | MongoDB                                 |
| 캐시 & 메시징   | Redis / Kafka                           |
| 파일 스토리지   | AWS S3                                  |
| 미디어 스트리밍 | AWS Media Services / WebRTC + FFmpeg    |
| 검색 엔진       | Elasticsearch                           |

### 프론트엔드

#### Android

| 구성 요소     | 사용 기술                        |
| ------------- | -------------------------------- |
| UI 프레임워크 | Jetpack Compose                  |
| 상태 관리     | ViewModel + LiveData             |
| 네트워크      | Retrofit / OkHttp                |
| 인증          | Firebase Authentication / OAuth2 |
| 데이터 저장소 | Room Database / DataStore        |

#### iOS

| 구성 요소     | 사용 기술                           |
| ------------- | ----------------------------------- |
| UI 프레임워크 | UIKit                               |
| 상태 관리     | MVVM + InOut 패턴                   |
| 네트워크      | Alamofire                           |
| 인증          | Google OAuth2                       |
| 데이터 저장소 | SwiftData / UserDefaults / Keychain |

### 인프라 및 운영

| 구성 요소      | 사용 기술                        |
| -------------- | -------------------------------- |
| 서버 배포 방식 | Docker + Kubernetes (AWS EKS)    |
| CI/CD          | GitHub Actions / ArgoCD          |
| 로드 밸런싱    | AWS ALB / Nginx                  |
| 모니터링       | Prometheus + Grafana / ELK Stack |
| 에러 트래킹    | Sentry / AWS CloudWatch          |
| CDN            | AWS CloudFront                   |
| 서비스 메시    | Istio / Linkerd                  |

---

## 🏗️ 아키텍처

### 백엔드 마이크로서비스 구조

```
User → API Gateway → Auth Service
                   → Vote Service
                   → Streaming Service
                   → Community Service
                   → Notification Service
```

### 대규모 트래픽 대응 방안

-   ✅ **로드 밸런싱**: AWS Application Load Balancer (ALB) 활용
-   ✅ **데이터 캐싱**: Redis + CDN (CloudFront) 적용
-   ✅ **비동기 이벤트 처리**: Kafka 기반 이벤트 드리븐 아키텍처
-   ✅ **마이크로서비스 분리**: Vote Service, Streaming Service 등 독립 서비스 운영
-   ✅ **오토 스케일링**: Kubernetes HPA 적용

---

## 💾 데이터베이스 구조

### PostgreSQL 테이블

-   **users**: 사용자 정보 관리 (로그인, 회원가입)
-   **auth_tokens**: 사용자 인증 토큰 저장
-   **polls, vote_options, votes**: 투표 시스템 구현
-   **notifications**: 사용자 알림 기능
-   **media**: 이미지 및 동영상 저장
-   **points, point_transactions**: 포인트 적립 및 사용 기록
-   **memberships**: VIP 멤버십 관리
-   **likes**: 게시글/댓글 좋아요 기능
-   **streaming_events**: 라이브 스트리밍 이벤트
-   **crawled_news**: 크롤링된 뉴스 데이터
-   **crawled_charts**: 크롤링된 차트 순위
-   **crawled_concerts**: 크롤링된 콘서트 일정

### MongoDB 컬렉션

-   **posts**: 팬 커뮤니티 게시글 (아티스트별 분류, 태그, 이미지)
-   **comments**: 댓글 및 대댓글 (계층 구조 지원)

---

## 🕷️ 크롤링 시스템

### 크롤링 대상 데이터

1. **K-POP 뉴스**: Naver 뉴스, Google News, Reddit
2. **음악 차트**: Melon, Bugs, Billboard, Oricon
3. **콘서트 일정**: Ticketmaster, Melon Ticket, Interpark
4. **굿즈/광고**: Ktown4u, Weverse Shop
5. **아티스트 정보**: Wikipedia, Spotify, Last.fm

### 크롤링 기술 스택

-   **라이브러리**: Selenium, BeautifulSoup, Scrapy, Playwright
-   **API 활용**: Spotify API, Twitter API, Billboard API, Ticketmaster API
-   **자동화**: Celery (스케줄링), Airflow (모니터링)
-   **데이터 저장**: PostgreSQL, MongoDB, Elasticsearch

### 크롤링 파이프라인

```
크롤링 (Selenium/API) → 데이터 정제 → 중복 제거 → DB 저장 → 자동화 (Celery/Airflow)
```

---

## 📱 주요 화면 구성

| 화면 ID | 화면명          | 설명                                   |
| ------- | --------------- | -------------------------------------- |
| H001    | 메인 화면       | 최신 뉴스, 인기 게시글, 차트 순위 표시 |
| H002    | 로그인/회원가입 | OAuth2 기반 SNS 로그인                 |
| H003    | 팬 커뮤니티     | 아티스트별 팬 페이지 및 게시물         |
| H004    | 투표 페이지     | 실시간 투표 참여 및 결과 확인          |
| H005    | 차트 순위       | Billboard, Melon 등 차트 순위          |
| H006    | 라이브 스트리밍 | 콘서트 및 방송 실시간 스트리밍         |
| H007    | 콘서트 일정     | 공연 일정 및 티켓 예매 정보            |
| H008    | 광고 참여       | 광고 시청 후 포인트 적립               |
| H009    | 팬 멤버십       | VIP 구독 서비스 관리                   |
| H016    | 마이페이지      | 프로필, 포인트, 활동 내역 확인         |

---

## 🚀 설치 및 실행

### 사전 요구사항

-   JDK 17 이상
-   Kotlin 1.9+
-   Docker & Docker Compose
-   PostgreSQL 15+
-   MongoDB 6+
-   Redis 7+
-   Node.js 18+ (프론트엔드)

### 백엔드 실행

```bash
# 저장소 클론
git clone https://github.com/your-org/fanpulse.git
cd fanpulse

# 환경 변수 설정
cp .env.example .env
set -a; source .env; set +a

# Docker로 PostgreSQL 실행
docker compose up -d postgres

# Spring Boot 애플리케이션 실행
cd backend
./gradlew bootRun
```

MongoDB와 Redis는 별도 설치/실행이 필요합니다.

### 프론트엔드 실행

#### Android

```bash
cd fanpulse/android
./gradlew assembleDebug
```

#### iOS

```bash
cd fanpulse/ios
pod install
open FanPulse.xcworkspace
```

---

## 📚 API 문서

API 문서는 Swagger UI를 통해 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui.html
```

### 주요 엔드포인트

-   `POST /api/auth/login` - 사용자 로그인
-   `GET /api/posts` - 게시글 목록 조회
-   `POST /api/votes` - 투표 참여
-   `GET /api/charts` - 차트 순위 조회
-   `GET /api/concerts` - 콘서트 일정 조회

---

## 🔄 CI/CD 파이프라인

### GitHub Actions 워크플로우

```yaml
1. 코드 푸시
2. 빌드 & 테스트
3. Docker 이미지 생성
4. AWS ECR 업로드
5. Kubernetes 배포 (ArgoCD)
```

---

## 📈 모니터링

-   **애플리케이션 모니터링**: Prometheus + Grafana
-   **로그 수집**: ELK Stack (Elasticsearch, Logstash, Kibana)
-   **에러 트래킹**: Sentry
-   **인프라 모니터링**: AWS CloudWatch

---

## 🤝 기여 방법

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 라이선스

This project is licensed under the MIT License

---

## 👥 팀

-   **Backend**: 정지원
-   **PM**: 정지원
-   **기획**: 정지원, 이승구
-   **디자인**: 정지원(AI)
-   **Android**: 나유성
-   **iOS**: 김송
-   **Crawler**: 정지원, 나영민
-   **DevOps**: 정지원
-   **AI**: 장종화, 나영민

---

## 📞 문의

-   **Email**: jskjw157@gmail.com
-   **Website**: https://fanpulse.com
-   **GitHub Issues**: https://github.com/your-org/fanpulse/issues

---

## 🌟 발전 가능성

-   **AI 기반 추천 시스템**: 팬 맞춤형 콘텐츠 추천
-   **NFT 기반 투표 시스템**: 블록체인 투표 신뢰성 강화
-   **실시간 번역 & 자막**: AI 기반 자동 번역 지원
-   **메타버스 연동**: 가상 콘서트 및 팬미팅 공간 제공

---

**Made with ❤️ by FanPulse Team**
