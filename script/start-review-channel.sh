#!/bin/bash
#
# PR Review Webhook Server - Bun HTTP 서버 실행 스크립트
# LaunchAgent에서 호출. webhook 수신 → claude -p 로 PR 리뷰 실행.
#

cd /Users/ohchaeeun/source/FanPulse || exit 1

export PATH="/Users/ohchaeeun/.bun/bin:/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:$PATH"
export LANG="en_US.UTF-8"
export HOME="/Users/ohchaeeun"

# WEBHOOK_SECRET은 환경변수 또는 .env에서 로드
if [ -f .env.review ]; then
  export $(grep -v '^#' .env.review | xargs)
fi

exec bun ./webhook-channel/webhook.ts
