"""
뉴스 도메인 URL 라우팅
"""
from django.urls import path
from .views import (
    HealthCheckView,
    NewsSearchView,
    SavedNewsListView,
    SavedNewsDetailView,
    DBNewsListView,
    DBNewsDetailView,
)

urlpatterns = [
    path('health', HealthCheckView.as_view(), name='health'),
    path('news/search', NewsSearchView.as_view(), name='news-search'),
    path('news/saved', SavedNewsListView.as_view(), name='saved-news-list'),
    path('news/saved/<str:filename>', SavedNewsDetailView.as_view(), name='saved-news-detail'),
    # PostgreSQL DB 뉴스 조회
    path('news/db', DBNewsListView.as_view(), name='db-news-list'),
    path('news/db/<str:news_id>', DBNewsDetailView.as_view(), name='db-news-detail'),
]
