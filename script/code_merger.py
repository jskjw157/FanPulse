#!/usr/bin/env python3
"""
Code Merger
대규모 코드베이스를 하나의 파일로 병합하여 AI 분석을 용이하게 합니다.
Repomix 스타일의 병합 기능 제공.
"""

import os
from pathlib import Path
from typing import List, Set, Optional
import fnmatch
import json


class CodeMerger:
    """코드베이스 병합기"""

    def __init__(
        self,
        project_root: str = ".",
        output_file: str = "merged_code.txt"
    ):
        self.project_root = Path(project_root)
        self.output_file = Path(output_file)

        # 기본 제외 패턴
        self.default_exclude = {
            # 디렉토리
            "node_modules", ".git", ".venv", "venv", "__pycache__",
            ".pytest_cache", ".mypy_cache", "dist", "build",
            ".next", ".nuxt", "out", "target",
            # 파일 패턴
            "*.pyc", "*.pyo", "*.so", "*.dylib", "*.dll",
            "*.class", "*.jar", "*.war",
            "*.min.js", "*.bundle.js",
            "package-lock.json", "yarn.lock", "poetry.lock",
            ".DS_Store", "Thumbs.db"
        }

        self.stats = {
            "total_files": 0,
            "total_lines": 0,
            "total_chars": 0,
            "by_extension": {}
        }

    def merge(
        self,
        include_patterns: Optional[List[str]] = None,
        exclude_patterns: Optional[List[str]] = None,
        include_extensions: Optional[List[str]] = None,
        max_file_size_kb: int = 500
    ) -> str:
        """
        코드베이스 병합

        Args:
            include_patterns: 포함할 파일 패턴 리스트 (예: ["src/**/*.py"])
            exclude_patterns: 제외할 패턴 (default_exclude에 추가됨)
            include_extensions: 포함할 확장자만 (예: [".py", ".js"])
            max_file_size_kb: 최대 파일 크기 (KB)

        Returns:
            생성된 파일 경로
        """
        print(f"🔍 Merging codebase from: {self.project_root}\n")

        # 제외 패턴 병합
        exclude = self.default_exclude.copy()
        if exclude_patterns:
            exclude.update(exclude_patterns)

        # 파일 수집
        files = self._collect_files(
            include_patterns,
            exclude,
            include_extensions,
            max_file_size_kb
        )

        if not files:
            print("⚠️  No files found matching criteria")
            return ""

        print(f"📝 Found {len(files)} files to merge\n")

        # 병합 실행
        self._merge_files(files)

        # 통계 출력
        self._print_stats()

        return str(self.output_file)

    def _collect_files(
        self,
        include_patterns: Optional[List[str]],
        exclude: Set[str],
        include_extensions: Optional[List[str]],
        max_file_size_kb: int
    ) -> List[Path]:
        """파일 수집"""
        files = []

        if include_patterns:
            # 패턴 기반 수집
            for pattern in include_patterns:
                matched = list(self.project_root.glob(pattern))
                files.extend([f for f in matched if f.is_file()])
        else:
            # 전체 프로젝트 순회
            for root, dirs, filenames in os.walk(self.project_root):
                # 제외 디렉토리 필터링
                dirs[:] = [d for d in dirs if d not in exclude]

                for filename in filenames:
                    filepath = Path(root) / filename
                    files.append(filepath)

        # 필터링
        filtered = []
        for filepath in files:
            # 제외 패턴 체크
            if self._should_exclude(filepath, exclude):
                continue

            # 확장자 체크
            if include_extensions and filepath.suffix not in include_extensions:
                continue

            # 파일 크기 체크
            try:
                size_kb = filepath.stat().st_size / 1024
                if size_kb > max_file_size_kb:
                    print(f"⚠️  Skipping large file ({size_kb:.1f}KB): {filepath}")
                    continue
            except Exception:
                continue

            # 텍스트 파일인지 확인
            if self._is_text_file(filepath):
                filtered.append(filepath)

        return sorted(set(filtered))

    def _should_exclude(self, filepath: Path, exclude: Set[str]) -> bool:
        """파일 제외 여부 확인"""
        # 파일명 체크
        if filepath.name in exclude:
            return True

        # 패턴 체크
        for pattern in exclude:
            if fnmatch.fnmatch(filepath.name, pattern):
                return True
            # 경로 체크
            if any(fnmatch.fnmatch(part, pattern) for part in filepath.parts):
                return True

        return False

    def _is_text_file(self, filepath: Path) -> bool:
        """텍스트 파일인지 확인"""
        text_extensions = {
            # 프로그래밍 언어
            ".py", ".js", ".ts", ".jsx", ".tsx", ".java", ".kt", ".kts",
            ".c", ".cpp", ".h", ".hpp", ".cs", ".go", ".rs", ".rb",
            ".php", ".swift", ".m", ".scala", ".clj", ".ex", ".exs",
            # 웹
            ".html", ".css", ".scss", ".sass", ".less", ".vue",
            # 설정
            ".json", ".yaml", ".yml", ".toml", ".ini", ".cfg", ".conf",
            ".xml", ".gradle", ".properties",
            # 문서
            ".md", ".txt", ".rst", ".adoc",
            # 쉘/스크립트
            ".sh", ".bash", ".zsh", ".fish", ".bat", ".ps1",
            # 기타
            ".sql", ".graphql", ".proto", ".Dockerfile"
        }

        if filepath.suffix.lower() in text_extensions:
            return True

        # 확장자 없는 파일 (Makefile, Dockerfile 등)
        if not filepath.suffix:
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    f.read(1024)  # 처음 1KB만 읽어서 텍스트인지 확인
                return True
            except (UnicodeDecodeError, Exception):
                return False

        return False

    def _merge_files(self, files: List[Path]) -> None:
        """파일 병합"""
        with open(self.output_file, 'w', encoding='utf-8') as out:
            # 헤더
            out.write("# Merged Codebase\n\n")
            out.write(f"Project: {self.project_root.absolute()}\n")
            out.write(f"Total files: {len(files)}\n")
            out.write(f"Generated by: Code Merger\n\n")
            out.write("=" * 80 + "\n\n")

            # 파일별 병합
            for i, filepath in enumerate(files, 1):
                try:
                    relative_path = filepath.relative_to(self.project_root)
                except ValueError:
                    relative_path = filepath

                print(f"[{i}/{len(files)}] {relative_path}")

                # 파일 헤더
                out.write(f"\n{'=' * 80}\n")
                out.write(f"File: {relative_path}\n")
                out.write(f"{'=' * 80}\n\n")

                # 파일 내용
                try:
                    with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
                        content = f.read()

                    out.write(content)
                    out.write("\n\n")

                    # 통계 업데이트
                    self.stats["total_files"] += 1
                    lines = content.count('\n')
                    self.stats["total_lines"] += lines
                    self.stats["total_chars"] += len(content)

                    ext = filepath.suffix or "no_ext"
                    self.stats["by_extension"][ext] = self.stats["by_extension"].get(ext, 0) + 1

                except Exception as e:
                    out.write(f"# Error reading file: {e}\n\n")
                    print(f"  ⚠️  Error: {e}")

    def _print_stats(self) -> None:
        """통계 출력"""
        print(f"\n{'=' * 80}")
        print("📊 Merge Statistics")
        print(f"{'=' * 80}")
        print(f"  Total files:      {self.stats['total_files']:,}")
        print(f"  Total lines:      {self.stats['total_lines']:,}")
        print(f"  Total characters: {self.stats['total_chars']:,}")
        print(f"  Output file:      {self.output_file}")
        print(f"  Output size:      {self.output_file.stat().st_size / 1024:.1f} KB")

        if self.stats["by_extension"]:
            print("\n  Files by extension:")
            for ext, count in sorted(
                self.stats["by_extension"].items(),
                key=lambda x: x[1],
                reverse=True
            )[:10]:
                print(f"    {ext:15} {count:4} files")

        # JSON 통계 저장
        stats_file = self.output_file.parent / f"{self.output_file.stem}_stats.json"
        with open(stats_file, 'w', encoding='utf-8') as f:
            json.dump(self.stats, f, indent=2)
        print(f"\n  Stats saved to: {stats_file}")


def main():
    """CLI 실행"""
    import argparse

    parser = argparse.ArgumentParser(description="Code Merger - Merge codebase into single file")
    parser.add_argument("--project-root", default=".", help="Project root directory")
    parser.add_argument("--output", default="merged_code.txt", help="Output file path")
    parser.add_argument("--include", nargs="+", help="Include patterns (e.g., 'src/**/*.py')")
    parser.add_argument("--exclude", nargs="+", help="Exclude patterns")
    parser.add_argument("--ext", nargs="+", help="Include only these extensions (e.g., .py .js)")
    parser.add_argument("--max-size", type=int, default=500, help="Max file size in KB (default: 500)")

    args = parser.parse_args()

    # 병합
    merger = CodeMerger(
        project_root=args.project_root,
        output_file=args.output
    )

    output_path = merger.merge(
        include_patterns=args.include,
        exclude_patterns=args.exclude,
        include_extensions=args.ext,
        max_file_size_kb=args.max_size
    )

    if output_path:
        print(f"\n✅ Merge complete! Check: {output_path}")
    else:
        print("\n❌ Merge failed")


if __name__ == "__main__":
    main()
