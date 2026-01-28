"""
뉴스 도메인 모델
크롤링 데이터 관련 테이블
"""
import uuid
from django.db import models
from common.models import BaseModel


# =============================================
# 크롤링 데이터 모델
# =============================================

class CrawledNews(BaseModel):
    """크롤링된 뉴스 테이블"""
    title = models.CharField(max_length=255)
    content = models.TextField(null=True, blank=True)
    origin_news = models.TextField(null=True, blank=True)  # 뉴스 데이터 원본 (원문 링크에서 추출)
    thumbnail_url = models.TextField(null=True, blank=True)
    url = models.CharField(max_length=500)
    source = models.CharField(max_length=100, null=True, blank=True)
    published_at = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = 'crawled_news'


class CrawledChart(models.Model):
    """크롤링된 차트 순위 테이블"""
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    chart_source = models.CharField(max_length=100)
    chart_period = models.CharField(max_length=20)
    as_of = models.DateTimeField()
    rank = models.IntegerField()
    previous_rank = models.IntegerField(null=True, blank=True)
    rank_delta = models.IntegerField(null=True, blank=True)
    artist = models.CharField(max_length=255)
    song = models.CharField(max_length=255)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'crawled_charts'


class CrawledChartHistory(models.Model):
    """크롤링된 차트 히스토리 테이블"""
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    chart_source = models.CharField(max_length=100)
    chart_period = models.CharField(max_length=20)
    as_of = models.DateTimeField()
    rank = models.IntegerField()
    artist = models.CharField(max_length=255)
    song = models.CharField(max_length=255)
    crawled_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'crawled_charts_history'


class CrawledConcert(BaseModel):
    """크롤링된 콘서트 일정 테이블"""
    event_name = models.CharField(max_length=255)
    artist = models.CharField(max_length=255, null=True, blank=True)
    venue = models.CharField(max_length=255, null=True, blank=True)
    date = models.DateTimeField(null=True, blank=True)
    ticket_link = models.CharField(max_length=500, null=True, blank=True)
    price_min = models.DecimalField(max_digits=10, decimal_places=2, null=True, blank=True)
    price_max = models.DecimalField(max_digits=10, decimal_places=2, null=True, blank=True)

    class Meta:
        db_table = 'crawled_concerts'


class CrawledAd(models.Model):
    """크롤링된 광고 상품 테이블"""
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    product_name = models.CharField(max_length=200)
    description = models.TextField(null=True, blank=True)
    price = models.DecimalField(max_digits=10, decimal_places=2, null=True, blank=True)
    image_url = models.TextField(null=True, blank=True)
    source = models.CharField(max_length=50, null=True, blank=True)
    product_url = models.TextField(null=True, blank=True)
    is_event = models.BooleanField(default=False)
    crawled_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'crawled_ads'
