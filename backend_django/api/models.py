"""
FanPulse Django Models
PostgreSQL 데이터베이스 모델 정의
"""
import uuid
from django.db import models


class BaseModel(models.Model):
    """공통 베이스 모델"""
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        abstract = True


# =============================================
# 사용자 관련 모델
# =============================================

class User(BaseModel):
    """사용자 테이블"""
    username = models.CharField(max_length=50, unique=True)
    email = models.EmailField(max_length=100, unique=True)
    password_hash = models.TextField(null=True, blank=True)

    class Meta:
        db_table = 'users'


class AuthToken(BaseModel):
    """인증 토큰 테이블"""
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='auth_tokens')
    access_token = models.TextField(unique=True)
    access_expires_at = models.DateTimeField()
    refresh_token = models.TextField(unique=True, null=True, blank=True)
    refresh_expires_at = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = 'auth_tokens'


class OAuthAccount(BaseModel):
    """OAuth 소셜 로그인 계정"""
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='oauth_accounts')
    provider = models.CharField(max_length=20)
    provider_user_id = models.CharField(max_length=255)
    email = models.EmailField(max_length=100, null=True, blank=True)

    class Meta:
        db_table = 'oauth_accounts'
        unique_together = ['provider', 'provider_user_id']


class UserSettings(BaseModel):
    """사용자 설정 테이블"""
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='settings')
    theme = models.CharField(max_length=10, default='light')
    language = models.CharField(max_length=10, default='ko')
    push_enabled = models.BooleanField(default=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'user_settings'


class UserDailyMission(BaseModel):
    """사용자 일일 미션"""
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='daily_missions')
    mission_type = models.CharField(max_length=50)
    count = models.IntegerField(default=0)
    last_updated_at = models.DateTimeField(auto_now=True)
    reset_date = models.DateField()

    class Meta:
        db_table = 'user_daily_missions'


# =============================================
# 아티스트 관련 모델
# =============================================

class Artist(BaseModel):
    """아티스트 테이블"""
    name = models.CharField(max_length=100)
    debut_date = models.DateField(null=True, blank=True)
    agency = models.CharField(max_length=100, null=True, blank=True)
    genre = models.CharField(max_length=50, null=True, blank=True)
    fandom_name = models.CharField(max_length=50, null=True, blank=True)
    profile_image_url = models.TextField(null=True, blank=True)
    description = models.TextField(null=True, blank=True)

    class Meta:
        db_table = 'artists'


class UserFavorite(BaseModel):
    """사용자 팔로우 테이블"""
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='favorites')
    artist = models.ForeignKey(Artist, on_delete=models.CASCADE, related_name='followers')

    class Meta:
        db_table = 'user_favorites'
        unique_together = ['user', 'artist']


# =============================================
# 투표 시스템 모델
# =============================================

class Poll(BaseModel):
    """투표 진행 테이블"""
    title = models.CharField(max_length=100)
    description = models.TextField(null=True, blank=True)
    expires_at = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = 'polls'


class VoteOption(models.Model):
    """투표 옵션 테이블"""
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    poll = models.ForeignKey(Poll, on_delete=models.CASCADE, related_name='options')
    option_text = models.TextField()

    class Meta:
        db_table = 'vote_options'


class Vote(BaseModel):
    """투표 테이블"""
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='votes')
    vote_option = models.ForeignKey(VoteOption, on_delete=models.CASCADE, related_name='votes')
    poll = models.ForeignKey(Poll, on_delete=models.CASCADE, related_name='votes')

    class Meta:
        db_table = 'votes'
        unique_together = ['user', 'poll']


class VotingPower(models.Model):
    """투표권 테이블"""
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='voting_power')
    daily_votes = models.IntegerField(default=1)
    bonus_votes = models.IntegerField(default=0)
    used_today = models.IntegerField(default=0)
    last_reset_date = models.DateField(null=True, blank=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'voting_power'


# =============================================
# 포인트/멤버십 모델
# =============================================

class Point(models.Model):
    """포인트 테이블"""
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='points')
    amount = models.IntegerField(default=0)
    earned_points = models.IntegerField(default=0)
    spent_points = models.IntegerField(default=0)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'points'


class PointTransaction(BaseModel):
    """포인트 기록 테이블"""
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='point_transactions')
    transaction_type = models.CharField(max_length=20)
    amount = models.IntegerField()
    source = models.CharField(max_length=50, null=True, blank=True)
    description = models.TextField(null=True, blank=True)

    class Meta:
        db_table = 'point_transactions'


class Membership(BaseModel):
    """멤버십 테이블"""
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='memberships')
    membership_type = models.CharField(max_length=20)
    start_date = models.DateTimeField()
    end_date = models.DateTimeField(null=True, blank=True)
    is_active = models.BooleanField(default=True)

    class Meta:
        db_table = 'memberships'


class Reward(BaseModel):
    """포인트 교환 상품 테이블"""
    name = models.CharField(max_length=200)
    description = models.TextField(null=True, blank=True)
    required_points = models.IntegerField()
    image_url = models.TextField(null=True, blank=True)
    category = models.CharField(max_length=50, null=True, blank=True)
    stock = models.IntegerField(default=-1)
    is_active = models.BooleanField(default=True)

    class Meta:
        db_table = 'rewards'


# =============================================
# 소셜/알림 모델
# =============================================

class Notification(BaseModel):
    """알림 테이블"""
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='notifications')
    message = models.TextField()
    is_read = models.BooleanField(default=False)

    class Meta:
        db_table = 'notifications'


class Media(models.Model):
    """미디어 테이블"""
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='media')
    media_url = models.TextField()
    media_type = models.CharField(max_length=50)
    uploaded_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'media'


class Like(BaseModel):
    """좋아요 테이블"""
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='likes')
    target_type = models.CharField(max_length=20)
    target_id = models.UUIDField()

    class Meta:
        db_table = 'likes'
        unique_together = ['user', 'target_type', 'target_id']


class SavedPost(BaseModel):
    """저장한 게시물 테이블"""
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='saved_posts')
    post_id = models.CharField(max_length=24)

    class Meta:
        db_table = 'saved_posts'
        unique_together = ['user', 'post_id']


class SearchHistory(BaseModel):
    """검색 기록 테이블"""
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='search_history')
    keyword = models.CharField(max_length=100)

    class Meta:
        db_table = 'search_history'


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


# =============================================
# 라이브 스트리밍 모델
# =============================================

class StreamingEvent(BaseModel):
    """라이브 스트리밍 이벤트 테이블"""
    title = models.CharField(max_length=255)
    description = models.TextField(null=True, blank=True)
    stream_url = models.TextField(null=True, blank=True)
    thumbnail_url = models.TextField(null=True, blank=True)
    artist = models.ForeignKey(Artist, on_delete=models.SET_NULL, null=True, blank=True, related_name='streaming_events')
    scheduled_at = models.DateTimeField(null=True, blank=True)
    started_at = models.DateTimeField(null=True, blank=True)
    ended_at = models.DateTimeField(null=True, blank=True)
    status = models.CharField(max_length=20, default='SCHEDULED')
    viewer_count = models.IntegerField(default=0)

    class Meta:
        db_table = 'streaming_events'


class ChatMessage(BaseModel):
    """라이브 채팅 메시지 테이블"""
    streaming = models.ForeignKey(StreamingEvent, on_delete=models.CASCADE, related_name='chat_messages')
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='chat_messages')
    message = models.CharField(max_length=500)
    is_deleted = models.BooleanField(default=False)

    class Meta:
        db_table = 'chat_messages'


class LiveHeart(BaseModel):
    """라이브 하트 기록 테이블"""
    streaming = models.ForeignKey(StreamingEvent, on_delete=models.CASCADE, related_name='live_hearts')
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='live_hearts')
    count = models.IntegerField(default=1)

    class Meta:
        db_table = 'live_hearts'


# =============================================
# 예매/고객센터 모델
# =============================================

class TicketReservation(BaseModel):
    """예매 내역 테이블"""
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='ticket_reservations')
    concert = models.ForeignKey(CrawledConcert, on_delete=models.CASCADE, related_name='reservations')
    status = models.CharField(max_length=20, default='RESERVED')
    seat_info = models.CharField(max_length=100, null=True, blank=True)
    ticket_count = models.IntegerField(default=1)
    total_price = models.DecimalField(max_digits=10, decimal_places=2, null=True, blank=True)
    payment_method = models.CharField(max_length=30, null=True, blank=True)
    qr_code = models.TextField(null=True, blank=True)
    reserved_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = 'ticket_reservations'


class SupportTicket(BaseModel):
    """고객센터 문의 테이블"""
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='support_tickets')
    title = models.CharField(max_length=200)
    content = models.TextField()
    status = models.CharField(max_length=20, default='PENDING')
    resolved_at = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = 'support_tickets'


class FAQ(BaseModel):
    """FAQ 테이블"""
    question = models.CharField(max_length=500)
    answer = models.TextField()
    category = models.CharField(max_length=50, null=True, blank=True)
    display_order = models.IntegerField(default=0)
    is_active = models.BooleanField(default=True)

    class Meta:
        db_table = 'faq'


class Notice(BaseModel):
    """공지사항 테이블"""
    title = models.CharField(max_length=200)
    content = models.TextField()
    is_pinned = models.BooleanField(default=False)
    published_at = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = 'notices'
