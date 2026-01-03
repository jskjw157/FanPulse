#!/usr/bin/env python3
"""
PR Analyzer
GitHub PR의 변경사항을 분석하여 파일별 영향도, 리스크 수준, 리뷰 우선순위를 JSON으로 출력
"""

import json
import subprocess
import re
from pathlib import Path
from typing import Dict, List, Any, Optional
from dataclasses import dataclass, asdict


@dataclass
class FileChange:
    """파일 변경 정보"""
    path: str
    additions: int
    deletions: int
    status: str  # added, modified, deleted, renamed
    risk_level: str  # critical, high, medium, low
    risk_reasons: List[str]
    review_priority: int  # 1 (highest) ~ 5 (lowest)


class PrAnalyzer:
    """GitHub PR 분석기"""

    # 고위험 파일 패턴
    CRITICAL_PATTERNS = [
        (r"\.env", "환경 변수 파일"),
        (r"secrets?\.ya?ml", "시크릿 설정"),
        (r"credentials", "인증 정보"),
        (r"auth", "인증 관련"),
        (r"security", "보안 관련"),
        (r"password|passwd", "비밀번호 관련"),
    ]

    HIGH_RISK_PATTERNS = [
        (r"config.*\.(ya?ml|json|toml)", "설정 파일"),
        (r"docker", "Docker 설정"),
        (r"k8s|kubernetes", "Kubernetes 설정"),
        (r"ci|cd|pipeline", "CI/CD 파이프라인"),
        (r"build\.gradle|pom\.xml|package\.json", "빌드 설정"),
        (r"migrations?/", "DB 마이그레이션"),
        (r"schema", "스키마 변경"),
    ]

    MEDIUM_RISK_PATTERNS = [
        (r"(service|controller|handler)\.(kt|java|py|ts|js)$", "핵심 비즈니스 로직"),
        (r"(repository|dao)\.(kt|java|py|ts|js)$", "데이터 접근 계층"),
        (r"api/", "API 엔드포인트"),
    ]

    def __init__(self, pr_number: Optional[int] = None, base_branch: str = "main"):
        self.pr_number = pr_number
        self.base_branch = base_branch
        self.files: List[FileChange] = []

    def analyze(self) -> Dict[str, Any]:
        """PR 분석 실행"""
        print(f"🔍 Analyzing PR{'#' + str(self.pr_number) if self.pr_number else ''}...\n")

        if self.pr_number:
            self._analyze_pr()
        else:
            self._analyze_local_diff()

        self._calculate_priorities()

        return self._generate_report()

    def _analyze_pr(self) -> None:
        """GitHub PR 분석"""
        try:
            # PR 파일 목록 가져오기
            result = subprocess.run(
                ["gh", "pr", "view", str(self.pr_number), "--json", "files,additions,deletions"],
                capture_output=True,
                text=True,
                timeout=30
            )

            if result.returncode != 0:
                print(f"⚠️  Failed to fetch PR: {result.stderr}")
                return

            data = json.loads(result.stdout)

            for file in data.get("files", []):
                self._analyze_file(
                    path=file.get("path", ""),
                    additions=file.get("additions", 0),
                    deletions=file.get("deletions", 0),
                    status="modified"
                )

            print(f"✅ Analyzed {len(self.files)} files from PR #{self.pr_number}")

        except FileNotFoundError:
            print("⚠️  gh CLI not found. Install: https://cli.github.com")
        except subprocess.TimeoutExpired:
            print("⚠️  gh command timeout")
        except Exception as e:
            print(f"⚠️  Error analyzing PR: {e}")

    def _analyze_local_diff(self) -> None:
        """로컬 git diff 분석"""
        try:
            # git diff --stat으로 변경 파일 목록 가져오기
            result = subprocess.run(
                ["git", "diff", "--numstat", f"{self.base_branch}...HEAD"],
                capture_output=True,
                text=True,
                timeout=30
            )

            if result.returncode != 0:
                # base branch와 비교 실패 시 HEAD와 비교
                result = subprocess.run(
                    ["git", "diff", "--numstat", "HEAD"],
                    capture_output=True,
                    text=True,
                    timeout=30
                )

            for line in result.stdout.strip().split("\n"):
                if not line:
                    continue

                parts = line.split("\t")
                if len(parts) >= 3:
                    additions = int(parts[0]) if parts[0] != "-" else 0
                    deletions = int(parts[1]) if parts[1] != "-" else 0
                    path = parts[2]

                    self._analyze_file(
                        path=path,
                        additions=additions,
                        deletions=deletions,
                        status="modified"
                    )

            print(f"✅ Analyzed {len(self.files)} changed files")

        except Exception as e:
            print(f"⚠️  Error analyzing local diff: {e}")

    def _analyze_file(self, path: str, additions: int, deletions: int, status: str) -> None:
        """개별 파일 분석"""
        risk_level, risk_reasons = self._assess_risk(path, additions, deletions)

        self.files.append(FileChange(
            path=path,
            additions=additions,
            deletions=deletions,
            status=status,
            risk_level=risk_level,
            risk_reasons=risk_reasons,
            review_priority=0  # 나중에 계산
        ))

    def _assess_risk(self, path: str, additions: int, deletions: int) -> tuple:
        """파일 리스크 평가"""
        reasons = []
        path_lower = path.lower()

        # 패턴 매칭으로 리스크 평가
        for pattern, reason in self.CRITICAL_PATTERNS:
            if re.search(pattern, path_lower):
                reasons.append(f"🔴 {reason}")
                return "critical", reasons

        for pattern, reason in self.HIGH_RISK_PATTERNS:
            if re.search(pattern, path_lower):
                reasons.append(f"🟠 {reason}")

        for pattern, reason in self.MEDIUM_RISK_PATTERNS:
            if re.search(pattern, path_lower):
                reasons.append(f"🟡 {reason}")

        # 변경량 기반 리스크
        total_changes = additions + deletions
        if total_changes > 500:
            reasons.append(f"🟠 대규모 변경 ({total_changes}줄)")
        elif total_changes > 200:
            reasons.append(f"🟡 중간 규모 변경 ({total_changes}줄)")

        # 삭제 비율이 높으면 주의
        if deletions > 0 and deletions > additions * 2:
            reasons.append("🟡 삭제 위주 변경")

        # 리스크 레벨 결정
        if any("🔴" in r for r in reasons):
            return "critical", reasons
        elif any("🟠" in r for r in reasons):
            return "high", reasons
        elif any("🟡" in r for r in reasons):
            return "medium", reasons
        else:
            return "low", ["일반 변경"]

    def _calculate_priorities(self) -> None:
        """리뷰 우선순위 계산"""
        risk_order = {"critical": 0, "high": 1, "medium": 2, "low": 3}

        # 리스크와 변경량으로 정렬
        sorted_files = sorted(
            self.files,
            key=lambda f: (risk_order.get(f.risk_level, 4), -(f.additions + f.deletions))
        )

        # 우선순위 할당
        for i, file in enumerate(sorted_files):
            if file.risk_level == "critical":
                file.review_priority = 1
            elif file.risk_level == "high":
                file.review_priority = 2
            elif file.risk_level == "medium":
                file.review_priority = 3
            else:
                file.review_priority = 4

        self.files = sorted_files

    def _generate_report(self) -> Dict[str, Any]:
        """분석 리포트 생성"""
        summary = {
            "total_files": len(self.files),
            "critical": sum(1 for f in self.files if f.risk_level == "critical"),
            "high": sum(1 for f in self.files if f.risk_level == "high"),
            "medium": sum(1 for f in self.files if f.risk_level == "medium"),
            "low": sum(1 for f in self.files if f.risk_level == "low"),
            "total_additions": sum(f.additions for f in self.files),
            "total_deletions": sum(f.deletions for f in self.files),
        }

        return {
            "pr_number": self.pr_number,
            "base_branch": self.base_branch,
            "summary": summary,
            "files": [asdict(f) for f in self.files],
            "review_order": [f.path for f in self.files[:10]],  # 상위 10개
        }


def main():
    """CLI 실행"""
    import argparse

    parser = argparse.ArgumentParser(description="PR Analyzer")
    parser.add_argument("--pr", type=int, help="GitHub PR number")
    parser.add_argument("--base", default="main", help="Base branch for comparison")
    parser.add_argument("--output", "-o", default=".claude/pr-analysis.json", help="Output file")

    args = parser.parse_args()

    analyzer = PrAnalyzer(pr_number=args.pr, base_branch=args.base)
    report = analyzer.analyze()

    # 결과 저장
    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)

    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(report, f, indent=2, ensure_ascii=False)

    print(f"\n📄 Report saved to: {output_path}")
    print(f"\n📊 Summary:")
    print(f"  - Total files: {report['summary']['total_files']}")
    print(f"  - Critical: {report['summary']['critical']}")
    print(f"  - High: {report['summary']['high']}")
    print(f"  - Medium: {report['summary']['medium']}")
    print(f"  - Low: {report['summary']['low']}")
    print(f"  - Changes: +{report['summary']['total_additions']} / -{report['summary']['total_deletions']}")

    if report['review_order']:
        print(f"\n🎯 Review Order (Top 10):")
        for i, path in enumerate(report['review_order'], 1):
            print(f"  {i}. {path}")


if __name__ == "__main__":
    main()
