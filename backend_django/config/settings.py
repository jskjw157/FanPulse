"""
#######################
# Django 설정 파일
#######################
# 이 파일은 Django 프로젝트의 모든 설정을 관리합니다.
#
# 주요 설정 영역:
# - 기본 설정 (SECRET_KEY, DEBUG 등)
# - 앱 등록 (INSTALLED_APPS)
# - 미들웨어 (MIDDLEWARE)
# - 데이터베이스 (DATABASES)
# - REST Framework 설정
# - CORS 설정 (프론트엔드 연동)
# - 로깅 설정
#
# 참고: 프로덕션 환경에서는 환경변수 사용 권장
#######################
"""

from pathlib import Path
import os
from dotenv import load_dotenv

#######################
# 기본 경로 설정
#######################
# BASE_DIR: 프로젝트 루트 디렉토리 (manage.py가 있는 위치)
BASE_DIR = Path(__file__).resolve().parent.parent

# .env 파일 로드
load_dotenv(BASE_DIR / '.env')


#######################
# 보안 설정
#######################
# SECRET_KEY: Django에서 암호화에 사용하는 키
# 주의: 프로덕션에서는 환경변수로 관리해야 함!
SECRET_KEY = 'django-insecure-local-dev-key-change-in-production'

# DEBUG: 개발 모드 여부
# True: 상세한 에러 페이지 표시 (개발용)
# False: 보안 에러 페이지 표시 (프로덕션용)
DEBUG = True

# ALLOWED_HOSTS: 이 서버에 접근 가능한 호스트 목록
# 프로덕션에서는 실제 도메인 추가 필요
ALLOWED_HOSTS = ['localhost', '127.0.0.1', '0.0.0.0']


#######################
# 앱 등록
#######################
INSTALLED_APPS = [
    #######################
    # Django 기본 앱
    #######################
    'django.contrib.admin',         # 관리자 페이지
    'django.contrib.auth',          # 인증 시스템
    'django.contrib.contenttypes',  # 콘텐츠 타입
    'django.contrib.sessions',      # 세션 관리
    'django.contrib.messages',      # 메시지 프레임워크
    'django.contrib.staticfiles',   # 정적 파일 관리

    #######################
    # 서드파티 앱
    #######################
    'rest_framework',   # Django REST Framework (API 개발)
    'corsheaders',      # CORS 헤더 처리 (프론트엔드 연동)
    'drf_yasg',         # Swagger/OpenAPI 문서 자동 생성

    #######################
    # 로컬 앱 (직접 개발한 앱)
    #######################
    'api',  # 요약 API 앱
]


#######################
# 미들웨어 설정
#######################
# 미들웨어: 요청/응답 처리 파이프라인
# 순서가 중요함! (위에서 아래로 요청 처리, 아래에서 위로 응답 처리)
MIDDLEWARE = [
    'django.middleware.security.SecurityMiddleware',      # 보안 헤더 추가
    'corsheaders.middleware.CorsMiddleware',              # CORS 처리 (CommonMiddleware 앞에 위치해야 함!)
    'django.contrib.sessions.middleware.SessionMiddleware',  # 세션 처리
    'django.middleware.common.CommonMiddleware',          # 공통 처리 (URL 정규화 등)
    'django.middleware.csrf.CsrfViewMiddleware',          # CSRF 보호
    'django.contrib.auth.middleware.AuthenticationMiddleware',  # 인증 처리
    'django.contrib.messages.middleware.MessageMiddleware',     # 메시지 처리
    'django.middleware.clickjacking.XFrameOptionsMiddleware',   # 클릭재킹 방지
]


#######################
# URL 설정
#######################
# 루트 URL 설정 파일 경로
ROOT_URLCONF = 'config.urls'


#######################
# 템플릿 설정
#######################
# 이 프로젝트는 API 서버이므로 템플릿을 거의 사용하지 않음
TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]


#######################
# WSGI 설정
#######################
# 프로덕션 배포 시 사용 (gunicorn 등)
WSGI_APPLICATION = 'config.wsgi.application'


#######################
# 데이터베이스 설정
#######################
# 현재: SQLite (개발용, 파일 기반)
# 프로덕션: PostgreSQL 또는 MongoDB 권장
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',  # SQLite 엔진
        'NAME': BASE_DIR / 'db.sqlite3',         # DB 파일 경로
    }
}


#######################
# 비밀번호 검증 설정
#######################
# 사용자 비밀번호 생성 시 적용되는 검증 규칙
AUTH_PASSWORD_VALIDATORS = [
    {
        'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator',
    },
]


#######################
# 국제화 설정
#######################
LANGUAGE_CODE = 'ko-kr'      # 기본 언어: 한국어
TIME_ZONE = 'Asia/Seoul'     # 시간대: 서울
USE_I18N = True              # 국제화 활성화
USE_TZ = True                # 시간대 인식 활성화


#######################
# 정적 파일 설정
#######################
# CSS, JavaScript, 이미지 등 정적 파일 URL
STATIC_URL = 'static/'


#######################
# 기본 키 필드 타입
#######################
# 모델의 자동 생성 ID 필드 타입
DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'


#######################
# REST Framework 설정
#######################
# Django REST Framework 관련 설정
REST_FRAMEWORK = {
    # 응답 렌더러: JSON만 사용
    'DEFAULT_RENDERER_CLASSES': [
        'rest_framework.renderers.JSONRenderer',
    ],
    # 요청 파서: JSON만 허용
    'DEFAULT_PARSER_CLASSES': [
        'rest_framework.parsers.JSONParser',
    ],
    # 예외 처리기
    'EXCEPTION_HANDLER': 'rest_framework.views.exception_handler',
}


#######################
# CORS 설정
#######################
# CORS (Cross-Origin Resource Sharing)
# 프론트엔드가 다른 도메인에서 API를 호출할 수 있도록 허용

# 허용된 출처 목록 (프론트엔드 개발 서버)
CORS_ALLOWED_ORIGINS = [
    'http://localhost:5173',  # Vite 개발 서버 기본 포트
]

# 인증 정보(쿠키 등) 포함 허용
CORS_ALLOW_CREDENTIALS = True


#######################
# 로깅 설정
#######################
# 로그 출력 설정 (디버깅 및 모니터링용)
LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,  # 기존 로거 유지

    #######################
    # 포매터 정의
    #######################
    'formatters': {
        'verbose': {
            # 출력 형식: [레벨] 시간 모듈명 메시지
            'format': '{levelname} {asctime} {module} {message}',
            'style': '{',
        },
    },

    #######################
    # 핸들러 정의
    #######################
    'handlers': {
        'console': {
            'class': 'logging.StreamHandler',  # 콘솔 출력
            'formatter': 'verbose',
        },
    },

    #######################
    # 루트 로거 설정
    #######################
    'root': {
        'handlers': ['console'],
        'level': 'INFO',  # INFO 이상 로그만 출력
    },

    #######################
    # 앱별 로거 설정
    #######################
    'loggers': {
        'api': {
            'handlers': ['console'],
            'level': 'DEBUG',      # api 앱은 DEBUG 레벨까지 출력
            'propagate': False,    # 루트 로거로 전파하지 않음
        },
    },
}
