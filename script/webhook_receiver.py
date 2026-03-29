#!/usr/bin/env python3
"""
PR Review Webhook Receiver
GitHub Actions → POST → 이 서버 → cokacdir --cron → Claude Code 세션 생성 → 자동 리뷰

포트 8788에서 실행, Cloudflare Named Tunnel(webhook.atelierpopo.shop)로 외부 노출
"""

import json
import subprocess
import hmac
import hashlib
import os
import sys
from http.server import HTTPServer, BaseHTTPRequestHandler
from datetime import datetime

COKACDIR = "/usr/local/bin/cokacdir"
CHAT_ID = "7787917549"
API_KEY = "ed63edaed666d89a"
WEBHOOK_SECRET = os.environ.get("WEBHOOK_SECRET", "")
PORT = 8788


def verify_signature(payload: bytes, signature: str) -> bool:
    """GitHub webhook signature 검증 (WEBHOOK_SECRET 설정 시)"""
    if not WEBHOOK_SECRET:
        return True
    expected = "sha256=" + hmac.new(
        WEBHOOK_SECRET.encode(), payload, hashlib.sha256
    ).hexdigest()
    return hmac.compare_digest(expected, signature)


class WebhookHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        """헬스체크"""
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps({"status": "ok", "service": "pr-review-webhook"}).encode())

    def do_POST(self):
        content_length = int(self.headers.get("Content-Length", 0))
        payload = self.rfile.read(content_length)

        # signature 검증
        sig = self.headers.get("X-Hub-Signature-256", "")
        if WEBHOOK_SECRET and not verify_signature(payload, sig):
            self.send_response(403)
            self.end_headers()
            self.wfile.write(b'{"status":"error","message":"invalid signature"}')
            return

        try:
            data = json.loads(payload)
        except json.JSONDecodeError:
            self.send_response(400)
            self.end_headers()
            self.wfile.write(b'{"status":"error","message":"invalid json"}')
            return

        # PR review 요청 처리
        required = ["repo", "pr", "title", "author", "head", "base", "sha", "event", "url"]
        if not all(k in data for k in required):
            self.send_response(400)
            self.end_headers()
            self.wfile.write(b'{"status":"error","message":"missing fields"}')
            return

        prompt = (
            f"[PR-REVIEW-REQUEST]\n"
            f"repo: {data['repo']}\n"
            f"pr: #{data['pr']}\n"
            f"title: {data['title']}\n"
            f"author: {data['author']}\n"
            f"head: {data['head']}\n"
            f"base: {data['base']}\n"
            f"sha: {data['sha']}\n"
            f"event: {data['event']}\n"
            f"url: {data['url']}"
        )

        # cokacdir로 즉시 세션 예약 (30초 후)
        try:
            result = subprocess.run(
                [COKACDIR, "--cron", prompt, "--at", "30s", "--once",
                 "--chat", CHAT_ID, "--key", API_KEY],
                capture_output=True, text=True, timeout=10
            )
            log(f"PR #{data['pr']} review scheduled: {result.stdout.strip()}")
        except Exception as e:
            log(f"cokacdir error: {e}")
            self.send_response(500)
            self.end_headers()
            self.wfile.write(json.dumps({"status": "error", "message": str(e)}).encode())
            return

        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps({"status": "ok", "pr": data["pr"]}).encode())

    def log_message(self, format, *args):
        log(f"{self.client_address[0]} - {format % args}")


def log(msg):
    ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{ts}] {msg}", flush=True)


if __name__ == "__main__":
    log(f"Starting webhook receiver on port {PORT}")
    server = HTTPServer(("0.0.0.0", PORT), WebhookHandler)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        log("Shutting down")
        server.server_close()
