"""
API URL 라우터 - news, summary 앱으로 위임
하위호환을 위해 기존 /api/* 경로 유지
"""
from django.urls import path, include

urlpatterns = [
    path('', include('news.urls')),
    path('', include('summary.urls')),
]
