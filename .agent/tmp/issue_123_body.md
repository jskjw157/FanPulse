## 📋 범위
- seed로 입력된 `streaming_events.stream_url`(YouTube embed) 기준으로 제목/썸네일을 보강하거나 status를 갱신합니다.

## ✅ 완료 조건
- [ ] VIDEO_ID 추출 로직 정의
- [ ] (옵션) YouTube Data API 사용 시: 키/쿼터/에러 처리 정책
- [ ] DB 업데이트 정책(어떤 필드를 덮어쓸지)

## ⚠️ 주의
- 일정 리스크가 커서, Week4에 RSS upsert(Stretch A) 완료 후 착수
