#!/usr/bin/env python3
"""
PR Review Webhook Receiver
GitHub Actions → POST → 이 서버 → Claude Code CLI 직접 호출 → 자동 리뷰

포트 8788에서 실행, Cloudflare Named Tunnel(webhook.atelierpopo.shop)로 외부 노출
"""

import json
import subprocess
import hmac
import hashlib
import os
import threading
from http.server import HTTPServer, BaseHTTPRequestHandler
from datetime import datetime

CLAUDE_CLI = "/opt/homebrew/bin/claude"
COKACDIR = "/usr/local/bin/cokacdir"
CHAT_ID = os.environ.get("COKACDIR_CHAT_ID", "7787917549")
API_KEY = os.environ.get("COKACDIR_API_KEY", "ed63edaed666d89a")
WEBHOOK_SECRET = os.environ.get("WEBHOOK_SECRET", "")
PROJECT_DIR = "/Users/ohchaeeun/source/FanPulse"
PORT = 8788
REVIEW_TIMEOUT = 600  # 10분

# 동시 리뷰 방지
active_reviews = {}
lock = threading.Lock()


def verify_signature(payload: bytes, signature: str) -> bool:
    if not WEBHOOK_SECRET:
        return True
    expected = "sha256=" + hmac.new(
        WEBHOOK_SECRET.encode(), payload, hashlib.sha256
    ).hexdigest()
    return hmac.compare_digest(expected, signature)


def run_review(data: dict):
    """백그라운드에서 Claude Code CLI로 PR 리뷰 실행"""
    pr_num = data["pr"]
    try:
        log(f"PR #{pr_num} review started")

        prompt = (
            f"[PR-REVIEW-REQUEST]\n"
            f"repo: {data['repo']}\n"
            f"pr: #{pr_num}\n"
            f"title: {data['title']}\n"
            f"author: {data['author']}\n"
            f"head: {data['head']}\n"
            f"base: {data['base']}\n"
            f"sha: {data['sha']}\n"
            f"event: {data['event']}\n"
            f"url: {data['url']}"
        )

        env = os.environ.copy()
        env["LANG"] = "en_US.UTF-8"
        env["PATH"] = "/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:" + env.get("PATH", "")

        result = subprocess.run(
            [CLAUDE_CLI, "-p", prompt, "--allowedTools",
             "Bash(gh *),Bash(git *),Read,Grep,Glob",
             "--output-format", "text", "--max-turns", "30"],
            capture_output=True, text=True, timeout=REVIEW_TIMEOUT,
            cwd=PROJECT_DIR, env=env
        )

        log(f"PR #{pr_num} review completed (exit={result.returncode})")
        if result.stderr.strip():
            log(f"PR #{pr_num} stderr: {result.stderr[:500]}")

        # Telegram 알림
        notify_telegram(pr_num, result.returncode == 0)

    except subprocess.TimeoutExpired:
        log(f"PR #{pr_num} review timed out ({REVIEW_TIMEOUT}s)")
        notify_telegram(pr_num, False, "timeout")
    except Exception as e:
        log(f"PR #{pr_num} review error: {e}")
        notify_telegram(pr_num, False, str(e))
    finally:
        with lock:
            active_reviews.pop(pr_num, None)


def notify_telegram(pr_num: int, success: bool, error: str = ""):
    """리뷰 결과를 Telegram으로 알림"""
    if success:
        msg = f"PR #{pr_num} 자동 리뷰 완료"
    else:
        msg = f"PR #{pr_num} 자동 리뷰 실패: {error}" if error else f"PR #{pr_num} 자동 리뷰 실패"
    try:
        subprocess.run(
            [COKACDIR, "--sendfile", "/dev/null", "--chat", CHAT_ID, "--key", API_KEY],
            capture_output=True, timeout=5
        )
    except Exception:
        pass
    log(f"Telegram notify: {msg}")


class WebhookHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        resp = {"status": "ok", "service": "pr-review-webhook",
                "active_reviews": list(active_reviews.keys())}
        self.wfile.write(json.dumps(resp).encode())

    def do_POST(self):
        content_length = int(self.headers.get("Content-Length", 0))
        payload = self.rfile.read(content_length)

        sig = self.headers.get("X-Hub-Signature-256", "")
        if WEBHOOK_SECRET and not verify_signature(payload, sig):
            self._respond(403, {"status": "error", "message": "invalid signature"})
            return

        try:
            data = json.loads(payload)
        except json.JSONDecodeError:
            self._respond(400, {"status": "error", "message": "invalid json"})
            return

        required = ["repo", "pr", "title", "author", "head", "base", "sha", "event", "url"]
        if not all(k in data for k in required):
            self._respond(400, {"status": "error", "message": "missing fields"})
            return

        pr_num = data["pr"]

        # 동일 PR 리뷰 중이면 스킵
        with lock:
            if pr_num in active_reviews:
                log(f"PR #{pr_num} already being reviewed, skipping")
                self._respond(200, {"status": "ok", "pr": pr_num, "action": "skipped"})
                return
            active_reviews[pr_num] = data["sha"]

        # 백그라운드에서 리뷰 실행
        thread = threading.Thread(target=run_review, args=(data,), daemon=True)
        thread.start()

        log(f"PR #{pr_num} review queued (sha: {data['sha'][:8]})")
        self._respond(200, {"status": "ok", "pr": pr_num, "action": "queued"})

    def _respond(self, code: int, body: dict):
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps(body).encode())

    def log_message(self, format, *args):
        log(f"{self.client_address[0]} - {format % args}")


def log(msg):
    ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{ts}] {msg}", flush=True)


if __name__ == "__main__":
    log(f"Starting webhook receiver on port {PORT}")
    log(f"Claude CLI: {CLAUDE_CLI}")
    log(f"Project dir: {PROJECT_DIR}")
    server = HTTPServer(("127.0.0.1", PORT), WebhookHandler)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        log("Shutting down")
        server.server_close()
