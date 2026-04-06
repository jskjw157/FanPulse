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
    "",
    "## 리뷰 규칙",
    "- `gh pr diff ${pr번호}`로 diff만 리뷰. diff에 없는 파일은 절대 리뷰하지 마.",
    "- 이슈 심각도를 반드시 아래 형식으로 분류:",
    "  - 🔴 **Critical**: 버그, 보안, 데이터 손실 위험",
    "  - 🟡 **Medium**: 설계 개선, 테스트 누락, 설정 불일치",
    "  - 🟢 **Low**: 컨벤션, 네이밍, 코드 스타일",
    "- 각 이슈마다 해당 코드 스니펫과 수정 방향을 제시",
    "- AI 관련 표기(Co-Authored-By, Generated with Claude 등) 절대 금지",
    "- 한국어로 작성",
  ].join("\n");
}

// 동시 리뷰 방지
const activeReviews = new Set<number>();

// 커밋 상태 세팅 (claude-code-review status check)
async function setCommitStatus(
  repo: string,
  sha: string,
  state: "pending" | "success" | "failure",
  description: string
): Promise<void> {
  try {
    const proc = Bun.spawn(
      [
        "gh", "api",
        `repos/${repo}/statuses/${sha}`,
        "-f", `state=${state}`,
        "-f", "context=claude-code-review",
        "-f", `description=${description}`,
      ],
      {
        cwd: PROJECT_DIR,
        env: { ...process.env, HOME: "/Users/ohchaeeun" },
        stdout: "pipe",
        stderr: "pipe",
      }
    );
    const exitCode = await proc.exited;
    if (exitCode !== 0) {
      const stderr = await new Response(proc.stderr).text();
      log(`setCommitStatus failed (${state}): ${stderr.slice(0, 200)}`);
    }
  } catch (err) {
    log(`setCommitStatus error: ${err}`);
  }
}

// PR에 리뷰 코멘트 포스트
async function postReviewComment(prNum: number, body: string): Promise<void> {
  try {
    const proc = Bun.spawn(
      ["gh", "pr", "comment", String(prNum), "--body", body],
      {
        cwd: PROJECT_DIR,
        env: { ...process.env, HOME: "/Users/ohchaeeun" },
        stdout: "pipe",
        stderr: "pipe",
      }
    );
    const exitCode = await proc.exited;
    if (exitCode === 0) {
      log(`PR #${prNum} review comment posted`);
    } else {
      const stderr = await new Response(proc.stderr).text();
      log(`PR #${prNum} comment post failed: ${stderr.slice(0, 200)}`);
    }
  } catch (err) {
    log(`PR #${prNum} comment post error: ${err}`);
  }
}

// claude -p 실행으로 리뷰 수행
async function runReview(prompt: string, prNum: number, repo: string, sha: string): Promise<void> {
  log(`PR #${prNum} starting review...`);
  await setCommitStatus(repo, sha, "pending", "코드 리뷰 진행 중");

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

    // 15분 타임아웃 (process kill → pipe EOF → Promise.all 해제)
    let timedOut = false;
    const timeout = setTimeout(() => {
      timedOut = true;
      proc.kill();
      activeReviews.delete(prNum);
      log(`PR #${prNum} review timed out (15min)`);
    }, 15 * 60 * 1000);

    // stdout/stderr 동시에 읽어 pipe buffer deadlock 방지
    const [stdoutText, stderrText, exitCode] = await Promise.all([
      new Response(proc.stdout).text(),
      new Response(proc.stderr).text(),
      proc.exited,
    ]);

    clearTimeout(timeout);
    activeReviews.delete(prNum);

    if (timedOut) {
      await setCommitStatus(repo, sha, "failure", "리뷰 타임아웃 (15분)");
      return;
    }

    if (exitCode === 0) {
      log(`PR #${prNum} review completed`);
      const reviewText = stdoutText.trim();
      if (reviewText) {
        const body = reviewText;
        await postReviewComment(prNum, body);
      }
      // 🔴 Critical 이슈가 있으면 failure, 없으면 success
      const hasCritical = reviewText.includes("🔴");
      await setCommitStatus(
        repo, sha,
        hasCritical ? "failure" : "success",
        hasCritical ? "Critical 이슈 발견 — 수정 필요" : "리뷰 완료 — 이슈 없음"
      );
    } else {
      log(`PR #${prNum} review failed (exit ${exitCode}): ${stderrText.slice(0, 300)}`);
      await setCommitStatus(repo, sha, "failure", "리뷰 실행 오류");
    }
  } catch (err) {
    activeReviews.delete(prNum);
    log(`PR #${prNum} review spawn error: ${err}`);
    await setCommitStatus(repo, sha, "failure", "리뷰 실행 오류");
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
    runReview(prompt, prNum, String(data.repo), String(data.sha));

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
