/**
 * PR Review Webhook Server
 *
 * Bun HTTP 서버. GitHub Actions → POST → Cloudflare Tunnel → localhost:8788 → claude -p 실행
 *
 * webhook_receiver.py를 대체. Python → TypeScript/Bun으로 마이그레이션.
 */

import { createHmac, timingSafeEqual } from "crypto";

const PORT = Number(process.env.WEBHOOK_PORT) || 8788;
const WEBHOOK_SECRET = process.env.WEBHOOK_SECRET || "";
const CLAUDE_BIN = process.env.CLAUDE_BIN || "/opt/homebrew/bin/claude";
const PROJECT_DIR = process.env.PROJECT_DIR || "/Users/ohchaeeun/source/FanPulse";

// HMAC 서명 검증
function verifySignature(payload: string, signature: string): boolean {
  if (!WEBHOOK_SECRET) return true;
  const expected =
    "sha256=" +
    createHmac("sha256", WEBHOOK_SECRET).update(payload).digest("hex");
  try {
    return timingSafeEqual(Buffer.from(expected), Buffer.from(signature));
  } catch {
    return false;
  }
}

// PR 데이터를 [PR-REVIEW-REQUEST] 프롬프트로 변환
function buildReviewPrompt(data: Record<string, unknown>): string {
  return [
    "[PR-REVIEW-REQUEST]",
    `repo: ${data.repo}`,
    `pr: #${data.pr}`,
    `title: ${data.title}`,
    `author: ${data.author}`,
    `head: ${data.head}`,
    `base: ${data.base}`,
    `sha: ${data.sha}`,
    `event: ${data.event}`,
    `url: ${data.url}`,
  ].join("\n");
}

// 동시 리뷰 방지
const activeReviews = new Set<number>();

// claude -p 실행으로 리뷰 수행
async function runReview(prompt: string, prNum: number): Promise<void> {
  log(`PR #${prNum} starting review...`);

  try {
    const proc = Bun.spawn([CLAUDE_BIN, "-p", "--no-session-persistence"], {
      cwd: PROJECT_DIR,
      env: {
        ...process.env,
        HOME: "/Users/ohchaeeun",
        PATH: "/Users/ohchaeeun/.bun/bin:/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin",
      },
      stdin: new Response(prompt).body!,
      stdout: "pipe",
      stderr: "pipe",
    });

    // 15분 타임아웃
    const timeout = setTimeout(() => {
      proc.kill();
      activeReviews.delete(prNum);
      log(`PR #${prNum} review timed out (15min)`);
    }, 15 * 60 * 1000);

    const exitCode = await proc.exited;
    clearTimeout(timeout);
    activeReviews.delete(prNum);

    if (exitCode === 0) {
      log(`PR #${prNum} review completed`);
    } else {
      const stderr = await new Response(proc.stderr).text();
      log(`PR #${prNum} review failed (exit ${exitCode}): ${stderr.slice(0, 300)}`);
    }
  } catch (err) {
    activeReviews.delete(prNum);
    log(`PR #${prNum} review spawn error: ${err}`);
  }
}

// HTTP 서버
const server = Bun.serve({
  port: PORT,
  hostname: "127.0.0.1",

  async fetch(req) {
    // Health check
    if (req.method === "GET") {
      return Response.json({
        status: "ok",
        service: "pr-review-webhook",
        active_reviews: [...activeReviews],
      });
    }

    if (req.method !== "POST") {
      return Response.json({ status: "error", message: "method not allowed" }, { status: 405 });
    }

    const body = await req.text();

    // HMAC 검증
    const sig = req.headers.get("X-Hub-Signature-256") || "";
    if (WEBHOOK_SECRET && !verifySignature(body, sig)) {
      log("Invalid signature, rejecting");
      return Response.json({ status: "error", message: "invalid signature" }, { status: 403 });
    }

    let data: Record<string, unknown>;
    try {
      data = JSON.parse(body);
    } catch {
      return Response.json({ status: "error", message: "invalid json" }, { status: 400 });
    }

    // 필수 필드 체크
    const required = ["repo", "pr", "title", "author", "head", "base", "sha", "event", "url"];
    const missing = required.filter((k) => !(k in data));
    if (missing.length > 0) {
      return Response.json(
        { status: "error", message: `missing fields: ${missing.join(", ")}` },
        { status: 400 }
      );
    }

    const prNum = Number(data.pr);

    // 동일 PR 리뷰 중이면 스킵
    if (activeReviews.has(prNum)) {
      log(`PR #${prNum} already being reviewed, skipping`);
      return Response.json({ status: "ok", pr: prNum, action: "skipped" });
    }

    activeReviews.add(prNum);

    // 비동기 리뷰 시작 (HTTP 응답은 즉시 반환)
    const prompt = buildReviewPrompt(data);
    runReview(prompt, prNum);

    log(`PR #${prNum} review queued (sha: ${String(data.sha).slice(0, 8)})`);
    return Response.json({ status: "ok", pr: prNum, action: "queued" });
  },
});

function log(msg: string) {
  const ts = new Date().toISOString().replace("T", " ").slice(0, 19);
  console.error(`[${ts}] ${msg}`);
}

log(`Webhook server started on port ${PORT}`);
log(`HMAC verification: ${WEBHOOK_SECRET ? "enabled" : "disabled"}`);
