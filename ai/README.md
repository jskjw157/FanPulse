### 방법. 로컬 개발 (AI Sidecar)

```bash
cd ai

# 1) 환경변수 설정
cp .env.example .env
# .env 파일을 열어 실제 값으로 수정

# 2) 가상환경 생성 및 의존성 설치
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# 3) 마이그레이션 및 실행
python manage.py migrate
python manage.py runserver
```

<b>Swagger 확인</b>
http://localhost:8000/swagger/

### .env (Django AI Sidecar 로컬 실행용)

- `SECRET_KEY` -- Django 비밀키 (프로덕션 시 반드시 변경)
- `DJANGO_DEBUG` -- 디버그 모드 (true/false)
- `ALLOWED_HOSTS` -- 허용 호스트 목록
- `AI_SERVICE_ACCEPTED_KEYS` -- 서비스 인증키
- `NAVER_CLIENT_ID` / `NAVER_CLIENT_SECRET` -- 네이버 뉴스 API (선택)
- `USE_POSTGRES` -- PostgreSQL 사용 여부
- `POSTGRES_*` -- PostgreSQL 접속 정보


# FAQ AI
### 프로그램
PostgreSQL 설치
pgvector 설치 : https://github.com/pgvector/pgvector.git README 참고

### .env
`USE_POSTGRES = true`
`GOOGLE_API_KEY` : https://aistudio.google.com/api-keys 에서 발급

### 라이브러리
google-genai>=1.74.0
pgvector>=0.4.2

### 코드
postgres에서 sql/001_create_database.sql의 `-- FAQ 테이블`과 sql/002_more_faq.sql 실행
first_embeddings.py 실행