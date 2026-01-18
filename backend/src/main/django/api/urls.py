"""
URL routing for API endpoints
"""
from django.urls import path
from .views import HealthCheckView, SummarizeView

urlpatterns = [
    path('health', HealthCheckView.as_view(), name='health'),
    path('summarize', SummarizeView.as_view(), name='summarize'),
]
