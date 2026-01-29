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
    # 댓글 필터링 API
    CommentFilterTestView,
    CommentFilterBatchView,
    CommentFilterRuleListView,
    CommentFilterRuleDetailView,
    FilteredCommentLogListView,
    # AI 모더레이션 API
    AIModerationCheckView,
    AIModerationBatchView,
    AIModerationStatusView,
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

    # 댓글 자동 필터링 API
    path('comments/filter/test', CommentFilterTestView.as_view(), name='comment-filter-test'),
    path('comments/filter/batch', CommentFilterBatchView.as_view(), name='comment-filter-batch'),
    path('comments/filter/rules', CommentFilterRuleListView.as_view(), name='comment-filter-rules'),
    path('comments/filter/rules/<str:rule_id>', CommentFilterRuleDetailView.as_view(), name='comment-filter-rule-detail'),
    path('comments/filter/logs', FilteredCommentLogListView.as_view(), name='filtered-comment-logs'),

    # AI 모더레이션 API
    path('moderation/check', AIModerationCheckView.as_view(), name='ai-moderation-check'),
    path('moderation/batch', AIModerationBatchView.as_view(), name='ai-moderation-batch'),
    path('moderation/status', AIModerationStatusView.as_view(), name='ai-moderation-status'),
]
