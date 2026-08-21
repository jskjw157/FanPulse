# 코루틴 환경에서의 Spring `@Transactional` 사용 가이드

## TL;DR

**`suspend` 함수에는 `@Transactional`을 직접 적용하지 않는다.**
Spring의 `@Transactional`은 ThreadLocal 기반이라 코루틴의 컨텍스트 전환(thread-hopping) 시 트랜잭션이 유실된다.
DB 쓰기는 **동기(non-suspend) 메서드**로 위임하고, 그 메서드에 `@Transactional(propagation = REQUIRES_NEW)`를 붙여 트랜잭션 경계를 명확히 한다.

관련 이슈/PR: [#166](https://github.com/jskjw157/FanPulse/issues/166), [#224](https://github.com/jskjw157/FanPulse/issues/224)

---

## 1. 배경 — 왜 이 문제가 발생하나

### 1.1 Spring `@Transactional` 동작 원리

Spring의 `@Transactional`은 두 가지 메커니즘에 의존한다.

1. **AOP 프록시**: `@Transactional` 메서드는 CGLIB/JDK 프록시로 감싸진다. 메서드 진입 시 트랜잭션을 열고, 정상 종료 시 commit, 예외 발생 시 rollback을 호출한다.
2. **ThreadLocal 트랜잭션 컨텍스트**: 열린 트랜잭션은 `TransactionSynchronizationManager`가 ThreadLocal 변수에 저장한다. 같은 스레드 안에서 호출되는 JPA/JDBC 코드는 이 ThreadLocal을 통해 동일한 `EntityManager`/`Connection`을 공유한다.

```
[Caller Thread T1]
  └─ proxy.foo()
       ├─ TX BEGIN  → ThreadLocal[T1] = TxContext
       ├─ foo() 내부 DB 호출 → ThreadLocal[T1]에서 TxContext 조회 → 같은 트랜잭션
       └─ TX COMMIT → ThreadLocal[T1] 정리
```

### 1.2 코루틴과 ThreadLocal의 충돌

Kotlin 코루틴은 **suspend point에서 자유롭게 스레드를 옮긴다.** `Dispatchers.IO`, `Dispatchers.Default`, `withContext` 등 어디든 다른 스레드로 재개될 수 있다.

```kotlin
suspend fun fooSuspend() {
    // T1에서 시작
    delay(100)            // ← suspend point: 재개 시 T2로 이동 가능
    saveToDb()            // T2의 ThreadLocal에는 트랜잭션 컨텍스트가 없음
}
```

`@Transactional`이 코루틴 진입 시 T1에 트랜잭션을 열어도, suspend point 이후 T2로 옮겨가면 ThreadLocal이 텅 빈 상태가 된다. 결과적으로:

- **트랜잭션 미보장**: DB 쓰기가 트랜잭션 밖에서 일어나거나, 다른 트랜잭션에서 일어남
- **commit 누락**: 프록시는 진입 스레드에서만 commit/rollback을 트리거하므로 다른 스레드에서 발생한 변경은 일관성 없는 상태로 남음
- **간헐적 실패**: 스레드 전환 타이밍에 따라 동작이 달라져 디버깅 난해

> Spring 6+에서는 `Dispatchers.IO + TransactionContextElement` 같은 우회법이 있지만, 코드 작성·유지가 까다롭고 사고 패턴이 분산된다. **본 프로젝트는 단순한 위임 패턴을 선호한다.**

---

## 2. 잘못된 패턴: `suspend` + `@Transactional`

### 안티 패턴

```kotlin
// ❌ 하지 말 것
@Service
class MetadataRefreshService(
    private val eventPort: StreamingEventPort,
    private val oEmbedClient: YouTubeOEmbedClient
) {
    @Transactional   // ← 코루틴 컨텍스트에서 ThreadLocal이 유실됨
    suspend fun refreshEvent(eventId: UUID) {
        val event = eventPort.findById(eventId) ?: return
        val metadata = oEmbedClient.fetchMetadata(event.videoId)  // suspend point
        event.updateMetadata(metadata)
        eventPort.save(event)   // ← 다른 스레드에서 실행될 수 있음 → 트랜잭션 누수
    }
}
```

### 무엇이 깨지나

| 증상 | 원인 |
|------|------|
| 변경이 DB에 반영되지 않음 | suspend point 이후 스레드 전환으로 ThreadLocal 트랜잭션 미연결 |
| `LazyInitializationException` | 트랜잭션 밖에서 lazy 컬렉션 접근 |
| 부분 commit / 비결정적 동작 | 일부 호출은 트랜잭션 안, 일부는 밖 — 환경에 따라 결과가 다름 |
| 테스트는 통과, 운영에서 실패 | 단일 스레드 디스패처로 테스트 시 문제가 드러나지 않음 |

---

## 3. 권장 패턴: 동기 메서드에 트랜잭션 위임

`suspend` 함수는 **오케스트레이션**(조회, IO, 분기)만 담당하고, **DB 쓰기는 별도의 동기 컴포넌트 메서드로 추출**한다. 그 동기 메서드에 `@Transactional`을 적용한다.

### 3.1 구조

```
┌─────────────────────────────────────┐
│ MetadataRefreshServiceImpl (suspend)│  ← 트랜잭션 없음
│  - 이벤트 목록 조회                  │
│  - 각 이벤트에 대해 ↓                │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│ TransactionalMetadataUpdater        │  ← @Transactional(REQUIRES_NEW)
│  - oEmbed 호출                       │
│  - 도메인 객체 변경                  │
│  - eventPort.save()                  │
│  - 도메인 이벤트 발행                │
└─────────────────────────────────────┘
```

### 3.2 코드 예시

```kotlin
@Service
class MetadataRefreshServiceImpl(
    private val eventPort: StreamingEventPort,
    private val metadataUpdater: TransactionalMetadataUpdater
) : MetadataRefreshService {

    /**
     * 단일 이벤트 메타데이터를 갱신한다.
     *
     * 트랜잭션 보장: 이 suspend 함수 자체에는 @Transactional을 적용하지 않는다.
     * Spring @Transactional은 ThreadLocal 기반이므로 코루틴 컨텍스트 전환 시
     * 트랜잭션이 소실될 수 있기 때문이다.
     *
     * DB 쓰기는 [TransactionalMetadataUpdater.updateEventMetadata] 호출 한 번만 수행되며,
     * 해당 메서드가 @Transactional(propagation = REQUIRES_NEW)로 독립 트랜잭션을 보장한다.
     */
    override suspend fun refreshEvent(eventId: UUID): Boolean {
        val event = eventPort.findEventById(eventId) ?: return false
        return metadataUpdater.updateEventMetadata(event)   // ← 동기 메서드 호출
    }
}

@Component
class TransactionalMetadataUpdater(
    private val eventPort: StreamingEventPort,
    private val oEmbedClient: YouTubeOEmbedClient
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateEventMetadata(event: StreamingEvent): Boolean {
        val metadata = oEmbedClient.fetchMetadata(event.videoId) ?: return false
        event.updateMetadata(metadata.title, metadata.thumbnailUrl)
        eventPort.save(event)
        return true
    }
}
```

### 3.3 핵심 규칙

1. `suspend` 함수에는 `@Transactional`을 절대 붙이지 않는다.
2. 트랜잭션이 필요한 DB 쓰기는 **동기 메서드 한 번 호출**로 묶는다.
3. 동기 메서드는 **별도 `@Component`** (혹은 별도 클래스의 메서드)에 둔다 — Spring AOP 프록시는 같은 클래스 내부 호출(self-invocation)을 통과하지 않으므로 동일 클래스에 `@Transactional` 메서드를 두면 적용되지 않는다.
4. `suspend` 함수가 여러 번 DB를 쓴다면 **트랜잭션 경계 재설계**가 필요하다 (각 호출이 독립 트랜잭션이 되어 부분 성공이 발생함).

---

## 4. `REQUIRES_NEW`를 쓰는 이유

### 4.1 `REQUIRED` (기본값) vs `REQUIRES_NEW`

| 옵션 | 동작 | 적합 시나리오 |
|------|------|--------------|
| `REQUIRED` | 호출자에 트랜잭션이 있으면 참여, 없으면 새로 시작 | 단일 요청-응답 흐름, 호출자와 운명 공유 |
| `REQUIRES_NEW` | 호출자 트랜잭션을 일시 중단하고 **항상 새 트랜잭션**을 연다 | 배치/스케줄러에서 **개별 항목 격리** |

### 4.2 배치 시나리오에서의 격리

`MetadataRefreshService.refreshLiveEvents()`는 N개의 이벤트를 순회하며 각각 `updateEventMetadata`를 호출한다. 이때 `REQUIRES_NEW`의 효과:

- 이벤트 1 실패(예: oEmbed 5xx) → 트랜잭션 1만 rollback
- 이벤트 2 성공 → 트랜잭션 2 commit (이벤트 1과 무관)
- **부분 성공(partial success)이 의도된 동작**

만약 `REQUIRED`(또는 `@Transactional` 없음 + 외부 트랜잭션)였다면 한 이벤트의 실패가 전체 배치를 rollback시켜 이미 commit된 다른 이벤트의 변경까지 잃을 위험이 있다.

### 4.3 비용

- 새 트랜잭션 = 새 DB 커넥션 (또는 풀에서 추가 획득)
- 호출자 트랜잭션을 일시 중단 → 컨텍스트 스위칭 오버헤드
- → **무분별한 사용은 커넥션 풀 고갈을 일으킬 수 있다.** "각 항목을 독립적으로 처리해야 한다"는 명확한 요구가 있을 때만 사용.

---

## 5. 회귀 가드 (테스트)

`@Transactional`이 다시 추가되거나 트랜잭션 위임이 깨지는 회귀를 막기 위해 **격리 동작 자체를 검증하는 통합 테스트**를 둔다.

```kotlin
@Test
@DisplayName("should isolate transactions between events (REQUIRES_NEW)")
fun shouldIsolateTransactionsBetweenEvents() {
    // given: 두 이벤트 — 하나는 oEmbed 성공, 하나는 5xx 실패
    val successEvent = createEvent("successVid1", LIVE)
    val failureEvent = createEvent("failureVid1", LIVE)
    repository.saveAll(listOf(successEvent, failureEvent))

    stubOEmbedSuccess("successVid1", "Successfully Updated")
    wireMockServer.stubFor(
        get(urlPathEqualTo("/oembed"))
            .withQueryParam("url", containing("failureVid1"))
            .willReturn(aResponse().withStatus(500))
    )

    // when
    val result = runBlocking { metadataRefreshService.refreshLiveEvents() }

    // then: 성공 이벤트는 commit, 실패 이벤트는 rollback
    assertEquals(1, result.updated)
    assertEquals(1, result.failed)
    assertEquals("Successfully Updated", repository.findById(successEvent.id).get().title)
    assertEquals("Old Title", repository.findById(failureEvent.id).get().title)
}
```

전체 배치를 감싼 외부 트랜잭션이 실수로 도입되면 이 어서션이 깨지므로 회귀를 즉시 감지한다.

전체 코드: `backend/src/test/kotlin/com/fanpulse/integration/MetadataRefreshIntegrationTest.kt`

---

## 6. 체크리스트

코드 리뷰/구현 시 확인.

- [ ] `suspend` 함수에 `@Transactional`이 붙어 있지 않다.
- [ ] DB 쓰기를 수행하는 코드는 **별도 `@Component`의 동기 메서드**로 분리되어 있다.
- [ ] 그 동기 메서드에 `@Transactional`이 적용되어 있다.
- [ ] **배치/순회 처리**라면 `propagation = REQUIRES_NEW`를 사용해 항목 간 격리를 보장한다.
- [ ] 같은 클래스 내부에서 `@Transactional` 메서드를 self-invocation으로 호출하지 않는다 (AOP 프록시 우회됨).
- [ ] 부분 성공 시 통계(updated/failed)와 errors가 정확히 누적되는지 테스트로 검증한다.

---

## 7. 참고

- 이슈 #166 — `MetadataRefreshService`의 suspend `@Transactional` 제거
- 이슈 #224 — `runCatching`과 `@Transactional`의 상호작용 (예외가 catch되어 rollback이 트리거되지 않는 문제)
- Spring Reference: [Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- Kotlin Coroutines: [Coroutine context and dispatchers](https://kotlinlang.org/docs/coroutines-and-channels.html)
