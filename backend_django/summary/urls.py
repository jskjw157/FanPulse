"""
요약 도메인 URL 라우팅
"""
from django.urls import path
from .views import (
    SummarizeView,
    BatchSummarizeView,
    SummarizedNewsListView,
    SummarizedNewsDetailView,
)

urlpatterns = [
    path('summarize', SummarizeView.as_view(), name='summarize'),
    path('news/batch-summarize', BatchSummarizeView.as_view(), name='batch-summarize'),
    path('news/summarized', SummarizedNewsListView.as_view(), name='summarized-news-list'),
    path('news/summarized/<str:filename>', SummarizedNewsDetailView.as_view(), name='summarized-news-detail'),
]
