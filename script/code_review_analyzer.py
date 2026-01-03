#!/usr/bin/env python3
"""
Code Review Analyzer
정적 분석 도구(ktlint, eslint, flake8 등)를 통합하여 JSON 리포트 생성
"""

import json
import subprocess
import os
from pathlib import Path
from typing import Dict, List, Any, Optional
import re


class CodeReviewAnalyzer:
    """코드 리뷰 정적 분석기"""

    def __init__(self, project_root: str = "."):
        self.project_root = Path(project_root)
        self.issues = []
        self.summary = {
            "error": 0,
            "warning": 0,
            "info": 0,
            "total": 0,
            "auto_fixable": 0
        }

    def analyze(self) -> Dict[str, Any]:
        """전체 분석 실행"""
        print("🔍 Starting code review analysis...\n")

        # 각 언어별 분석 실행
        self._analyze_kotlin()
        self._analyze_typescript()
        self._analyze_python()

        # 결과 집계
        self._aggregate_results()

        return self._generate_report()

    def _analyze_kotlin(self) -> None:
        """Kotlin 코드 분석 (ktlint)"""
        kotlin_files = list(self.project_root.glob("**/*.kt"))
        if not kotlin_files:
            return

        print(f"📝 Analyzing Kotlin files ({len(kotlin_files)} files)...")

        # ktlint 실행
        try:
            result = subprocess.run(
                ["ktlint", "--reporter=json"],
                cwd=self.project_root,
                capture_output=True,
                text=True,
                timeout=60
            )

            if result.stdout:
                self._parse_ktlint_output(result.stdout)
            print("✅ Kotlin analysis complete")

        except FileNotFoundError:
            print("⚠️  ktlint not found, skipping Kotlin analysis")
        except subprocess.TimeoutExpired:
            print("⚠️  ktlint timeout, skipping Kotlin analysis")
        except Exception as e:
            print(f"⚠️  Kotlin analysis error: {e}")

    def _parse_ktlint_output(self, output: str) -> None:
        """ktlint JSON 출력 파싱"""
        try:
            data = json.loads(output)
            for item in data:
                file_path = item.get("file", "")
                errors = item.get("errors", [])

                for error in errors:
                    self.issues.append({
                        "file": file_path,
                        "line": error.get("line", 0),
                        "column": error.get("column", 0),
                        "severity": "error",
                        "message": error.get("message", ""),
                        "rule": error.get("rule", ""),
                        "tool": "ktlint",
                        "fixable": False
                    })
                    self.summary["error"] += 1

        except json.JSONDecodeError:
            print("⚠️  Failed to parse ktlint JSON output")

    def _analyze_typescript(self) -> None:
        """TypeScript/JavaScript 코드 분석 (eslint)"""
        ts_files = list(self.project_root.glob("**/*.ts")) + \
                   list(self.project_root.glob("**/*.tsx")) + \
                   list(self.project_root.glob("**/*.js")) + \
                   list(self.project_root.glob("**/*.jsx"))

        # node_modules 제외
        ts_files = [f for f in ts_files if "node_modules" not in str(f)]

        if not ts_files:
            return

        print(f"📝 Analyzing TypeScript/JavaScript files ({len(ts_files)} files)...")

        # eslint 실행
        try:
            result = subprocess.run(
                ["npx", "eslint", ".", "--format=json"],
                cwd=self.project_root,
                capture_output=True,
                text=True,
                timeout=60
            )

            if result.stdout:
                self._parse_eslint_output(result.stdout)
            print("✅ TypeScript/JavaScript analysis complete")

        except FileNotFoundError:
            print("⚠️  eslint not found, skipping TypeScript/JavaScript analysis")
        except subprocess.TimeoutExpired:
            print("⚠️  eslint timeout")
        except Exception as e:
            print(f"⚠️  TypeScript/JavaScript analysis error: {e}")

    def _parse_eslint_output(self, output: str) -> None:
        """eslint JSON 출력 파싱"""
        try:
            data = json.loads(output)
            for file_result in data:
                file_path = file_result.get("filePath", "")
                messages = file_result.get("messages", [])

                for msg in messages:
                    severity_map = {1: "warning", 2: "error"}
                    severity = severity_map.get(msg.get("severity", 1), "info")

                    self.issues.append({
                        "file": file_path,
                        "line": msg.get("line", 0),
                        "column": msg.get("column", 0),
                        "severity": severity,
                        "message": msg.get("message", ""),
                        "rule": msg.get("ruleId", ""),
                        "tool": "eslint",
                        "fixable": msg.get("fix") is not None
                    })

                    self.summary[severity] += 1
                    if msg.get("fix"):
                        self.summary["auto_fixable"] += 1

        except json.JSONDecodeError:
            print("⚠️  Failed to parse eslint JSON output")

    def _analyze_python(self) -> None:
        """Python 코드 분석 (flake8)"""
        py_files = list(self.project_root.glob("**/*.py"))

        # 가상환경 제외
        py_files = [f for f in py_files if "venv" not in str(f) and ".venv" not in str(f)]

        if not py_files:
            return

        print(f"📝 Analyzing Python files ({len(py_files)} files)...")

        # flake8 실행
        try:
            result = subprocess.run(
                ["flake8", "--format=json", "."],
                cwd=self.project_root,
                capture_output=True,
                text=True,
                timeout=60
            )

            if result.stdout:
                self._parse_flake8_output(result.stdout)
            print("✅ Python analysis complete")

        except FileNotFoundError:
            # flake8이 없으면 기본 문법 체크만
            print("⚠️  flake8 not found, trying basic Python syntax check...")
            self._basic_python_check(py_files)

        except subprocess.TimeoutExpired:
            print("⚠️  flake8 timeout")
        except Exception as e:
            print(f"⚠️  Python analysis error: {e}")

    def _parse_flake8_output(self, output: str) -> None:
        """flake8 JSON 출력 파싱"""
        try:
            # flake8의 JSON 형식이 다양하므로 라인별 파싱
            for line in output.splitlines():
                if not line.strip():
                    continue

                # 기본 flake8 형식: file.py:line:col: CODE message
                match = re.match(r"(.+?):(\d+):(\d+):\s*(\w+)\s+(.+)", line)
                if match:
                    file_path, line_num, col, code, message = match.groups()

                    # 심각도 판단
                    severity = "error" if code.startswith("E") else "warning"

                    self.issues.append({
                        "file": file_path,
                        "line": int(line_num),
                        "column": int(col),
                        "severity": severity,
                        "message": message.strip(),
                        "rule": code,
                        "tool": "flake8",
                        "fixable": False
                    })

                    self.summary[severity] += 1

        except Exception as e:
            print(f"⚠️  Failed to parse flake8 output: {e}")

    def _basic_python_check(self, py_files: List[Path]) -> None:
        """기본 Python 문법 체크"""
        import py_compile

        for py_file in py_files[:20]:  # 최대 20개만
            try:
                py_compile.compile(str(py_file), doraise=True)
            except py_compile.PyCompileError as e:
                self.issues.append({
                    "file": str(py_file),
                    "line": e.exc_value.lineno if hasattr(e.exc_value, 'lineno') else 0,
                    "column": 0,
                    "severity": "error",
                    "message": str(e.exc_value),
                    "rule": "SyntaxError",
                    "tool": "py_compile",
                    "fixable": False
                })
                self.summary["error"] += 1

    def _aggregate_results(self) -> None:
        """결과 집계"""
        self.summary["total"] = sum([
            self.summary["error"],
            self.summary["warning"],
            self.summary["info"]
        ])

        # 파일별 이슈 수 집계
        file_issues = {}
        for issue in self.issues:
            file = issue["file"]
            file_issues[file] = file_issues.get(file, 0) + 1

        self.summary["files_with_issues"] = len(file_issues)
        self.summary["top_files"] = sorted(
            file_issues.items(),
            key=lambda x: x[1],
            reverse=True
        )[:5]

    def _generate_report(self) -> Dict[str, Any]:
        """최종 리포트 생성"""
        return {
            "summary": self.summary,
            "issues": self.issues[:100],  # 상위 100개만
            "recommendations": self._generate_recommendations()
        }

    def _generate_recommendations(self) -> List[str]:
        """개선 권장사항 생성"""
        recommendations = []

        if self.summary["auto_fixable"] > 0:
            recommendations.append(
                f"Run auto-fix to resolve {self.summary['auto_fixable']} issues automatically"
            )

        if self.summary["error"] > 10:
            recommendations.append(
                "High number of errors detected. Consider running linters in CI/CD"
            )

        if self.summary["files_with_issues"] > 20:
            recommendations.append(
                "Many files have issues. Consider incremental refactoring"
            )

        return recommendations


def main():
    """CLI 실행"""
    import argparse

    parser = argparse.ArgumentParser(description="Code Review Analyzer")
    parser.add_argument("--project-root", default=".", help="Project root directory")
    parser.add_argument("--output", default=".claude/review-report.json", help="Output file path")

    args = parser.parse_args()

    # 분석 실행
    analyzer = CodeReviewAnalyzer(project_root=args.project_root)
    report = analyzer.analyze()

    # 결과 저장
    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)

    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(report, f, indent=2, ensure_ascii=False)

    print(f"\n✅ Report saved to: {output_path}")
    print(f"\n📊 Summary:")
    print(f"  - Total issues: {report['summary']['total']}")
    print(f"  - Errors: {report['summary']['error']}")
    print(f"  - Warnings: {report['summary']['warning']}")
    print(f"  - Auto-fixable: {report['summary']['auto_fixable']}")
    print(f"  - Files with issues: {report['summary']['files_with_issues']}")


if __name__ == "__main__":
    main()
