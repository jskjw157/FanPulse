#!/usr/bin/env python3
"""
Documentation Analyzer
코드 문서화 품질을 분석하고 누락된 문서화를 식별합니다.
"""

import ast
import re
from pathlib import Path
from typing import Dict, List, Any, Optional
import json


class DocAnalyzer:
    """문서화 분석기"""

    def __init__(self, target_path: str = "."):
        self.target_path = Path(target_path)
        self.missing_docs = []
        self.summary = {
            "total_items": 0,
            "documented": 0,
            "missing": 0,
            "doc_coverage": 0.0,
            "by_priority": {
                "critical": 0,
                "important": 0,
                "nice_to_have": 0
            }
        }

    def analyze(self) -> Dict[str, Any]:
        """전체 분석 실행"""
        print(f"🔍 Analyzing documentation...\n")

        # 파일 타입별 분석
        self._analyze_python_docs()
        self._analyze_kotlin_docs()
        self._analyze_typescript_docs()

        # 커버리지 계산
        if self.summary["total_items"] > 0:
            self.summary["doc_coverage"] = round(
                (self.summary["documented"] / self.summary["total_items"]) * 100,
                2
            )

        return self._generate_report()

    def _analyze_python_docs(self) -> None:
        """Python 문서화 분석"""
        py_files = list(self.target_path.glob("**/*.py"))
        py_files = [f for f in py_files if "venv" not in str(f) and ".venv" not in str(f)]

        if not py_files:
            return

        print(f"📝 Analyzing Python documentation ({len(py_files)} files)...")

        for py_file in py_files:
            self._analyze_python_file(py_file)

        print("✅ Python documentation analysis complete\n")

    def _analyze_python_file(self, file_path: Path) -> None:
        """개별 Python 파일 분석"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()

            tree = ast.parse(content)

            for node in ast.walk(tree):
                # 함수 분석
                if isinstance(node, ast.FunctionDef):
                    self._check_python_function(node, file_path)

                # 클래스 분석
                elif isinstance(node, ast.ClassDef):
                    self._check_python_class(node, file_path)

        except SyntaxError:
            pass  # 문법 오류는 무시
        except Exception:
            pass

    def _check_python_function(self, node: ast.FunctionDef, file_path: Path) -> None:
        """Python 함수 문서화 확인"""
        # private 함수는 낮은 우선순위
        is_private = node.name.startswith("_")
        is_public = not is_private

        self.summary["total_items"] += 1

        # docstring 확인
        has_docstring = ast.get_docstring(node) is not None

        if has_docstring:
            self.summary["documented"] += 1
        else:
            self.summary["missing"] += 1

            # 우선순위 결정
            if is_public and len(node.args.args) > 0:
                priority = "critical"
            elif is_public:
                priority = "important"
            else:
                priority = "nice_to_have"

            self.summary["by_priority"][priority] += 1

            self.missing_docs.append({
                "file": str(file_path),
                "line": node.lineno,
                "type": "function",
                "name": node.name,
                "priority": priority,
                "language": "python",
                "reason": "Missing docstring for public function" if is_public else "Missing docstring"
            })

    def _check_python_class(self, node: ast.ClassDef, file_path: Path) -> None:
        """Python 클래스 문서화 확인"""
        is_private = node.name.startswith("_")
        is_public = not is_private

        self.summary["total_items"] += 1

        has_docstring = ast.get_docstring(node) is not None

        if has_docstring:
            self.summary["documented"] += 1
        else:
            self.summary["missing"] += 1

            priority = "critical" if is_public else "nice_to_have"
            self.summary["by_priority"][priority] += 1

            self.missing_docs.append({
                "file": str(file_path),
                "line": node.lineno,
                "type": "class",
                "name": node.name,
                "priority": priority,
                "language": "python",
                "reason": "Missing docstring for public class" if is_public else "Missing docstring"
            })

    def _analyze_kotlin_docs(self) -> None:
        """Kotlin 문서화 분석"""
        kt_files = list(self.target_path.glob("**/*.kt"))

        if not kt_files:
            return

        print(f"📝 Analyzing Kotlin documentation ({len(kt_files)} files)...")

        for kt_file in kt_files:
            self._analyze_kotlin_file(kt_file)

        print("✅ Kotlin documentation analysis complete\n")

    def _analyze_kotlin_file(self, file_path: Path) -> None:
        """개별 Kotlin 파일 분석"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()

            lines = content.splitlines()

            for i, line in enumerate(lines, 1):
                # 함수 선언 찾기
                if re.match(r'^\s*(public\s+|private\s+)?fun\s+\w+', line):
                    self._check_kotlin_function(lines, i, file_path)

                # 클래스 선언 찾기
                elif re.match(r'^\s*(public\s+|private\s+)?(class|interface|object)\s+\w+', line):
                    self._check_kotlin_class(lines, i, file_path)

        except Exception:
            pass

    def _check_kotlin_function(self, lines: List[str], line_num: int, file_path: Path) -> None:
        """Kotlin 함수 문서화 확인"""
        line = lines[line_num - 1]

        # private 여부 확인
        is_private = "private" in line

        self.summary["total_items"] += 1

        # 이전 라인에 KDoc이 있는지 확인
        has_kdoc = False
        if line_num > 1:
            prev_line = lines[line_num - 2].strip()
            has_kdoc = prev_line.endswith("*/") or "/**" in prev_line

        if has_kdoc:
            self.summary["documented"] += 1
        else:
            self.summary["missing"] += 1

            priority = "important" if not is_private else "nice_to_have"
            self.summary["by_priority"][priority] += 1

            # 함수명 추출
            match = re.search(r'fun\s+(\w+)', line)
            func_name = match.group(1) if match else "unknown"

            self.missing_docs.append({
                "file": str(file_path),
                "line": line_num,
                "type": "function",
                "name": func_name,
                "priority": priority,
                "language": "kotlin",
                "reason": "Missing KDoc for function"
            })

    def _check_kotlin_class(self, lines: List[str], line_num: int, file_path: Path) -> None:
        """Kotlin 클래스 문서화 확인"""
        line = lines[line_num - 1]

        is_private = "private" in line

        self.summary["total_items"] += 1

        # 이전 라인에 KDoc이 있는지 확인
        has_kdoc = False
        if line_num > 1:
            prev_line = lines[line_num - 2].strip()
            has_kdoc = prev_line.endswith("*/") or "/**" in prev_line

        if has_kdoc:
            self.summary["documented"] += 1
        else:
            self.summary["missing"] += 1

            priority = "critical" if not is_private else "nice_to_have"
            self.summary["by_priority"][priority] += 1

            # 클래스명 추출
            match = re.search(r'(class|interface|object)\s+(\w+)', line)
            class_name = match.group(2) if match else "unknown"

            self.missing_docs.append({
                "file": str(file_path),
                "line": line_num,
                "type": "class",
                "name": class_name,
                "priority": priority,
                "language": "kotlin",
                "reason": "Missing KDoc for class/interface"
            })

    def _analyze_typescript_docs(self) -> None:
        """TypeScript/JavaScript 문서화 분석"""
        ts_files = list(self.target_path.glob("**/*.ts")) + \
                   list(self.target_path.glob("**/*.tsx")) + \
                   list(self.target_path.glob("**/*.js")) + \
                   list(self.target_path.glob("**/*.jsx"))

        ts_files = [f for f in ts_files if "node_modules" not in str(f)]

        if not ts_files:
            return

        print(f"📝 Analyzing TypeScript/JavaScript documentation ({len(ts_files)} files)...")

        for ts_file in ts_files:
            self._analyze_typescript_file(ts_file)

        print("✅ TypeScript/JavaScript documentation analysis complete\n")

    def _analyze_typescript_file(self, file_path: Path) -> None:
        """개별 TypeScript 파일 분석"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()

            lines = content.splitlines()

            for i, line in enumerate(lines, 1):
                # 함수/메서드 선언 찾기
                if re.match(r'^\s*(export\s+)?(async\s+)?function\s+\w+', line):
                    self._check_typescript_function(lines, i, file_path)

                # 클래스 선언 찾기
                elif re.match(r'^\s*(export\s+)?(abstract\s+)?class\s+\w+', line):
                    self._check_typescript_class(lines, i, file_path)

        except Exception:
            pass

    def _check_typescript_function(self, lines: List[str], line_num: int, file_path: Path) -> None:
        """TypeScript 함수 문서화 확인"""
        line = lines[line_num - 1]

        self.summary["total_items"] += 1

        # 이전 라인에 JSDoc이 있는지 확인
        has_jsdoc = False
        if line_num > 1:
            prev_line = lines[line_num - 2].strip()
            has_jsdoc = prev_line.endswith("*/") or "/**" in prev_line

        if has_jsdoc:
            self.summary["documented"] += 1
        else:
            self.summary["missing"] += 1

            is_exported = "export" in line
            priority = "important" if is_exported else "nice_to_have"
            self.summary["by_priority"][priority] += 1

            # 함수명 추출
            match = re.search(r'function\s+(\w+)', line)
            func_name = match.group(1) if match else "unknown"

            self.missing_docs.append({
                "file": str(file_path),
                "line": line_num,
                "type": "function",
                "name": func_name,
                "priority": priority,
                "language": "typescript",
                "reason": "Missing JSDoc for exported function" if is_exported else "Missing JSDoc"
            })

    def _check_typescript_class(self, lines: List[str], line_num: int, file_path: Path) -> None:
        """TypeScript 클래스 문서화 확인"""
        line = lines[line_num - 1]

        self.summary["total_items"] += 1

        # 이전 라인에 JSDoc이 있는지 확인
        has_jsdoc = False
        if line_num > 1:
            prev_line = lines[line_num - 2].strip()
            has_jsdoc = prev_line.endswith("*/") or "/**" in prev_line

        if has_jsdoc:
            self.summary["documented"] += 1
        else:
            self.summary["missing"] += 1

            is_exported = "export" in line
            priority = "critical" if is_exported else "nice_to_have"
            self.summary["by_priority"][priority] += 1

            # 클래스명 추출
            match = re.search(r'class\s+(\w+)', line)
            class_name = match.group(1) if match else "unknown"

            self.missing_docs.append({
                "file": str(file_path),
                "line": line_num,
                "type": "class",
                "name": class_name,
                "priority": priority,
                "language": "typescript",
                "reason": "Missing JSDoc for exported class" if is_exported else "Missing JSDoc"
            })

    def _generate_report(self) -> Dict[str, Any]:
        """최종 리포트 생성"""
        # 우선순위별 정렬
        critical = [d for d in self.missing_docs if d["priority"] == "critical"]
        important = [d for d in self.missing_docs if d["priority"] == "important"]
        nice_to_have = [d for d in self.missing_docs if d["priority"] == "nice_to_have"]

        return {
            "summary": self.summary,
            "missing_docs": {
                "critical": critical[:20],  # 상위 20개만
                "important": important[:30],  # 상위 30개만
                "nice_to_have": nice_to_have[:10]  # 상위 10개만
            },
            "recommendations": self._generate_recommendations()
        }

    def _generate_recommendations(self) -> List[str]:
        """개선 권장사항 생성"""
        recommendations = []

        if self.summary["doc_coverage"] < 50:
            recommendations.append(
                f"Documentation coverage is low ({self.summary['doc_coverage']}%). Consider adding docs for public APIs first."
            )

        if self.summary["by_priority"]["critical"] > 0:
            recommendations.append(
                f"Focus on {self.summary['by_priority']['critical']} critical items (public classes/exported functions)"
            )

        if self.summary["doc_coverage"] >= 80:
            recommendations.append(
                f"Good documentation coverage ({self.summary['doc_coverage']}%)! Consider reviewing doc quality."
            )

        return recommendations


def main():
    """CLI 실행"""
    import argparse

    parser = argparse.ArgumentParser(description="Documentation Analyzer")
    parser.add_argument("--target", default=".", help="Target directory")
    parser.add_argument("--output", default=".claude/doc-report.json", help="Output file path")

    args = parser.parse_args()

    # 분석 실행
    analyzer = DocAnalyzer(target_path=args.target)
    report = analyzer.analyze()

    # 결과 저장
    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)

    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(report, f, indent=2, ensure_ascii=False)

    print(f"✅ Report saved to: {output_path}")
    print(f"\n📊 Summary:")
    print(f"  - Total items: {report['summary']['total_items']}")
    print(f"  - Documented: {report['summary']['documented']}")
    print(f"  - Missing: {report['summary']['missing']}")
    print(f"  - Coverage: {report['summary']['doc_coverage']}%")
    print(f"\n  Missing by priority:")
    print(f"    - Critical: {report['summary']['by_priority']['critical']}")
    print(f"    - Important: {report['summary']['by_priority']['important']}")
    print(f"    - Nice to have: {report['summary']['by_priority']['nice_to_have']}")


if __name__ == "__main__":
    main()
