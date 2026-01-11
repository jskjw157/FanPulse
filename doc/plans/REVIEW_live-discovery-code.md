# Code Review: Live Discovery (yt-dlp)

## Scope
- `backend/src/main/kotlin/com/fanpulse/infrastructure/external/youtube/YtDlpStreamDiscoveryAdapter.kt`
- `backend/src/main/kotlin/com/fanpulse/infrastructure/external/youtube/YtDlpOutputParser.kt`
- `backend/src/main/kotlin/com/fanpulse/application/service/LiveDiscoveryServiceImpl.kt`
- `backend/src/main/kotlin/com/fanpulse/domain/streaming/StreamingEvent.kt`
- `backend/src/main/kotlin/com/fanpulse/domain/streaming/port/StreamingEventPort.kt`
- `backend/src/main/kotlin/com/fanpulse/domain/streaming/StreamingEventRepository.kt`
- `backend/src/main/kotlin/com/fanpulse/infrastructure/scheduler/LiveDiscoveryScheduler.kt`
- `backend/src/main/resources/application.yml`
- `backend/src/test/resources/application-test.yml`
- `backend/src/test/kotlin/com/fanpulse/infrastructure/external/youtube/YtDlpOutputParserTest.kt`
- `script/live_concert_collector.py`, `script/query_concerts.py`

## Code Review Summary

### Critical Issues (Must Fix)
- 🔴 [데이터 정확성] live_status가 없을 때 기본값을 `ENDED`로 두어 예정 라이브가 종료로 저장될 수 있음  
  - 위치: `backend/src/main/kotlin/com/fanpulse/infrastructure/external/youtube/YtDlpStreamDiscoveryAdapter.kt`  
  - 문제: yt-dlp 결과에서 `live_status`가 null인 경우가 있는데, 현재 로직은 무조건 `ENDED` 처리  
  - 영향: 예정 라이브가 `ENDED`로 기록되어 H006/H019 데이터 신뢰도 저하  
  - 제안: `release_timestamp`가 미래면 `SCHEDULED`, 과거면 `ENDED`로 보정

### Warnings (Should Fix)
- 🟡 [안정성] yt-dlp 출력 스트림을 `waitFor()` 이후에만 읽음 → 대량 출력 시 데드락 가능  
  - 위치: `backend/src/main/kotlin/com/fanpulse/infrastructure/external/youtube/YtDlpStreamDiscoveryAdapter.kt`  
  - 문제: 프로세스가 stdout 버퍼에 막히면 종료되지 않아 타임아웃 발생 가능  
  - 제안: 출력 스트림을 별도 스레드로 읽거나 `readText()`를 먼저 수행해 버퍼를 비움
- 🟡 [데이터 중복 위험] 기존 `streaming_events`에 `platform/external_id`가 비어있으면 매칭 실패  
  - 위치: `backend/src/main/kotlin/com/fanpulse/application/service/LiveDiscoveryServiceImpl.kt`  
  - 문제: 마이그레이션 이전 데이터는 `findByPlatformAndExternalId`에 매칭되지 않아 중복 삽입 가능  
  - 제안: 초기 기간에 `stream_url` 기반 fallback 매칭 또는 백필 작업 필수

### Suggestions (Nice to Have)
- 🟢 [메트릭 정확도] 채널 오류 1건을 스트림 실패로 카운트하여 실패율 왜곡 가능  
  - 위치: `backend/src/main/kotlin/com/fanpulse/application/service/LiveDiscoveryServiceImpl.kt`  
  - 제안: 채널 실패/스트림 실패를 별도 카운터로 분리
- 🟢 [설정 유연성] `yt-dlp` 실행 옵션을 추가 인자로 분리할 수 있도록 설정 확장  
  - 위치: `backend/src/main/kotlin/com/fanpulse/infrastructure/config/LiveDiscoveryConfig.kt`

### Positive Highlights
- ✨ 스케줄러/서비스/포트 분리로 책임이 명확함
- ✨ `platform/external_id` 키 도입으로 중복 제거 전략이 개선됨
- ✨ 테스트 fixture/파서 테스트 추가로 최소한의 검증 기반 확보

### Testing Gaps
- `live_status = null` + `release_timestamp` 케이스 테스트 부족  
- yt-dlp 대용량 출력 처리(시간 초과/버퍼) 관련 테스트 부재

## DDD 관점 정합성
- `Discovery` 컨텍스트가 `Streaming` 컨텍스트에 이벤트를 upsert하는 구조는 적절함
- `StreamingEvent` Aggregate에 `platform/external_id/source_url`가 추가되어 식별성이 강화됨
- `ArtistChannel`이 Discovery 측 엔티티로 분리된 점은 컨텍스트 경계에 부합

## Open Questions
- 기존 `streaming_events` 데이터 백필 방식(스クリપ트/마이그레이션) 정의 여부?
- yt-dlp 실행 실패 시 재시도 정책은 어디에서 책임질지?

## Recommendations
1. `live_status` null 대응 로직 개선 + 테스트 추가
2. yt-dlp stdout 읽기 방식 개선으로 프로세스 안정성 확보
3. 초기 마이그레이션 동안 `stream_url` fallback 매칭 또는 백필 계획 명시
