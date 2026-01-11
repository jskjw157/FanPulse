# Feature Development Report: Live Discovery (yt-dlp)

**Branch**: `feature/160-crawling-k-pop-아티스트-youtube-라이브-자동-발견-크롤러`
**Date**: 2026-01-11
**Reviewers**: code-reviewer, doc-writer, backend-architect (병렬 실행)

---

## Executive Summary

| 영역 | 점수 | 상태 |
|------|------|------|
| Code Quality | 60/100 | ⚠️ Critical 이슈 3개 |
| Security | 1 issue | 🔴 Command Injection |
| Documentation | 65% | ⚠️ Interface KDoc 누락 |
| Architecture | 85% | ✅ Repository 위치만 수정 필요 |

**최종 판정**: **CHANGES REQUESTED** - Critical 이슈 해결 후 Merge 가능

---

## 1. Implementation Overview

### 커밋 내역 (4개)
| 커밋 | 내용 |
|------|------|
| `69a3ee9` | yt-dlp 기반 라이브 디스커버리 추가 |
| `51804f8` | 라이브 수집 예시 스크립트 추가 |
| `1e2bca3` | 코드 리뷰 문서 작성 |
| `9669840` | 리뷰 범위 확장 |

### 변경 파일 (29개, +1,851 / -43)

**Backend (Kotlin)**
- `LiveDiscoveryService.kt` / `LiveDiscoveryServiceImpl.kt` - 서비스 계층
- `YtDlpStreamDiscoveryAdapter.kt` - yt-dlp 실행 어댑터
- `YtDlpOutputParser.kt` - JSON 파싱
- `LiveDiscoveryScheduler.kt` - 1시간 주기 스케줄러
- `ArtistChannel.kt` - 아티스트-채널 매핑 도메인
- `StreamingEvent.kt` - platform/external_id 컬럼 추가

**Scripts (Python)**
- `live_concert_collector.py` - 라이브 콘서트 수집
- `query_concerts.py` - 콘서트 조회

---

## 2. Critical Issues (P0 - Must Fix)

### C1. 데이터 정확성: live_status null 처리 오류

**File**: `YtDlpStreamDiscoveryAdapter.kt:123-129`

```kotlin
// 현재 코드 (문제)
private fun mapStatus(liveStatus: String?): StreamingStatus {
    return when (liveStatus) {
        "is_live" -> StreamingStatus.LIVE
        "is_upcoming" -> StreamingStatus.SCHEDULED
        "was_live" -> StreamingStatus.ENDED
        else -> StreamingStatus.ENDED  // ← 예정 라이브도 ENDED 처리됨
    }
}
```

**문제**: `live_status`가 null일 때 무조건 ENDED로 처리하여 예정된 라이브가 종료로 표시됨

**영향**: H006(라이브 목록) 데이터 신뢰도 저하

**수정안**:
```kotlin
private fun mapStatus(liveStatus: String?, releaseTimestamp: Long?): StreamingStatus {
    if (liveStatus != null) {
        return when (liveStatus) {
            "is_live" -> StreamingStatus.LIVE
            "is_upcoming" -> StreamingStatus.SCHEDULED
            "was_live" -> StreamingStatus.ENDED
            else -> StreamingStatus.ENDED
        }
    }
    // Fallback: timestamp 기반 판단
    return if (releaseTimestamp != null &&
               Instant.ofEpochSecond(releaseTimestamp).isAfter(Instant.now())) {
        StreamingStatus.SCHEDULED
    } else {
        StreamingStatus.ENDED
    }
}
```

---

### C2. 안정성: Process Deadlock 위험

**File**: `YtDlpStreamDiscoveryAdapter.kt:58-74`

```kotlin
// 현재 코드 (문제)
val process = ProcessBuilder(command).redirectErrorStream(true).start()
val finished = process.waitFor(config.timeoutMs, TimeUnit.MILLISECONDS)
val output = process.inputStream.bufferedReader().readText()  // waitFor 후 읽기
```

**문제**: stdout 버퍼가 가득 차면 프로세스가 block되어 waitFor가 타임아웃됨

**영향**: 대량 출력 시 랜덤 타임아웃 발생

**수정안**:
```kotlin
val process = ProcessBuilder(command).redirectErrorStream(true).start()

// 출력을 별도 스레드에서 읽기
val outputFuture = CompletableFuture.supplyAsync {
    process.inputStream.bufferedReader().use { it.readText() }
}

val finished = process.waitFor(config.timeoutMs, TimeUnit.MILLISECONDS)
if (!finished) {
    process.destroyForcibly()
    throw IllegalStateException("yt-dlp timed out")
}

val output = outputFuture.get()
```

---

### C3. 보안: Command Injection 취약점

**File**: `YtDlpStreamDiscoveryAdapter.kt:34-36`

```kotlin
// 현재 코드 (문제)
private fun buildChannelStreamsUrl(handle: String): String {
    val normalized = if (handle.startsWith("@")) handle else "@$handle"
    return "https://www.youtube.com/$normalized/streams"  // 검증 없음
}
```

**문제**: channel handle 입력값 검증 없이 외부 프로세스에 전달

**영향**: 악의적 입력으로 시스템 명령 실행 가능

**수정안**:
```kotlin
private fun buildChannelStreamsUrl(handle: String): String {
    require(handle.matches(Regex("^@?[a-zA-Z0-9_-]+$"))) {
        "Invalid channel handle format: $handle"
    }
    val normalized = if (handle.startsWith("@")) handle else "@$handle"
    return "https://www.youtube.com/$normalized/streams"
}
```

---

## 3. Warnings (P1 - Should Fix)

| # | 이슈 | 파일 | 설명 |
|---|------|------|------|
| W1 | Legacy 데이터 중복 | `LiveDiscoveryServiceImpl.kt` | 기존 데이터에 platform/externalId 없으면 중복 삽입 |
| W2 | Metrics 혼재 | `LiveDiscoveryServiceImpl.kt` | 채널 실패를 스트림 실패로 카운트 |
| W3 | 예외 처리 미흡 | `LiveDiscoveryServiceImpl.kt` | 모든 예외를 catch하여 디버깅 어려움 |
| W4 | 동시 실행 위험 | `LiveDiscoveryScheduler.kt` | 스케줄러 중복 실행 방지 없음 |

### W1 수정안
```kotlin
private fun findExistingEvent(stream: DiscoveredStream): StreamingEvent? {
    val byExternalId = eventPort.findByPlatformAndExternalId(stream.platform, stream.externalId)
    if (byExternalId != null) return byExternalId
    // Fallback: legacy 데이터 매칭
    return eventPort.findByStreamUrl(stream.streamUrl)
}
```

### W4 수정안 (ShedLock 사용)
```kotlin
@Scheduled(cron = "...")
@SchedulerLock(name = "liveDiscoveryScheduler", lockAtMostFor = "50m", lockAtLeastFor = "5m")
fun discoverStreams() { ... }
```

---

## 4. Architecture Verification

### 검증 결과

| 항목 | 결과 | 비고 |
|------|------|------|
| DDD Bounded Context | ✅ PASS | discovery/streaming 분리 |
| 헥사고날 아키텍처 | ✅ PASS | Port/Adapter 패턴 적용 |
| **의존성 방향** | ❌ **FAIL** | Repository가 domain에 위치 |
| 플랫폼 확장성 | ✅ PASS | StreamDiscoveryPort로 추가 용이 |
| 테스트 용이성 | ✅ PASS | Port 인터페이스로 Mocking 가능 |
| 계획서 준수 | ✅ PASS | 설계 의도와 일치 |

### 의존성 방향 수정 필요

**현재 (위반)**:
```
domain/discovery/ArtistChannelRepository.kt  ← JPA 의존성 포함
domain/streaming/StreamingEventRepository.kt ← JPA 의존성 포함
```

**권장**:
```
infrastructure/persistence/
├── ArtistChannelJpaAdapter.kt      : ArtistChannelPort
├── ArtistChannelJpaRepository.kt   : JpaRepository
├── StreamingEventJpaAdapter.kt     : StreamingEventPort
└── StreamingEventJpaRepository.kt  : JpaRepository
```

---

## 5. Documentation Status

| 파일 | KDoc/Docstring | 완성도 |
|------|----------------|--------|
| LiveDiscoveryService.kt | ❌ Missing | 20% |
| LiveDiscoveryServiceImpl.kt | ❌ Missing | 30% |
| StreamDiscoveryPort.kt | ❌ Missing | 25% |
| YtDlpStreamDiscoveryAdapter.kt | ❌ Missing | 25% |
| YtDlpOutputParser.kt | ❌ Missing | 40% |
| StreamingEvent.kt | ✅ Methods | 85% |
| ArtistChannel.kt | ✅ Implicit | 75% |
| PLAN_live-discovery-yt-dlp.md | ✅ Excellent | 95% |
| live_concert_collector.py | ✅ Good | 95% |
| query_concerts.py | ✅ Complete | 100% |

**전체 문서화 완성도**: 65%

### 필요한 문서화 작업 (4-5시간)
1. Interface KDoc 추가 (LiveDiscoveryService, StreamDiscoveryPort)
2. Implementation Class KDoc 추가
3. 운영 가이드 작성 (doc/guides/live-discovery-operations.md)

---

## 6. Approval Checklist

- [ ] **C1**: mapStatus() null 처리 수정
- [ ] **C2**: Process stdout 비동기 읽기 구현
- [ ] **C3**: Channel handle 입력 검증 추가
- [ ] **W4**: Scheduler Lock 추가 (권장)
- [ ] **Arch**: Repository를 infrastructure/persistence/로 이동 (권장)
- [ ] **Doc**: Interface KDoc 추가 (권장)

---

## 7. Next Steps

### 즉시 수정 (Merge 전)
1. ✏️ `mapStatus()` timestamp 기반 fallback 추가
2. ✏️ yt-dlp stdout 비동기 읽기 구현
3. ✏️ Channel handle regex 검증 추가

### 권장 수정 (1주일 내)
4. 📁 Repository → infrastructure/persistence/ 이동
5. 📝 Interface KDoc 추가
6. 🔒 ShedLock 스케줄러 잠금 추가
7. 🧪 테스트 커버리지 80% 이상 확보

### 후속 개선
8. ⚡ 병렬 채널 처리로 성능 개선
9. 📊 Distributed Tracing 추가
10. 📖 운영 가이드 문서 작성

---

## 8. References

- `doc/plans/PLAN_live-discovery-yt-dlp.md` - 구현 계획서
- `doc/plans/REVIEW_live-discovery-code.md` - 기존 코드 리뷰
- Issue #160: Live discovery (yt-dlp 기반)

---

**Sign-off**:
- code-reviewer: CHANGES REQUESTED
- doc-writer: DOCUMENTATION INCOMPLETE
- backend-architect: ARCHITECTURE ALIGNED (minor fix needed)
