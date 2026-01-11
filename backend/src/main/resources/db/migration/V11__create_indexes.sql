-- V11__create_indexes.sql
-- Performance optimization indexes

-- =====================================================
-- USERS INDEXES
-- =====================================================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_created_at ON users(created_at DESC);

-- =====================================================
-- AUTH_TOKENS INDEXES
-- =====================================================
CREATE INDEX idx_auth_tokens_user_id ON auth_tokens(user_id);
CREATE INDEX idx_auth_tokens_access_expires ON auth_tokens(access_expires_at);
CREATE INDEX idx_auth_tokens_refresh_expires ON auth_tokens(refresh_expires_at);

-- =====================================================
-- OAUTH_ACCOUNTS INDEXES
-- =====================================================
CREATE INDEX idx_oauth_accounts_user_id ON oauth_accounts(user_id);
CREATE INDEX idx_oauth_accounts_provider ON oauth_accounts(provider);

-- =====================================================
-- POLLS INDEXES
-- =====================================================
CREATE INDEX idx_polls_status ON polls(status);
CREATE INDEX idx_polls_category ON polls(category);
CREATE INDEX idx_polls_expires_at ON polls(expires_at);
CREATE INDEX idx_polls_created_at ON polls(created_at DESC);

-- =====================================================
-- VOTE_OPTIONS INDEXES
-- =====================================================
CREATE INDEX idx_vote_options_poll_id ON vote_options(poll_id);
CREATE INDEX idx_vote_options_vote_count ON vote_options(vote_count DESC);

-- =====================================================
-- VOTES INDEXES
-- =====================================================
CREATE INDEX idx_votes_user_id ON votes(user_id);
CREATE INDEX idx_votes_poll_id ON votes(poll_id);
CREATE INDEX idx_votes_poll_created ON votes(poll_id, created_at DESC);

-- =====================================================
-- VOTING_POWER INDEXES
-- =====================================================
CREATE INDEX idx_voting_power_last_reset ON voting_power(last_reset_date);

-- =====================================================
-- POINTS INDEXES
-- =====================================================
CREATE INDEX idx_points_amount ON points(amount DESC);

-- =====================================================
-- POINT_TRANSACTIONS INDEXES
-- =====================================================
CREATE INDEX idx_point_transactions_user_id ON point_transactions(user_id);
CREATE INDEX idx_point_transactions_type ON point_transactions(transaction_type);
CREATE INDEX idx_point_transactions_created_at ON point_transactions(created_at DESC);
CREATE INDEX idx_point_transactions_user_created ON point_transactions(user_id, created_at DESC);

-- =====================================================
-- MEMBERSHIPS INDEXES
-- =====================================================
CREATE INDEX idx_memberships_user_id ON memberships(user_id);
CREATE INDEX idx_memberships_type ON memberships(membership_type);
CREATE INDEX idx_memberships_active ON memberships(is_active);
CREATE INDEX idx_memberships_end_date ON memberships(end_date);

-- =====================================================
-- USER_DAILY_MISSIONS INDEXES
-- =====================================================
CREATE INDEX idx_user_daily_missions_user_id ON user_daily_missions(user_id);
CREATE INDEX idx_user_daily_missions_reset_date ON user_daily_missions(reset_date);

-- =====================================================
-- STREAMING_EVENTS INDEXES
-- =====================================================
CREATE INDEX idx_streaming_events_status ON streaming_events(status);
CREATE INDEX idx_streaming_events_artist_id ON streaming_events(artist_id);
CREATE INDEX idx_streaming_events_scheduled ON streaming_events(scheduled_at);
CREATE INDEX idx_streaming_events_status_scheduled ON streaming_events(status, scheduled_at);

-- =====================================================
-- CHAT_MESSAGES INDEXES
-- =====================================================
CREATE INDEX idx_chat_messages_streaming_id ON chat_messages(streaming_id);
CREATE INDEX idx_chat_messages_user_id ON chat_messages(user_id);
CREATE INDEX idx_chat_messages_created_at ON chat_messages(created_at DESC);
CREATE INDEX idx_chat_messages_streaming_created ON chat_messages(streaming_id, created_at DESC);

-- =====================================================
-- LIVE_HEARTS INDEXES
-- =====================================================
CREATE INDEX idx_live_hearts_streaming_id ON live_hearts(streaming_id);
CREATE INDEX idx_live_hearts_user_id ON live_hearts(user_id);

-- =====================================================
-- CRAWLED_NEWS INDEXES
-- =====================================================
CREATE INDEX idx_crawled_news_source ON crawled_news(source);
CREATE INDEX idx_crawled_news_published_at ON crawled_news(published_at DESC);
CREATE INDEX idx_crawled_news_created_at ON crawled_news(created_at DESC);

-- =====================================================
-- CRAWLED_CHARTS INDEXES
-- =====================================================
CREATE INDEX idx_crawled_charts_source_period ON crawled_charts(chart_source, chart_period);
CREATE INDEX idx_crawled_charts_as_of ON crawled_charts(as_of DESC);
CREATE INDEX idx_crawled_charts_rank ON crawled_charts(rank);
CREATE INDEX idx_crawled_charts_artist ON crawled_charts(artist);

-- =====================================================
-- CRAWLED_CHARTS_HISTORY INDEXES
-- =====================================================
CREATE INDEX idx_crawled_charts_history_source_period ON crawled_charts_history(chart_source, chart_period);
CREATE INDEX idx_crawled_charts_history_as_of ON crawled_charts_history(as_of DESC);
CREATE INDEX idx_crawled_charts_history_artist_song ON crawled_charts_history(artist, song);

-- =====================================================
-- CRAWLED_CONCERTS INDEXES
-- =====================================================
CREATE INDEX idx_crawled_concerts_date ON crawled_concerts(date);
CREATE INDEX idx_crawled_concerts_artist ON crawled_concerts(artist);

-- =====================================================
-- CRAWLED_ADS INDEXES
-- =====================================================
CREATE INDEX idx_crawled_ads_source ON crawled_ads(source);
CREATE INDEX idx_crawled_ads_is_event ON crawled_ads(is_event);
CREATE INDEX idx_crawled_ads_crawled_at ON crawled_ads(crawled_at DESC);

-- =====================================================
-- NOTIFICATIONS INDEXES
-- =====================================================
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at DESC);

-- =====================================================
-- MEDIA INDEXES
-- =====================================================
CREATE INDEX idx_media_user_id ON media(user_id);
CREATE INDEX idx_media_type ON media(media_type);

-- =====================================================
-- LIKES INDEXES
-- =====================================================
CREATE INDEX idx_likes_user_id ON likes(user_id);
CREATE INDEX idx_likes_target ON likes(target_type, target_id);

-- =====================================================
-- USER_FAVORITES INDEXES
-- =====================================================
CREATE INDEX idx_user_favorites_user_id ON user_favorites(user_id);
CREATE INDEX idx_user_favorites_artist_id ON user_favorites(artist_id);

-- =====================================================
-- SAVED_POSTS INDEXES
-- =====================================================
CREATE INDEX idx_saved_posts_user_id ON saved_posts(user_id);
CREATE INDEX idx_saved_posts_post_id ON saved_posts(post_id);
CREATE INDEX idx_saved_posts_user_created ON saved_posts(user_id, created_at DESC);

-- =====================================================
-- SUPPORT_TICKETS INDEXES
-- =====================================================
CREATE INDEX idx_support_tickets_user_id ON support_tickets(user_id);
CREATE INDEX idx_support_tickets_status ON support_tickets(status);
CREATE INDEX idx_support_tickets_created_at ON support_tickets(created_at DESC);

-- =====================================================
-- FAQ INDEXES
-- =====================================================
CREATE INDEX idx_faq_category ON faq(category);
CREATE INDEX idx_faq_is_active ON faq(is_active);
CREATE INDEX idx_faq_display_order ON faq(display_order);

-- =====================================================
-- NOTICES INDEXES
-- =====================================================
CREATE INDEX idx_notices_is_pinned ON notices(is_pinned);
CREATE INDEX idx_notices_published_at ON notices(published_at DESC);

-- =====================================================
-- SEARCH_HISTORY INDEXES
-- =====================================================
CREATE INDEX idx_search_history_user_id ON search_history(user_id);
CREATE INDEX idx_search_history_keyword ON search_history(keyword);
CREATE INDEX idx_search_history_user_created ON search_history(user_id, created_at DESC);

-- =====================================================
-- TICKET_RESERVATIONS INDEXES
-- =====================================================
CREATE INDEX idx_ticket_reservations_user_id ON ticket_reservations(user_id);
CREATE INDEX idx_ticket_reservations_concert_id ON ticket_reservations(concert_id);
CREATE INDEX idx_ticket_reservations_status ON ticket_reservations(status);
CREATE INDEX idx_ticket_reservations_reserved_at ON ticket_reservations(reserved_at DESC);

-- =====================================================
-- ARTISTS INDEXES
-- =====================================================
CREATE INDEX idx_artists_name ON artists(name);
CREATE INDEX idx_artists_agency ON artists(agency);

-- =====================================================
-- REWARDS INDEXES
-- =====================================================
CREATE INDEX idx_rewards_category ON rewards(category);
CREATE INDEX idx_rewards_is_active ON rewards(is_active);
CREATE INDEX idx_rewards_required_points ON rewards(required_points);
