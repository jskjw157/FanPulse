"""
URL routing for API endpoints
"""
from django.urls import path
from .views import (
    HealthCheckView,
    SummarizeView,
    NewsSearchView,
    SavedNewsListView,
    SavedNewsDetailView,
    BatchSummarizeView,
    SummarizedNewsListView,
    SummarizedNewsDetailView,
    DBNewsListView,
    DBNewsDetailView,
)

urlpatterns = [
    path('health', HealthCheckView.as_view(), name='health'),
    path('summarize', SummarizeView.as_view(), name='summarize'),
    path('news/search', NewsSearchView.as_view(), name='news-search'),
    path('news/saved', SavedNewsListView.as_view(), name='saved-news-list'),
    path('news/saved/<str:filename>', SavedNewsDetailView.as_view(), name='saved-news-detail'),
    # 배치 요약 및 요약 결과 조회
    path('news/batch-summarize', BatchSummarizeView.as_view(), name='batch-summarize'),
    path('news/summarized', SummarizedNewsListView.as_view(), name='summarized-news-list'),
    path('news/summarized/<str:filename>', SummarizedNewsDetailView.as_view(), name='summarized-news-detail'),
    # PostgreSQL DB 뉴스 조회
    path('news/db', DBNewsListView.as_view(), name='db-news-list'),
    path('news/db/<str:news_id>', DBNewsDetailView.as_view(), name='db-news-detail'),
]
