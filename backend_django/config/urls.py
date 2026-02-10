"""
#######################
# URL 라우팅 설정
#######################
# 이 파일은 프로젝트의 모든 URL 라우팅을 정의합니다.
#
# URL 구조:
# - /admin/     : Django 관리자 페이지
# - /api/       : 요약 API 엔드포인트 (api/urls.py로 위임)
# - /swagger/   : Swagger UI (API 문서 & 테스트)
# - /redoc/     : ReDoc UI (대안 API 문서)
#
# API 문서 접근:
# - Swagger UI: http://localhost:8000/swagger/
# - ReDoc: http://localhost:8000/redoc/
# - OpenAPI JSON: http://localhost:8000/swagger.json
# - OpenAPI YAML: http://localhost:8000/swagger.yaml
#######################
"""
from django.contrib import admin
from django.urls import path, include, re_path
from rest_framework import permissions
from drf_yasg.views import get_schema_view
from drf_yasg import openapi


#######################
# Swagger/OpenAPI 설정
#######################
# drf-yasg를 사용한 API 문서 자동 생성 설정
schema_view = get_schema_view(
    #######################
    # API 정보 정의
    #######################
    openapi.Info(
        title="뉴스 요약 API",           # API 제목
        default_version='v1',            # API 버전
        description="""
## 뉴스 요약 API 문서

이 API는 뉴스 기사 URL 또는 텍스트를 입력받아 요약을 생성합니다.

### 주요 기능
- **URL 요약**: 뉴스 URL에서 기사를 추출하여 요약
- **텍스트 요약**: 직접 입력한 텍스트를 요약
- **요약 방식 선택**: 알고리즘(rule) 또는 AI 모델(ai)

### 지원 언어
- 한국어 (ko)
- 영어 (en)
        """,
        terms_of_service="https://www.google.com/policies/terms/",
        contact=openapi.Contact(email="contact@example.com"),
        license=openapi.License(name="MIT License"),
    ),
    public=True,  # 공개 API (인증 없이 문서 접근 가능)
    permission_classes=(permissions.AllowAny,),  # 모든 사용자 접근 허용
)


#######################
# URL 패턴 정의
#######################
urlpatterns = [
    #######################
    # Django 관리자 페이지
    #######################
    # 접근: http://localhost:8000/admin/
    # 용도: 데이터베이스 관리, 사용자 관리 등
    path('admin/', admin.site.urls),

    #######################
    # API 엔드포인트
    #######################
    # 실제 API 라우팅은 api/urls.py에서 정의
    # - /api/health : 서버 상태 확인
    # - /api/summarize : 요약 API
    path('api/', include('api.urls')),

    #######################
    # Swagger UI
    #######################
    # 접근: http://localhost:8000/swagger/
    # 용도: API 문서 확인 및 직접 테스트
    # cache_timeout=0: 캐시 비활성화 (개발 중 변경사항 즉시 반영)
    path('swagger/', schema_view.with_ui('swagger', cache_timeout=0), name='schema-swagger-ui'),

    #######################
    # ReDoc UI
    #######################
    # 접근: http://localhost:8000/redoc/
    # 용도: Swagger의 대안 문서 UI (더 깔끔한 디자인)
    path('redoc/', schema_view.with_ui('redoc', cache_timeout=0), name='schema-redoc'),

    #######################
    # OpenAPI 스펙 파일
    #######################
    # JSON 형식: http://localhost:8000/swagger.json
    # YAML 형식: http://localhost:8000/swagger.yaml
    # 용도: 다른 도구에서 API 스펙 가져오기 (Postman 등)
    re_path(r'^swagger(?P<format>\.json|\.yaml)$', schema_view.without_ui(cache_timeout=0), name='schema-json'),
]
