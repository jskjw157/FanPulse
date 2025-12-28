#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Iterable


DEFAULT_REPO = "jskjw157/FanPulse"

# MVP(4주)에서 "살릴" 기존 이슈 후보(분석 문서 기준)
MVP_ISSUE_NUMBERS = {
    # Backend
    16,
    17,
    18,
    19,
    # Android
    31,
    32,
    36,
    40,
    65,
    70,
    91,
    # iOS
    41,
    42,
    52,
    56,
    71,
    76,
    92,
    # (선택) 드로어
    107,
    108,
}

# MVP와 방향성이 크게 충돌하는 이슈(Phase2로 격리 권장)
PHASE2_CONFLICT_NUMBERS = {
    20,  # MongoDB 컬렉션 설계
}

# MVP와 무관하지만 high-priority가 붙어 있어 혼선이 큰 이슈(Phase2로 내리기 권장)
HIGH_PRIORITY_NON_MVP_NUMBERS = {
    44,
    43,
    34,
    33,
    29,
    24,
    23,
    22,
    21,
    20,
    10,
    9,
    6,
    5,
    2,
}


@dataclass(frozen=True)
class NewIssue:
    title: str
    labels: list[str]
    milestone: str | None
    body: str


def run(cmd: list[str], *, input_text: str | None = None, check: bool = True) -> str:
    proc = subprocess.run(
        cmd,
        input=input_text,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if check and proc.returncode != 0:
        raise RuntimeError(f"Command failed ({proc.returncode}): {' '.join(cmd)}\n{proc.stdout}")
    return proc.stdout


def gh_ok() -> tuple[bool, str]:
    out = run(["gh", "auth", "status", "-h", "github.com"], check=False)
    # gh는 실패해도 exit code 0인 경우가 있어, 출력으로 판별
    if "Failed to log in" in out or "token in default is invalid" in out:
        return False, out.strip()
    if "Logged in to github.com" in out or "Logged in to GitHub.com" in out:
        return True, out.strip()
    # 애매하면 실패로 처리(안전)
    return False, out.strip()


def load_issues_from_files(paths: list[Path]) -> list[dict[str, Any]]:
    issues: list[dict[str, Any]] = []
    for p in paths:
        if not p.exists():
            continue
        with p.open() as f:
            data = json.load(f)
        if isinstance(data, list):
            issues.extend(data)
        else:
            raise ValueError(f"Unexpected JSON format in {p}")
    return [it for it in issues if "pull_request" not in it]


def load_open_issues_from_gh(repo: str) -> list[dict[str, Any]]:
    owner, name = repo.split("/", 1)
    page = 1
    issues: list[dict[str, Any]] = []
    while True:
        # NOTE: `gh api` will treat `-f` as request body and default to POST.
        # For GET query parameters, use `-F` and explicitly set method GET.
        out = run(
            [
                "gh",
                "api",
                "--method",
                "GET",
                f"repos/{owner}/{name}/issues",
                "-F",
                "state=open",
                "-F",
                "per_page=100",
                "-F",
                f"page={page}",
            ]
        )
        batch = json.loads(out)
        batch = [it for it in batch if "pull_request" not in it]
        if not batch:
            break
        issues.extend(batch)
        if len(batch) < 100:
            break
        page += 1
    return issues


def replace_doc_links(body: str) -> str:
    return body.replace("../document/", "../docs/")


def tighten_mvp_scope(body: str, issue_number: int) -> str:
    """
    MVP 결정(소셜=Google만, Live/News=seed upsert, AWS 스트리밍 제외)에 맞게
    일부 핵심 이슈의 본문을 "자동으로 과감하게 삭제"하지 않고, 최소 수준으로 정리한다.
    """
    text = body

    if issue_number in {31, 41, 16}:
        # Providers: Google only
        text = re.sub(r"Google\s*/\s*Kakao\s*/\s*Naver", "Google", text, flags=re.I)
        text = re.sub(r"Google,\s*Kakao,\s*Naver", "Google", text, flags=re.I)
        # Remove Kakao/Naver/Apple mention lines (UI/요구사항)
        lines = text.splitlines()
        filtered: list[str] = []
        for line in lines:
            if re.search(r"\bKakao\b|\bNaver\b|\bApple\b|카카오|네이버|애플", line, flags=re.I):
                continue
            filtered.append(line)
        text = "\n".join(filtered).strip() + "\n"
        text = text.replace("OAuth SDK 연동 (3개)", "OAuth SDK 연동 (Google 1종)")
        text = text.replace("3개 Provider", "Google 1개 Provider")

        # MVP 문서 링크로 유도
        if issue_number in {31, 41}:
            text = re.sub(
                r"\[화면 정의서\]\([^)]+\)",
                "[MVP 화면 정의서](../docs/mvp/mvp_화면_정의서.md)",
                text,
                flags=re.I,
            )

    if issue_number in {19, 30}:
        # MVP 범위 경고 배너를 상단에 추가(내용 삭제는 하지 않음)
        banner = (
            "## ⚠️ MVP 범위 조정\n\n"
            "- 본 이슈는 원래 전체 기능 기준으로 작성되어 있어요.\n"
            "- 4주 MVP에서는 `docs/mvp/*` 및 `docs/mvp/mvp_API_계약.md`에 맞춰 **범위를 축소**해서 진행합니다.\n\n"
        )
        if "⚠️ MVP 범위 조정" not in text:
            text = banner + text.lstrip()

    return text


def body_fix(body: str, issue_number: int) -> str:
    text = replace_doc_links(body or "")
    text = tighten_mvp_scope(text, issue_number)
    return text


def desired_new_issues(repo: str) -> list[NewIssue]:
    # MVP에서 "실제로 필요한데 오픈 이슈에 없음" 위주로 최소 생성
    milestone = "MVP (4주)"
    return [
        NewIssue(
            title="[Backend][MVP] Auth: Email 회원가입/로그인 + /me",
            labels=["backend", "feature", "mvp"],
            milestone=milestone,
            body=(
                "## 📋 범위\n"
                "- `docs/mvp/mvp_API_계약.md` 기준으로 Email/Password 가입/로그인, `/me`를 구현합니다.\n\n"
                "## ✅ 완료 조건\n"
                "- [ ] `POST /api/v1/auth/signup`\n"
                "- [ ] `POST /api/v1/auth/login`\n"
                "- [ ] `GET /api/v1/me`\n"
                "- [ ] 인증 미들웨어/가드(보호 엔드포인트)\n"
                "- [ ] 에러 코드(401/403/409/422 등) 정리\n"
            ),
        ),
        NewIssue(
            title="[Backend][MVP] Auth: Google ID Token 검증 + 계정 연동",
            labels=["backend", "feature", "mvp"],
            milestone=milestone,
            body=(
                "## 📋 범위\n"
                "- 소셜 로그인은 **Google 1종**만 지원합니다.\n"
                "- 클라이언트에서 받은 Google ID Token을 검증하고, 사용자 계정과 연동합니다.\n\n"
                "## ✅ 완료 조건\n"
                "- [ ] `POST /api/v1/auth/google`\n"
                "- [ ] `oauth_accounts(provider=GOOGLE)` 저장/업서트\n"
                "- [ ] 신규/기존 계정 매핑 정책 정의\n"
            ),
        ),
        NewIssue(
            title="[Backend][MVP] Live: 목록/상세 Read API (임베드 URL 기반)",
            labels=["backend", "feature", "mvp"],
            milestone=milestone,
            body=(
                "## 📋 범위\n"
                "- MVP는 자체 스트리밍 서버가 아니라 **외부 플랫폼 임베드**로 제공합니다.\n\n"
                "## ✅ 완료 조건\n"
                "- [ ] `GET /api/v1/live`\n"
                "- [ ] `GET /api/v1/live/{id}`\n"
                "- [ ] `streaming_events` 스키마에 맞춘 응답\n"
            ),
        ),
        NewIssue(
            title="[Backend][MVP] News: 목록/상세 Read API (seed 적재 데이터)",
            labels=["backend", "feature", "mvp"],
            milestone=milestone,
            body=(
                "## 📋 범위\n"
                "- MVP는 seed(큐레이션) → DB upsert로 적재된 뉴스 데이터를 읽습니다.\n\n"
                "## ✅ 완료 조건\n"
                "- [ ] `GET /api/v1/news`\n"
                "- [ ] `GET /api/v1/news/{id}`\n"
                "- [ ] `crawled_news` 스키마에 맞춘 응답\n"
            ),
        ),
        NewIssue(
            title="[Backend][MVP] Search: Live/News 통합 검색 API",
            labels=["backend", "feature", "mvp"],
            milestone=milestone,
            body=(
                "## 📋 범위\n"
                "- MVP 범위: Live/News 대상으로 `q` 파라미터 검색\n\n"
                "## ✅ 완료 조건\n"
                "- [ ] `GET /api/v1/search?q=...`\n"
                "- [ ] 최소 검색 필드(제목/아티스트명) 정의\n"
            ),
        ),
        NewIssue(
            title="[Crawling/Seed][MVP] seed(JSON/CSV) → PostgreSQL upsert 적재 도구",
            labels=["crawling", "feature", "mvp"],
            milestone=milestone,
            body=(
                "## 📋 범위\n"
                "- MVP는 외부 API/크롤링 대신 seed 파일로 `streaming_events`, `crawled_news`를 채웁니다.\n\n"
                "## ✅ 완료 조건\n"
                "- [ ] seed 포맷 확정(JSON/CSV)\n"
                "- [ ] upsert 스크립트(중복 키 정책 포함)\n"
                "- [ ] 실행 방법 문서화(`docs/mvp/mvp_크롤링.md` 갱신)\n"
            ),
        ),
        NewIssue(
            title="[Web][MVP] 반응형 화면 스켈레톤 + 라우팅(전체 플로우)",
            labels=["web", "feature", "mvp"],
            milestone=milestone,
            body=(
                "## 📋 범위\n"
                "- MVP 화면: H001/H002/H002-1/H006/H019/H011/H018/H016/H010/H024\n"
                "- `docs/mvp/mvp_화면_정의서.md` 플로우 기준\n\n"
                "## ✅ 완료 조건\n"
                "- [ ] 라우팅/보호 라우팅\n"
                "- [ ] 더미 데이터로 E2E 플로우 데모\n"
                "- [ ] 모바일/데스크탑 반응형 레이아웃\n"
            ),
        ),
        NewIssue(
            title="[iOS][MVP] 뉴스 상세 화면 (H011)",
            labels=["ios", "feature", "mvp"],
            milestone=milestone,
            body="참고: `docs/mvp/mvp_화면_정의서.md`, `docs/mvp/mvp_API_계약.md`\n",
        ),
        NewIssue(
            title="[Android][MVP] 뉴스 상세 화면 (H011)",
            labels=["android", "feature", "mvp"],
            milestone=milestone,
            body="참고: `docs/mvp/mvp_화면_정의서.md`, `docs/mvp/mvp_API_계약.md`\n",
        ),
        NewIssue(
            title="[iOS][MVP] 검색 화면 (H018) + 최근 검색어 로컬 저장",
            labels=["ios", "feature", "mvp"],
            milestone=milestone,
            body="참고: `docs/mvp/mvp_화면_정의서.md`\n",
        ),
        NewIssue(
            title="[Android][MVP] 검색 화면 (H018) + 최근 검색어 로컬 저장",
            labels=["android", "feature", "mvp"],
            milestone=milestone,
            body="참고: `docs/mvp/mvp_화면_정의서.md`\n",
        ),
        NewIssue(
            title="[iOS][MVP] 설정 화면 (H010) + 로그아웃",
            labels=["ios", "feature", "mvp"],
            milestone=milestone,
            body="참고: `docs/mvp/mvp_화면_정의서.md`\n",
        ),
        NewIssue(
            title="[Android][MVP] 설정 화면 (H010) + 로그아웃",
            labels=["android", "feature", "mvp"],
            milestone=milestone,
            body="참고: `docs/mvp/mvp_화면_정의서.md`\n",
        ),
    ]


def planned_body_updates(issues: Iterable[dict[str, Any]]) -> list[tuple[int, str]]:
    updates: list[tuple[int, str]] = []
    for it in issues:
        num = int(it["number"])
        body = it.get("body") or ""
        new_body = body_fix(body, num)
        if new_body != body:
            updates.append((num, new_body))
    return updates


def main() -> int:
    parser = argparse.ArgumentParser(description="FanPulse MVP 기준 GitHub 이슈 정리/생성 도구")
    parser.add_argument("--repo", default=DEFAULT_REPO, help="owner/repo")
    parser.add_argument(
        "--input-json",
        action="append",
        default=[],
        help="오픈 이슈 JSON 파일 경로(여러 개 가능). 미지정 시 gh api로 조회.",
    )
    parser.add_argument("--apply", action="store_true", help="실제 변경 적용(기본은 dry-run)")
    parser.add_argument("--create-missing", action="store_true", help="MVP 누락 이슈 생성")
    parser.add_argument(
        "--demote-non-mvp-high-priority",
        action="store_true",
        help="MVP 밖 high-priority를 phase2로 내리고 high-priority 라벨 제거",
    )
    args = parser.parse_args()

    if args.input_json:
        issues = load_issues_from_files([Path(p) for p in args.input_json])
    else:
        ok, msg = gh_ok()
        if not ok:
            print("ERROR: gh 인증이 필요합니다.\n" + msg, file=sys.stderr)
            print("hint: `gh auth login -h github.com` 후 다시 실행하세요.", file=sys.stderr)
            return 2
        issues = load_open_issues_from_gh(args.repo)

    by_number = {int(it["number"]): it for it in issues}

    # 1) 본문 링크/범위 수정(안전한 치환 중심)
    body_updates = planned_body_updates(issues)

    # 2) 라벨/마일스톤 정리 계획
    to_add_mvp = sorted([n for n in MVP_ISSUE_NUMBERS if n in by_number])
    to_phase2 = sorted([n for n in PHASE2_CONFLICT_NUMBERS if n in by_number])
    to_demote_hp = sorted([n for n in HIGH_PRIORITY_NON_MVP_NUMBERS if n in by_number])

    print(f"repo: {args.repo}")
    print(f"open_issues_loaded: {len(issues)}")
    print()
    print("== Planned body updates ==")
    print(f"count: {len(body_updates)}")
    for n, _ in body_updates[:40]:
        print(f"- #{n}")
    if len(body_updates) > 40:
        print(f"... (+{len(body_updates) - 40} more)")

    print()
    print("== Planned labels/milestone ==")
    print(f"- add label `mvp`: {to_add_mvp}")
    print(f"- add label `phase2` (conflict): {to_phase2}")
    if args.demote_non_mvp_high_priority:
        print(f"- demote non-mvp high-priority -> phase2: {to_demote_hp}")

    new_issues = desired_new_issues(args.repo) if args.create_missing else []
    if new_issues:
        print()
        print("== Planned new issues ==")
        for it in new_issues:
            print(f"- {it.title} (labels={it.labels}, milestone={it.milestone})")

    if not args.apply:
        print("\n(dry-run) Add `--apply` to execute changes.")
        return 0

    ok, msg = gh_ok()
    if not ok:
        print("ERROR: gh 인증이 필요합니다.\n" + msg, file=sys.stderr)
        return 2

    owner, name = args.repo.split("/", 1)

    # Ensure labels exist (best-effort)
    wanted_labels = {
        "mvp": ("B60205", "4주 MVP 범위"),
        "phase2": ("D4C5F9", "MVP 이후(Phase2)"),
        "web": ("1D76DB", "웹 프론트엔드"),
    }
    existing = run(["gh", "label", "list", "--repo", args.repo, "--limit", "200"])
    existing_names = {line.split("\t", 1)[0].strip() for line in existing.splitlines() if line.strip()}
    for name_, (color, desc) in wanted_labels.items():
        if name_ in existing_names:
            continue
        run(["gh", "label", "create", name_, "--repo", args.repo, "--color", color, "--description", desc])

    # Ensure milestones exist (best-effort)
    wanted_milestones = {
        "MVP (4주)": "로그인/홈/라이브/뉴스/검색 + iOS/Android/반응형 Web",
        "Phase2": "MVP 이후 기능(커뮤니티/투표/차트/콘서트/리워드/인프라 등)",
    }
    ms_out = run(
        [
            "gh",
            "api",
            "--method",
            "GET",
            f"repos/{owner}/{name}/milestones",
            "-F",
            "state=open",
            "-F",
            "per_page=100",
        ]
    )
    ms = json.loads(ms_out)
    ms_titles = {m["title"] for m in ms}
    for title, desc in wanted_milestones.items():
        if title in ms_titles:
            continue
        run(
            [
                "gh",
                "api",
                f"repos/{owner}/{name}/milestones",
                "--method",
                "POST",
                "-f",
                f"title={title}",
                "-f",
                f"description={desc}",
            ]
        )

    # Apply body updates
    for n, new_body in body_updates:
        payload = json.dumps({"body": new_body})
        run(["gh", "api", f"repos/{owner}/{name}/issues/{n}", "--method", "PATCH", "--input", "-"], input_text=payload)

    # Apply labels/milestone to MVP issues
    for n in to_add_mvp:
        run(["gh", "issue", "edit", str(n), "--repo", args.repo, "--add-label", "mvp", "--milestone", "MVP (4주)"])

    for n in to_phase2:
        run(["gh", "issue", "edit", str(n), "--repo", args.repo, "--add-label", "phase2", "--milestone", "Phase2"])

    if args.demote_non_mvp_high_priority:
        for n in to_demote_hp:
            run(
                [
                    "gh",
                    "issue",
                    "edit",
                    str(n),
                    "--repo",
                    args.repo,
                    "--add-label",
                    "phase2",
                    "--remove-label",
                    "high-priority",
                    "--milestone",
                    "Phase2",
                ]
            )

    # Create missing issues
    for it in new_issues:
        body_file = Path(".agent/tmp_issue_body.md")
        body_file.parent.mkdir(parents=True, exist_ok=True)
        body_file.write_text(it.body)
        cmd = [
            "gh",
            "issue",
            "create",
            "--repo",
            args.repo,
            "--title",
            it.title,
            "--body-file",
            str(body_file),
        ]
        if it.labels:
            cmd += ["--label", ",".join(it.labels)]
        if it.milestone:
            cmd += ["--milestone", it.milestone]
        run(cmd)

    print("Done.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
