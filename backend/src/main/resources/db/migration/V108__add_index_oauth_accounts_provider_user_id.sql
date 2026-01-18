-- Add index for OAuth account lookup performance
-- findByProviderAndProviderUserId is called on every OAuth login
CREATE INDEX IF NOT EXISTS idx_oauth_accounts_provider_user_id
    ON oauth_accounts(provider, provider_user_id);
