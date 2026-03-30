#!/usr/bin/env python3
"""
Claude Code Configuration Validator
.claude/ 디렉토리의 설정 파일(agents, skills, hooks)을 검증합니다.
"""

import json
import yaml
import re
import os
from pathlib import Path
from typing import Dict, List, Any, Optional, Tuple


class ConfigValidator:
    """Claude Code 설정 검증기"""

    def __init__(self, claude_dir: str = ".claude"):
        self.claude_dir = Path(claude_dir)
        self.issues = []
        self.summary = {
            "agents": {"total": 0, "valid": 0, "invalid": 0},
            "skills": {"total": 0, "valid": 0, "invalid": 0},
            "hooks": {"total": 0, "valid": 0, "invalid": 0},
            "rules": {"total": 0, "valid": 0, "invalid": 0}
        }

    def _detect_target_type(self) -> Optional[str]:
        """타겟 경로의 유형 감지

        Returns:
            - "skill_dir": SKILL.md가 있는 스킬 디렉토리
            - "agent_file": .md 에이전트 파일
            - "claude_root": .claude/ 루트 디렉토리
            - None: 알 수 없는 유형
        """
        target = self.claude_dir

        # SKILL.md가 있으면 스킬 디렉토리
        if (target / "SKILL.md").exists():
            return "skill_dir"

        # .md 파일이면 에이전트 파일일 수 있음
        if target.is_file() and target.suffix == ".md":
            return "agent_file"

        # agents/, skills/, hooks/, rules/ 중 하나라도 있으면 claude root
        subdirs = ["agents", "skills", "hooks", "rules"]
        if any((target / subdir).exists() for subdir in subdirs):
            return "claude_root"

        # 부모 디렉토리 확인하여 유형 추론
        if target.is_dir():
            parent_name = target.parent.name
            if parent_name == "skills":
                return "skill_dir"
            elif parent_name == "agents":
                return "agent_file"

        return None

    def validate(self) -> Dict[str, Any]:
        """전체 검증 실행"""
        print(f"🔍 Validating .claude/ configuration...\n")

        if not self.claude_dir.exists():
            self.issues.append({
                "type": "error",
                "category": "structure",
                "message": f".claude/ directory not found at {self.claude_dir}"
            })
            return self._generate_report()

        # 타겟 유형 감지
        target_type = self._detect_target_type()

        if target_type == "skill_dir":
            # 특정 스킬 디렉토리만 검증
            print(f"📁 Detected skill directory: {self.claude_dir}")
            self.summary["skills"]["total"] = 1
            is_valid = self._validate_skill_dir(self.claude_dir)
            if is_valid:
                self.summary["skills"]["valid"] += 1
            else:
                self.summary["skills"]["invalid"] += 1
            print(f"✅ Skill validation complete\n")
        elif target_type == "agent_file":
            # 특정 에이전트 파일만 검증
            print(f"📄 Detected agent file: {self.claude_dir}")
            self.summary["agents"]["total"] = 1
            is_valid = self._validate_agent_file(self.claude_dir)
            if is_valid:
                self.summary["agents"]["valid"] += 1
            else:
                self.summary["agents"]["invalid"] += 1
            print(f"✅ Agent validation complete\n")
        else:
            # 전체 .claude/ 디렉토리 검증
            self._validate_agents()
            self._validate_skills()
            self._validate_hooks()
            self._validate_rules()

        return self._generate_report()

    def _validate_agents(self) -> None:
        """에이전트 파일 검증"""
        agents_dir = self.claude_dir / "agents"
        if not agents_dir.exists():
            return

        agent_files = list(agents_dir.glob("*.md"))
        self.summary["agents"]["total"] = len(agent_files)

        print(f"📝 Validating {len(agent_files)} agent(s)...")

        for agent_file in agent_files:
            is_valid = self._validate_agent_file(agent_file)
            if is_valid:
                self.summary["agents"]["valid"] += 1
            else:
                self.summary["agents"]["invalid"] += 1

        print(f"✅ Agent validation complete\n")

    def _validate_agent_file(self, file_path: Path) -> bool:
        """개별 에이전트 파일 검증"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()

            # Frontmatter 추출
            frontmatter, body = self._extract_frontmatter(content)
            if not frontmatter:
                self.issues.append({
                    "type": "error",
                    "category": "agent",
                    "file": str(file_path.relative_to(self.claude_dir)),
                    "message": "Missing or invalid frontmatter"
                })
                return False

            # 필수 필드 검증
            required_fields = ["name", "description", "tools"]
            for field in required_fields:
                if field not in frontmatter:
                    self.issues.append({
                        "type": "error",
                        "category": "agent",
                        "file": str(file_path.relative_to(self.claude_dir)),
                        "message": f"Missing required field: {field}"
                    })
                    return False

            # name 형식 검증 (kebab-case)
            name = frontmatter.get("name", "")
            if not re.match(r'^[a-z][a-z0-9-]*$', name):
                self.issues.append({
                    "type": "warning",
                    "category": "agent",
                    "file": str(file_path.relative_to(self.claude_dir)),
                    "message": f"Agent name should be kebab-case: {name}"
                })

            # tools 검증
            tools = frontmatter.get("tools", "")
            valid_tools = ["Read", "Write", "Edit", "Grep", "Glob", "Bash", "WebFetch", "WebSearch"]
            if isinstance(tools, str):
                tool_list = [t.strip() for t in tools.split(",")]
            elif isinstance(tools, list):
                tool_list = tools
            else:
                self.issues.append({
                    "type": "error",
                    "category": "agent",
                    "file": str(file_path.relative_to(self.claude_dir)),
                    "message": "tools field must be string or list"
                })
                return False

            for tool in tool_list:
                if tool not in valid_tools:
                    self.issues.append({
                        "type": "warning",
                        "category": "agent",
                        "file": str(file_path.relative_to(self.claude_dir)),
                        "message": f"Unknown tool: {tool}"
                    })

            # model 검증 (optional)
            if "model" in frontmatter:
                model = frontmatter["model"]
                if model not in ["sonnet", "opus", "haiku"]:
                    self.issues.append({
                        "type": "warning",
                        "category": "agent",
                        "file": str(file_path.relative_to(self.claude_dir)),
                        "message": f"Unknown model: {model}"
                    })

            # Body 검증
            if not body.strip():
                self.issues.append({
                    "type": "warning",
                    "category": "agent",
                    "file": str(file_path.relative_to(self.claude_dir)),
                    "message": "Empty agent body"
                })

            return True

        except Exception as e:
            self.issues.append({
                "type": "error",
                "category": "agent",
                "file": str(file_path.relative_to(self.claude_dir)),
                "message": f"Validation error: {str(e)}"
            })
            return False

    def _validate_skills(self) -> None:
        """스킬 디렉토리 검증"""
        skills_dir = self.claude_dir / "skills"
        if not skills_dir.exists():
            return

        skill_dirs = [d for d in skills_dir.iterdir() if d.is_dir()]
        self.summary["skills"]["total"] = len(skill_dirs)

        print(f"📝 Validating {len(skill_dirs)} skill(s)...")

        for skill_dir in skill_dirs:
            is_valid = self._validate_skill_dir(skill_dir)
            if is_valid:
                self.summary["skills"]["valid"] += 1
            else:
                self.summary["skills"]["invalid"] += 1

        print(f"✅ Skill validation complete\n")

    def _get_relative_path(self, path: Path) -> str:
        """경로를 상대 경로 문자열로 변환 (실패 시 절대 경로 반환)"""
        try:
            return str(path.relative_to(self.claude_dir))
        except ValueError:
            # relative_to 실패 시 파일명만 반환
            return str(path.name) if path.is_file() else str(path)

    def _validate_skill_dir(self, skill_dir: Path) -> bool:
        """개별 스킬 디렉토리 검증"""
        skill_file = skill_dir / "SKILL.md"

        if not skill_file.exists():
            self.issues.append({
                "type": "error",
                "category": "skill",
                "file": self._get_relative_path(skill_dir),
                "message": "Missing SKILL.md file"
            })
            return False

        try:
            with open(skill_file, 'r', encoding='utf-8') as f:
                content = f.read()

            # Frontmatter 검증
            frontmatter, body = self._extract_frontmatter(content)
            if not frontmatter:
                self.issues.append({
                    "type": "error",
                    "category": "skill",
                    "file": self._get_relative_path(skill_file),
                    "message": "Missing or invalid frontmatter"
                })
                return False

            # 필수 필드 검증
            if "name" not in frontmatter:
                self.issues.append({
                    "type": "warning",
                    "category": "skill",
                    "file": self._get_relative_path(skill_file),
                    "message": "Missing name field"
                })

            if "description" not in frontmatter:
                self.issues.append({
                    "type": "warning",
                    "category": "skill",
                    "file": self._get_relative_path(skill_file),
                    "message": "Missing description field"
                })

            # name과 디렉토리명 일치 확인
            if "name" in frontmatter:
                name = frontmatter["name"]
                dir_name = skill_dir.name
                if name != dir_name:
                    self.issues.append({
                        "type": "warning",
                        "category": "skill",
                        "file": self._get_relative_path(skill_file),
                        "message": f"Skill name '{name}' doesn't match directory name '{dir_name}'"
                    })

            # Body 검증
            if not body.strip():
                self.issues.append({
                    "type": "warning",
                    "category": "skill",
                    "file": self._get_relative_path(skill_file),
                    "message": "Empty skill body"
                })

            return True

        except Exception as e:
            self.issues.append({
                "type": "error",
                "category": "skill",
                "file": self._get_relative_path(skill_file),
                "message": f"Validation error: {str(e)}"
            })
            return False

    def _validate_hooks(self) -> None:
        """훅 파일 검증"""
        hooks_dir = self.claude_dir / "hooks"
        if not hooks_dir.exists():
            return

        hook_files = list(hooks_dir.glob("*.py")) + list(hooks_dir.glob("*.js"))
        self.summary["hooks"]["total"] = len(hook_files)

        print(f"📝 Validating {len(hook_files)} hook(s)...")

        for hook_file in hook_files:
            is_valid = self._validate_hook_file(hook_file)
            if is_valid:
                self.summary["hooks"]["valid"] += 1
            else:
                self.summary["hooks"]["invalid"] += 1

        print(f"✅ Hook validation complete\n")

    def _validate_hook_file(self, file_path: Path) -> bool:
        """개별 훅 파일 검증"""
        try:
            # 파일이 실행 가능한지 확인
            if not os.access(file_path, os.X_OK):
                self.issues.append({
                    "type": "warning",
                    "category": "hook",
                    "file": str(file_path.relative_to(self.claude_dir)),
                    "message": "Hook file is not executable"
                })

            # Python 훅 문법 검증
            if file_path.suffix == ".py":
                import py_compile
                try:
                    py_compile.compile(str(file_path), doraise=True)
                except py_compile.PyCompileError as e:
                    self.issues.append({
                        "type": "error",
                        "category": "hook",
                        "file": str(file_path.relative_to(self.claude_dir)),
                        "message": f"Python syntax error: {str(e)}"
                    })
                    return False

            return True

        except Exception as e:
            self.issues.append({
                "type": "error",
                "category": "hook",
                "file": str(file_path.relative_to(self.claude_dir)),
                "message": f"Validation error: {str(e)}"
            })
            return False

    def _validate_rules(self) -> None:
        """규칙 파일 검증"""
        rules_dir = self.claude_dir / "rules"
        if not rules_dir.exists():
            return

        rule_files = list(rules_dir.glob("*.md"))
        self.summary["rules"]["total"] = len(rule_files)

        print(f"📝 Validating {len(rule_files)} rule(s)...")

        for rule_file in rule_files:
            is_valid = self._validate_rule_file(rule_file)
            if is_valid:
                self.summary["rules"]["valid"] += 1
            else:
                self.summary["rules"]["invalid"] += 1

        print(f"✅ Rule validation complete\n")

    def _validate_rule_file(self, file_path: Path) -> bool:
        """개별 규칙 파일 검증"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()

            # Frontmatter가 있으면 검증
            frontmatter, body = self._extract_frontmatter(content)

            if frontmatter and "paths" in frontmatter:
                # paths 패턴 검증
                paths = frontmatter["paths"]
                if not isinstance(paths, str):
                    self.issues.append({
                        "type": "warning",
                        "category": "rule",
                        "file": str(file_path.relative_to(self.claude_dir)),
                        "message": "paths should be a string pattern"
                    })

            # Body가 있는지 확인
            if not body.strip():
                self.issues.append({
                    "type": "warning",
                    "category": "rule",
                    "file": str(file_path.relative_to(self.claude_dir)),
                    "message": "Empty rule body"
                })

            return True

        except Exception as e:
            self.issues.append({
                "type": "error",
                "category": "rule",
                "file": str(file_path.relative_to(self.claude_dir)),
                "message": f"Validation error: {str(e)}"
            })
            return False

    def _extract_frontmatter(self, content: str) -> Tuple[Optional[Dict], str]:
        """Frontmatter 추출"""
        if not content.startswith("---"):
            return None, content

        parts = content.split("---", 2)
        if len(parts) < 3:
            return None, content

        try:
            frontmatter = yaml.safe_load(parts[1])
            body = parts[2].strip()
            return frontmatter, body
        except yaml.YAMLError:
            return None, content

    def _generate_report(self) -> Dict[str, Any]:
        """최종 리포트 생성"""
        # 이슈를 심각도별로 분류
        errors = [i for i in self.issues if i["type"] == "error"]
        warnings = [i for i in self.issues if i["type"] == "warning"]

        return {
            "summary": self.summary,
            "errors": errors,
            "warnings": warnings,
            "total_issues": len(self.issues),
            "is_valid": len(errors) == 0
        }


def main():
    """CLI 실행"""
    import argparse
    import os

    parser = argparse.ArgumentParser(description="Claude Code Config Validator")
    parser.add_argument("--target", default=".claude", help=".claude directory path")
    parser.add_argument("--output", default=".claude/config-report.json", help="Output file path")

    args = parser.parse_args()

    # 검증 실행
    validator = ConfigValidator(claude_dir=args.target)
    report = validator.validate()

    # 결과 저장
    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)

    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(report, f, indent=2, ensure_ascii=False)

    print(f"✅ Report saved to: {output_path}")
    print(f"\n📊 Summary:")
    for category, stats in report["summary"].items():
        print(f"  {category.capitalize()}:")
        print(f"    - Total: {stats['total']}")
        print(f"    - Valid: {stats['valid']}")
        print(f"    - Invalid: {stats['invalid']}")

    print(f"\n  Total Issues: {report['total_issues']}")
    print(f"  - Errors: {len(report['errors'])}")
    print(f"  - Warnings: {len(report['warnings'])}")
    print(f"  Overall Valid: {'✅ Yes' if report['is_valid'] else '❌ No'}")


if __name__ == "__main__":
    main()
