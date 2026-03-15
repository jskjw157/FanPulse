"""
URL routing for API endpoints

AI Sidecar 전용 엔드포인트만 유지합니다.
제거된 엔드포인트:
- /api/news/db (DBNewsListView, DBNewsDetailView) - Spring이 DB 직접 조회
- /api/ai/filter/rules (CommentFilterRuleListView) - CRUD는 Spring 담당
- /api/ai/filter/logs (FilteredCommentLogListView) - 로그 조회는 Spring 담당
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
    # 댓글 필터링 AI 서비스 API (유지)
    CommentFilterTestView,
    CommentFilterBatchView,
    # AI 모더레이션 API (유지)
    AIModerationCheckView,
    AIModerationBatchView,
    AIModerationStatusView,
)

urlpatterns = [
    path('health', HealthCheckView.as_view(), name='health'),
    path('ai/summarize', SummarizeView.as_view(), name='summarize'),
    path('news/search', NewsSearchView.as_view(), name='news-search'),
    path('news/saved', SavedNewsListView.as_view(), name='saved-news-list'),
    path('news/saved/<str:filename>', SavedNewsDetailView.as_view(), name='saved-news-detail'),
    # 배치 요약 및 요약 결과 조회
    path('news/batch-summarize', BatchSummarizeView.as_view(), name='batch-summarize'),
    path('news/summarized', SummarizedNewsListView.as_view(), name='summarized-news-list'),
    path('news/summarized/<str:filename>', SummarizedNewsDetailView.as_view(), name='summarized-news-detail'),

    # 댓글 자동 필터링 API (/api/ai/* 접두사)
    path('ai/filter', CommentFilterTestView.as_view(), name='comment-filter-test'),
    path('ai/filter/batch', CommentFilterBatchView.as_view(), name='comment-filter-batch'),
    # NOTE: /api/ai/filter/rules 제거됨 - CRUD는 Spring 담당
    # NOTE: /api/ai/filter/logs 제거됨 - 로그 조회는 Spring 담당

    # AI 모더레이션 API
    path('ai/moderate', AIModerationCheckView.as_view(), name='ai-moderation-check'),
    path('ai/moderate/batch', AIModerationBatchView.as_view(), name='ai-moderation-batch'),
    path('ai/moderate/status', AIModerationStatusView.as_view(), name='ai-moderation-status'),
]
