#!/usr/bin/env python3
"""
Tech Rule Generator
GitHub API와 문서 검색을 통해 기술 스택별 규칙 파일을 자동 생성합니다.
"""

import json
import re
import os
from pathlib import Path
from typing import Dict, List, Any, Optional
import subprocess
import sys


class TechRuleGenerator:
    """기술 스택 규칙 자동 생성기"""

    def __init__(
        self,
        output_dir: str = ".claude/rules",
        cache_dir: str = ".claude/cache"
    ):
        self.output_dir = Path(output_dir)
        self.cache_dir = Path(cache_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.cache_dir.mkdir(parents=True, exist_ok=True)

    def generate_rule(
        self,
        tech_name: str,
        file_pattern: Optional[str] = None,
        search_github: bool = True,
        use_cache: bool = True
    ) -> str:
        """
        기술 스택 규칙 파일 생성

        Args:
            tech_name: 기술 이름 (react, vue, django 등)
            file_pattern: 파일 패턴 (예: "**/*.tsx")
            search_github: GitHub 검색 여부
            use_cache: 캐시 사용 여부

        Returns:
            생성된 파일 경로
        """
        print(f"🔍 Generating rule for: {tech_name}\n")

        # 1. 캐시 확인
        if use_cache:
            cached_data = self._check_cache(tech_name)
            if cached_data:
                print("✅ Using cached data")
                return self._generate_markdown(tech_name, cached_data, file_pattern)

        # 2. 기술 정보 수집
        tech_data = {
            "name": tech_name,
            "best_practices": [],
            "common_patterns": [],
            "anti_patterns": [],
            "naming_conventions": [],
            "file_structure": [],
            "testing": []
        }

        # 3. GitHub 검색 (선택적)
        if search_github:
            github_data = self._search_github_awesome_list(tech_name)
            if github_data:
                tech_data.update(github_data)

        # 4. 내장 템플릿 사용
        template_data = self._get_builtin_template(tech_name)
        if template_data:
            tech_data = self._merge_data(tech_data, template_data)

        # 5. 캐시 저장
        if use_cache:
            self._save_cache(tech_name, tech_data)

        # 6. 마크다운 생성
        return self._generate_markdown(tech_name, tech_data, file_pattern)

    def _check_cache(self, tech_name: str) -> Optional[Dict]:
        """캐시된 데이터 확인"""
        cache_file = self.cache_dir / f"tech_rule_{tech_name}.json"
        if not cache_file.exists():
            return None

        try:
            with open(cache_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
            print(f"📦 Cache hit: {cache_file.name}")
            return data
        except Exception:
            return None

    def _save_cache(self, tech_name: str, data: Dict) -> None:
        """데이터 캐시 저장"""
        cache_file = self.cache_dir / f"tech_rule_{tech_name}.json"
        try:
            with open(cache_file, 'w', encoding='utf-8') as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
            print(f"💾 Cached to: {cache_file.name}")
        except Exception as e:
            print(f"⚠️  Cache save failed: {e}")

    def _search_github_awesome_list(self, tech_name: str) -> Optional[Dict]:
        """GitHub Awesome 리스트 검색"""
        print(f"🔎 Searching GitHub for awesome-{tech_name}...")

        try:
            # gh CLI 사용 (설치되어 있다면)
            result = subprocess.run(
                [
                    "gh", "search", "repos",
                    f"awesome-{tech_name}",
                    "--limit", "5",
                    "--json", "name,description,url,stargazersCount"
                ],
                capture_output=True,
                text=True,
                timeout=30
            )

            if result.returncode == 0 and result.stdout:
                repos = json.loads(result.stdout)
                if repos:
                    print(f"✅ Found {len(repos)} awesome lists on GitHub")
                    return {
                        "github_resources": [
                            {
                                "name": repo["name"],
                                "url": repo["url"],
                                "stars": repo.get("stargazersCount", 0)
                            }
                            for repo in repos[:3]
                        ]
                    }

        except FileNotFoundError:
            print("⚠️  gh CLI not found, skipping GitHub search")
        except subprocess.TimeoutExpired:
            print("⚠️  GitHub search timeout")
        except Exception as e:
            print(f"⚠️  GitHub search failed: {e}")

        return None

    def _get_builtin_template(self, tech_name: str) -> Dict:
        """내장 템플릿 반환"""
        templates = {
            "react": {
                "best_practices": [
                    "Use functional components with hooks",
                    "Implement proper error boundaries",
                    "Memoize expensive computations with useMemo/useCallback",
                    "Keep components small and focused (Single Responsibility)",
                    "Use TypeScript for type safety"
                ],
                "common_patterns": [
                    "Custom Hooks for reusable logic",
                    "Context API for global state",
                    "Component composition over inheritance",
                    "Controlled vs Uncontrolled components"
                ],
                "anti_patterns": [
                    "Prop drilling (use Context or state management)",
                    "Mutating state directly",
                    "Missing dependency arrays in useEffect",
                    "Too many useState hooks (consider useReducer)"
                ],
                "naming_conventions": [
                    "Components: PascalCase (UserProfile.tsx)",
                    "Hooks: camelCase starting with 'use' (useAuth.ts)",
                    "Files: match component name",
                    "Test files: ComponentName.test.tsx"
                ],
                "file_structure": [
                    "src/components/ - Reusable UI components",
                    "src/pages/ - Page components",
                    "src/hooks/ - Custom hooks",
                    "src/context/ - Context providers",
                    "src/utils/ - Utility functions"
                ],
                "testing": [
                    "React Testing Library for component tests",
                    "Jest for unit tests",
                    "Test user interactions, not implementation",
                    "Aim for >70% coverage on business logic"
                ]
            },
            "vue": {
                "best_practices": [
                    "Use Composition API for better TypeScript support",
                    "Keep components small and reusable",
                    "Use proper prop validation",
                    "Implement computed properties for derived state"
                ],
                "naming_conventions": [
                    "Components: PascalCase (UserProfile.vue)",
                    "Composables: camelCase starting with 'use' (useAuth.ts)",
                    "Events: kebab-case (user-updated)"
                ]
            },
            "django": {
                "best_practices": [
                    "Follow Django's MVT pattern strictly",
                    "Use Django ORM efficiently (select_related, prefetch_related)",
                    "Implement proper permission checks",
                    "Use Django forms for validation"
                ],
                "common_patterns": [
                    "Class-based views for complex logic",
                    "Function-based views for simple endpoints",
                    "Mixins for reusable view logic",
                    "Signals for decoupled actions"
                ],
                "naming_conventions": [
                    "Models: Singular, PascalCase (User, BlogPost)",
                    "Views: snake_case (user_list_view)",
                    "URLs: kebab-case (/blog-posts/)",
                    "Apps: plural, snake_case (blog_posts)"
                ]
            },
            "fastapi": {
                "best_practices": [
                    "Use Pydantic models for request/response validation",
                    "Implement dependency injection",
                    "Use async/await for I/O operations",
                    "Leverage automatic OpenAPI documentation"
                ],
                "common_patterns": [
                    "Router-based modular structure",
                    "Background tasks for async operations",
                    "Middleware for cross-cutting concerns"
                ]
            },
            "spring": {
                "best_practices": [
                    "Use constructor injection over field injection",
                    "Apply @Transactional appropriately",
                    "Separate DTOs from Entities",
                    "Use @Valid for request validation"
                ],
                "common_patterns": [
                    "Service layer for business logic",
                    "Repository pattern for data access",
                    "ControllerAdvice for global exception handling"
                ],
                "naming_conventions": [
                    "Controllers: *Controller suffix",
                    "Services: *Service suffix (interface) + *ServiceImpl",
                    "Repositories: *Repository suffix",
                    "DTOs: *Request/*Response suffix"
                ]
            }
        }

        tech_lower = tech_name.lower()
        return templates.get(tech_lower, {})

    def _merge_data(self, base: Dict, new: Dict) -> Dict:
        """데이터 병합 (중복 제거)"""
        merged = base.copy()
        for key, value in new.items():
            if key in merged and isinstance(merged[key], list) and isinstance(value, list):
                # 리스트 병합 (중복 제거)
                merged[key] = list(set(merged[key] + value))
            else:
                merged[key] = value
        return merged

    def _generate_markdown(
        self,
        tech_name: str,
        data: Dict,
        file_pattern: Optional[str]
    ) -> str:
        """마크다운 파일 생성"""
        filename = f"{tech_name.lower()}.md"
        filepath = self.output_dir / filename

        # Frontmatter
        frontmatter = "---\n"
        if file_pattern:
            frontmatter += f'paths: "{file_pattern}"\n'
        frontmatter += "---\n\n"

        # Content
        content = f"# {tech_name.title()} Project Rules\n\n"
        content += f"이 규칙은 {tech_name} 프로젝트에 적용됩니다.\n\n"

        # Best Practices
        if data.get("best_practices"):
            content += "## Best Practices\n\n"
            for practice in data["best_practices"]:
                content += f"- {practice}\n"
            content += "\n"

        # Common Patterns
        if data.get("common_patterns"):
            content += "## Common Patterns\n\n"
            for pattern in data["common_patterns"]:
                content += f"- {pattern}\n"
            content += "\n"

        # Anti-patterns
        if data.get("anti_patterns"):
            content += "## Anti-patterns to Avoid\n\n"
            for anti in data["anti_patterns"]:
                content += f"- ❌ {anti}\n"
            content += "\n"

        # Naming Conventions
        if data.get("naming_conventions"):
            content += "## Naming Conventions\n\n"
            for convention in data["naming_conventions"]:
                content += f"- {convention}\n"
            content += "\n"

        # File Structure
        if data.get("file_structure"):
            content += "## Recommended File Structure\n\n"
            content += "```\n"
            for structure in data["file_structure"]:
                content += f"{structure}\n"
            content += "```\n\n"

        # Testing
        if data.get("testing"):
            content += "## Testing Guidelines\n\n"
            for test in data["testing"]:
                content += f"- {test}\n"
            content += "\n"

        # GitHub Resources
        if data.get("github_resources"):
            content += "## Resources\n\n"
            for resource in data["github_resources"]:
                stars = resource.get("stars", 0)
                content += f"- [{resource['name']}]({resource['url']}) ⭐ {stars:,}\n"
            content += "\n"

        # Write file
        full_content = frontmatter + content

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(full_content)

        print(f"\n✅ Rule file created: {filepath}")
        return str(filepath)


def main():
    """CLI 실행"""
    import argparse

    parser = argparse.ArgumentParser(description="Tech Rule Generator")
    parser.add_argument("tech_name", help="Technology name (react, vue, django, etc.)")
    parser.add_argument("--pattern", help="File pattern (e.g., '**/*.tsx')")
    parser.add_argument("--no-github", action="store_true", help="Skip GitHub search")
    parser.add_argument("--no-cache", action="store_true", help="Skip cache")
    parser.add_argument("--output-dir", default=".claude/rules", help="Output directory")

    args = parser.parse_args()

    # 생성
    generator = TechRuleGenerator(output_dir=args.output_dir)
    filepath = generator.generate_rule(
        tech_name=args.tech_name,
        file_pattern=args.pattern,
        search_github=not args.no_github,
        use_cache=not args.no_cache
    )

    print(f"\n🎉 Done! Check the file: {filepath}")


if __name__ == "__main__":
    main()
