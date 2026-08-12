# Android 연동 가이드

본 문서는 **FanPulse Android 클라이언트(`FanPulse_AOS/`)** 가 백엔드 API 와 연동할 때 필요한 진입점, 인증 흐름, 로컬 개발 환경 구성을 정리한다.

- 대상: 안드로이드 개발자
- 백엔드 베이스: `http://<host>:8080`
- 인증 방식: JWT (Access / Refresh) — httpOnly 쿠키(웹) 또는 `Authorization: Bearer` 헤더(모바일)

---

## 1. 로컬 백엔드 기동

저장소 루트에서:

```bash
docker compose up -d postgres spring
# 헬스체크
curl -fsSL http://localhost:8080/actuator/health
```

기본 설정에서 다음 환경 변수가 자동 적용된다 (`docker-compose.yml` 참고):

| 변수 | 기본값 | 비고 |
|---|---|---|
| `FANPULSE_DEV_LOGIN_ENABLED` | `true` (compose 한정) | dev/QA 전용 토큰 발급 토글 |
| `JWT_SECRET` | 임의 기본값 | production 에서 반드시 교체 |
| `GOOGLE_CLIENT_ID` | 테스트용 placeholder | 실제 OAuth 사용 시 교체 |

---

## 2. dev/QA 전용 토큰 발급 (백도어)

Google OAuth 설정 없이 즉시 access/refresh 토큰을 발급받아 보호된 API 를 호출하기 위한 진입점.

### 2.1 엔드포인트

```
POST /api/v1/admin/dev-login
Content-Type: application/json
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `userId` | UUID? | 발급 대상 사용자 UUID (선택) |
| `email`  | String? | 발급 대상 사용자 이메일 (선택) |

세 가지 호출 패턴:

| 요청 본문 | 동작 |
|---|---|
| `{}` (빈 본문) | `devlogin@fanpulse.local` 공유 테스트 계정으로 발급. 미존재 시 자동 생성 |
| `{"email":"qa@example.com"}` | 해당 이메일 사용자로 발급. 미존재 시 자동 생성 (멱등) |
| `{"userId":"<uuid>"}` | 해당 UUID 사용자로 발급. 미존재 시 `404 RESOURCE_NOT_FOUND` |

### 2.2 응답

```json
{
  "userId": "382a1ba5-1e01-4ee1-80c2-17f871c6be66",
  "email": "devlogin@fanpulse.local",
  "username": "dev_tester",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600,
  "refreshExpiresIn": 604800
}
```

응답과 함께 다음 쿠키도 함께 발급된다 (웹 호환 목적):

```
Set-Cookie: fanpulse_access_token=...; HttpOnly; SameSite=Lax; Path=/; Max-Age=604800
Set-Cookie: fanpulse_refresh_token=...; HttpOnly; SameSite=Lax; Path=/; Max-Age=1209600
```

### 2.3 curl 예제

```bash
# 기본 테스트 계정
curl -X POST http://localhost:8080/api/v1/admin/dev-login \
  -H "Content-Type: application/json" -d '{}'

# 특정 이메일 (미존재 시 자동 생성, 재호출 시 동일 user 반환)
curl -X POST http://localhost:8080/api/v1/admin/dev-login \
  -H "Content-Type: application/json" \
  -d '{"email":"qa.tester@fanpulse.local"}'

# 특정 UUID (미존재 시 404)
curl -X POST http://localhost:8080/api/v1/admin/dev-login \
  -H "Content-Type: application/json" \
  -d '{"userId":"00000000-0000-0000-0000-000000000000"}'
```

### 2.4 안드로이드 클라이언트 코드 예시

```kotlin
// Retrofit 인터페이스
interface DevAuthApi {
    @POST("api/v1/admin/dev-login")
    suspend fun devLogin(@Body req: DevLoginRequest = DevLoginRequest()): DevLoginResponse
}

data class DevLoginRequest(
    val userId: String? = null,
    val email: String? = null
)

data class DevLoginResponse(
    val userId: String,
    val email: String,
    val username: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val refreshExpiresIn: Long
)

// 사용
val res = devAuthApi.devLogin(DevLoginRequest(email = "qa.tester@fanpulse.local"))
tokenStore.save(res.accessToken, res.refreshToken)
```

이후 일반 API 호출에서는 다음 헤더를 첨부한다:

```
Authorization: Bearer <accessToken>
```

### 2.5 보안 메모

- 컨트롤러 빈은 `@ConditionalOnProperty(matchIfMissing=false)` 로 보호된다. 토글 OFF 시 빈 자체가 등록되지 않아 `404`.
- production 환경에서는 `FANPULSE_DEV_LOGIN_ENABLED` 를 **절대 `true`로 설정하지 말 것**.
- `docker-compose.yml` 의 spring 서비스에만 `true` 가 명시되어 있다.

---

## 3. 토큰 갱신 / 로그아웃 / 사용자 조회

dev-login 으로 받은 토큰은 일반 인증 토큰과 동일하게 동작한다.

### 3.1 토큰 갱신

```
POST /api/v1/auth/refresh
Content-Type: application/json

{ "refreshToken": "..." }
```

모바일은 본문, 웹은 쿠키 중 한쪽이라도 유효하면 갱신된다.

### 3.2 로그아웃

```
POST /api/v1/auth/logout
```

응답 시 쿠키 삭제. 모바일 클라이언트는 로컬 토큰 저장소를 별도로 비워야 한다.

### 3.3 현재 사용자 조회

```
GET /api/v1/auth/me
Authorization: Bearer <accessToken>
```

응답:

```json
{
  "authenticated": true,
  "user": { "id": "...", "email": "...", "username": "..." }
}
```

토큰이 만료/위조된 경우 `authenticated: false` 가 반환된다 (401 아님).

---

## 4. 트러블슈팅

| 증상 | 원인 / 조치 |
|---|---|
| `POST /api/v1/admin/dev-login → 404` | 토글 미활성. `FANPULSE_DEV_LOGIN_ENABLED=true` 확인 후 spring 재기동 |
| `email` 지정 시 `400` Username 정규식 에러 | 구버전. 본 브랜치(feature/dev-login) 이후 자동 정규화 적용 |
| `userId` 지정 시 `404 RESOURCE_NOT_FOUND` | 의도된 동작. 시드 사용자 없으면 빈 본문(`{}`) 또는 `email` 패턴 사용 |
| 컨테이너 빌드 후에도 옛 동작 | 워크트리에서 compose 프로젝트명이 디렉터리명을 따라가 이미지가 분리될 수 있음. `docker images | grep spring` 으로 확인 |

---

## 5. 운영 체크리스트 (production)

- [ ] `FANPULSE_DEV_LOGIN_ENABLED` 환경 변수가 설정되어 있지 않은 것을 확인
- [ ] `JWT_SECRET` 을 production 전용 값으로 교체 (256bit 이상)
- [ ] `GOOGLE_CLIENT_ID` 를 실제 OAuth client 로 교체
- [ ] `/api/v1/admin/dev-login` 호출 시 `404` 가 반환되는지 smoke test
