#!/usr/bin/env python3
"""
AI PR Code Reviewer - Hybrid (GLM + Gemini)
===========================================

GLM-4-Flash와 Gemini 2.5 Flash를 동시에 사용하는 하이브리드 PR 코드 리뷰 봇.
두 AI의 리뷰를 병합하여 더 정확하고 포괄적인 코드 리뷰를 제공합니다.

Features:
- 🔥 GLM-4-Flash (Zhipu AI): 빠르고 효율적인 코드 분석
- ⚡ Gemini 2.5 Flash: 안정성 + 빠른 응답 + 1M 컨텍스트
- 🔄 병렬 실행으로 빠른 리뷰
- 📊 두 AI 의견 병합 및 합의 도출
- 🛡️ 폴백 지원 (한 쪽 실패 시 다른 쪽 사용)
- 📦 파일별 청킹: 큰 diff를 자동으로 파일별로 분리하여 리뷰

Usage:
    # 기본 사용
    python script/ai_pr_reviewer.py --pr 123
    python script/ai_pr_reviewer.py --diff "$(git diff main)"

    # 단일 AI만 사용
    python script/ai_pr_reviewer.py --pr 123 --gemini-only
    python script/ai_pr_reviewer.py --pr 123 --glm-only

    # 청킹 제어
    python script/ai_pr_reviewer.py --pr 123 --no-chunk        # 청킹 비활성화
    python script/ai_pr_reviewer.py --pr 123 --chunk-threshold 50000  # 임계값 변경

Environment Variables:
    GLM_API_KEY: Zhipu AI API 키 (또는 ZHIPU_API_KEY)
    GEMINI_API_KEY: Google Gemini API 키 (또는 GOOGLE_API_KEY)
    GITHUB_TOKEN: GitHub API 토큰 (PR 코멘트용)
"""

import os
import json
import subprocess
import argparse
import asyncio
from pathlib import Path
from typing import Dict, List, Any, Optional, Tuple
from dataclasses import dataclass, asdict, field
from enum import Enum
from concurrent.futures import ThreadPoolExecutor, as_completed
import re

# Optional imports - graceful degradation
import sys
import io

# Windows 콘솔 인코딩 문제 해결
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')

try:
    import google.generativeai as genai
    GEMINI_AVAILABLE = True
except ImportError:
    GEMINI_AVAILABLE = False
    print("[WARN] google-generativeai not installed. Gemini will be disabled.")

try:
    from openai import OpenAI
    OPENAI_AVAILABLE = True
except ImportError:
    OPENAI_AVAILABLE = False
    print("[WARN] openai package not installed. GLM will be disabled.")


# ============================================================================
# Diff Chunking - 파일별 분리
# ============================================================================

@dataclass
class DiffChunk:
    """파일별 diff 청크"""
    file_path: str
    content: str
    size: int

    @property
    def is_code_file(self) -> bool:
        """코드 파일 여부 (리뷰 대상)"""
        code_extensions = {'.kt', '.java', '.py', '.ts', '.tsx', '.js', '.jsx', '.swift', '.go', '.rs'}
        return any(self.file_path.endswith(ext) for ext in code_extensions)


def parse_diff_by_files(diff: str) -> List[DiffChunk]:
    """
    Git diff를 파일별로 분리

    Returns:
        List[DiffChunk]: 파일별 diff 청크 리스트
    """
    chunks = []

    # diff --git a/path/file b/path/file 패턴으로 분리
    pattern = r'^diff --git a/(.+?) b/(.+?)$'

    # 각 파일의 시작 위치 찾기
    file_starts = []
    for match in re.finditer(pattern, diff, re.MULTILINE):
        file_starts.append({
            'start': match.start(),
            'file_path': match.group(2)  # b/path 사용
        })

    if not file_starts:
        # diff 형식이 아닌 경우 전체를 하나의 청크로
        return [DiffChunk(file_path="unknown", content=diff, size=len(diff))]

    # 각 파일의 diff 내용 추출
    for i, file_info in enumerate(file_starts):
        start = file_info['start']
        end = file_starts[i + 1]['start'] if i + 1 < len(file_starts) else len(diff)

        content = diff[start:end].strip()
        chunks.append(DiffChunk(
            file_path=file_info['file_path'],
            content=content,
            size=len(content)
        ))

    return chunks


def group_chunks_by_size(chunks: List[DiffChunk], max_size: int = 30000) -> List[List[DiffChunk]]:
    """
    청크들을 max_size 이하로 그룹화

    Args:
        chunks: 파일별 diff 청크 리스트
        max_size: 그룹당 최대 크기 (기본 30KB)

    Returns:
        그룹화된 청크 리스트
    """
    groups = []
    current_group = []
    current_size = 0

    # 코드 파일만 필터링 (설정 파일 등 제외)
    code_chunks = [c for c in chunks if c.is_code_file]

    # 크기순 정렬 (큰 파일 먼저 - bin packing)
    sorted_chunks = sorted(code_chunks, key=lambda x: x.size, reverse=True)

    for chunk in sorted_chunks:
        # 단일 파일이 max_size 초과하면 별도 그룹
        if chunk.size > max_size:
            if current_group:
                groups.append(current_group)
                current_group = []
                current_size = 0
            groups.append([chunk])
            continue

        # 현재 그룹에 추가 가능하면 추가
        if current_size + chunk.size <= max_size:
            current_group.append(chunk)
            current_size += chunk.size
        else:
            # 새 그룹 시작
            if current_group:
                groups.append(current_group)
            current_group = [chunk]
            current_size = chunk.size

    if current_group:
        groups.append(current_group)

    return groups


def merge_chunks_to_diff(chunks: List[DiffChunk]) -> str:
    """청크 리스트를 하나의 diff 문자열로 병합"""
    return "\n\n".join(chunk.content for chunk in chunks)


class Severity(Enum):
    """리뷰 이슈 심각도"""
    CRITICAL = "critical"  # 🔴 보안, 버그, 크래시
    HIGH = "high"          # 🟠 성능, 아키텍처
    MEDIUM = "medium"      # 🟡 코드 스타일, 베스트 프랙티스
    LOW = "low"            # 🟢 제안, 개선사항
    INFO = "info"          # ℹ️ 정보성 코멘트


@dataclass
class ReviewIssue:
    """코드 리뷰 이슈"""
    file_path: str
    line_number: Optional[int]
    severity: Severity
    category: str  # security, performance, bug, style, suggestion
    title: str
    description: str
    suggestion: Optional[str] = None
    source: str = "unknown"  # glm, gemini, merged


@dataclass
class ReviewResult:
    """AI 리뷰 결과"""
    provider: str
    success: bool
    issues: List[ReviewIssue] = field(default_factory=list)
    summary: str = ""
    raw_response: str = ""
    error: Optional[str] = None


@dataclass
class MergedReview:
    """병합된 최종 리뷰"""
    glm_result: Optional[ReviewResult]
    gemini_result: Optional[ReviewResult]
    merged_issues: List[ReviewIssue] = field(default_factory=list)
    consensus_issues: List[ReviewIssue] = field(default_factory=list)  # 두 AI 모두 지적
    summary: str = ""
    stats: Dict[str, int] = field(default_factory=dict)


class GLMReviewer:
    """GLM-4-Flash 기반 코드 리뷰어 (Zhipu AI)"""

    def __init__(self, api_key: Optional[str] = None):
        self.api_key = api_key or os.environ.get("GLM_API_KEY") or os.environ.get("ZHIPU_API_KEY")
        self.client = None

        if self.api_key and OPENAI_AVAILABLE:
            # GLM은 OpenAI 호환 API 제공
            self.client = OpenAI(
                api_key=self.api_key,
                base_url="https://open.bigmodel.cn/api/paas/v4/"
            )
    
    @property
    def is_available(self) -> bool:
        return self.client is not None
    
    def review(self, diff: str, context: Dict[str, Any] = None) -> ReviewResult:
        """GLM으로 코드 리뷰 수행"""
        if not self.is_available:
            return ReviewResult(
                provider="glm",
                success=False,
                error="GLM API not configured"
            )

        try:
            prompt = self._build_prompt(diff, context)

            response = self.client.chat.completions.create(
                model="glm-4-flash",
                messages=[
                    {"role": "system", "content": self._get_system_prompt()},
                    {"role": "user", "content": prompt}
                ],
                max_tokens=4096,
                temperature=0.1
            )

            raw_response = response.choices[0].message.content
            issues = self._parse_response(raw_response)

            return ReviewResult(
                provider="glm",
                success=True,
                issues=issues,
                summary=self._extract_summary(raw_response),
                raw_response=raw_response
            )
            
        except Exception as e:
            return ReviewResult(
                provider="glm",
                success=False,
                error=str(e)
            )
    
    def _get_system_prompt(self) -> str:
        return """You are an expert code reviewer for FanPulse, a K-POP fan platform.

Tech Stack:
- Backend: Kotlin + Spring Boot 3.2 + PostgreSQL + MongoDB + Redis
- Frontend: Next.js + TypeScript + TailwindCSS
- Mobile: Android (Jetpack Compose), iOS (UIKit/SwiftUI)

Review Guidelines:
1. Focus on REAL bugs and security issues first
2. Check for performance problems (N+1 queries, memory leaks)
3. Verify error handling and edge cases
4. Suggest improvements for code quality

Output Format (JSON):
{
  "summary": "Brief overall assessment",
  "issues": [
    {
      "file": "path/to/file.kt",
      "line": 42,
      "severity": "critical|high|medium|low",
      "category": "security|bug|performance|style|suggestion",
      "title": "Short issue title",
      "description": "Detailed explanation",
      "suggestion": "How to fix (optional)"
    }
  ]
}

Be specific with file paths and line numbers when possible.
Focus on actionable feedback, not nitpicks."""
    
    def _build_prompt(self, diff: str, context: Dict[str, Any] = None) -> str:
        MAX_DIFF_SIZE = 30000
        truncated = len(diff) > MAX_DIFF_SIZE

        if truncated:
            print(f"[WARN] GLM: Diff truncated ({len(diff)} -> {MAX_DIFF_SIZE} chars)")
            diff_content = diff[:MAX_DIFF_SIZE] + "\n\n[... truncated, showing first 30KB ...]"
        else:
            diff_content = diff

        prompt = f"Review this code change:\n\n```diff\n{diff_content}\n```"

        if context:
            if context.get("pr_title"):
                prompt = f"PR: {context['pr_title']}\n\n" + prompt
            if context.get("pr_description"):
                prompt += f"\n\nPR Description: {context['pr_description']}"

        return prompt
    
    def _parse_response(self, response: str) -> List[ReviewIssue]:
        """응답에서 이슈 파싱"""
        issues = []
        data = self._extract_json(response)

        if data:
            for item in data.get("issues", []):
                try:
                    issues.append(ReviewIssue(
                        file_path=item.get("file", "unknown"),
                        line_number=item.get("line"),
                        severity=Severity(item.get("severity", "medium")),
                        category=item.get("category", "suggestion"),
                        title=item.get("title", ""),
                        description=item.get("description", ""),
                        suggestion=item.get("suggestion"),
                        source="glm"
                    ))
                except (ValueError, KeyError) as e:
                    print(f"[WARN] Skipping invalid issue: {e}")

        return issues
    
    def _extract_json(self, response: str) -> Optional[dict]:
        """응답에서 JSON 안전하게 추출"""
        # 1. JSON 코드 블록 우선 시도
        json_block = re.search(r'```json\s*(\{[\s\S]*?\})\s*```', response)
        if json_block:
            try:
                return json.loads(json_block.group(1))
            except json.JSONDecodeError:
                pass

        # 2. 첫 번째 유효한 JSON 객체 찾기 (balanced braces)
        start = response.find('{')
        if start == -1:
            return None

        depth = 0
        for i in range(start, len(response)):
            if response[i] == '{':
                depth += 1
            elif response[i] == '}':
                depth -= 1
                if depth == 0:
                    try:
                        return json.loads(response[start:i + 1])
                    except json.JSONDecodeError:
                        # 이 JSON이 유효하지 않으면 다음 시작점 찾기
                        start = response.find('{', i + 1)
                        if start == -1:
                            return None
                        depth = 0
                        continue
        return None

    def _extract_summary(self, response: str) -> str:
        """응답에서 요약 추출"""
        data = self._extract_json(response)
        if data:
            return data.get("summary", "")
        return ""


class GeminiReviewer:
    """Google Gemini 2.5 Flash 기반 코드 리뷰어"""
    
    def __init__(self, api_key: Optional[str] = None):
        self.api_key = api_key or os.environ.get("GEMINI_API_KEY") or os.environ.get("GOOGLE_API_KEY")
        self.model = None
        
        if self.api_key and GEMINI_AVAILABLE:
            genai.configure(api_key=self.api_key)
            self.model = genai.GenerativeModel('gemini-2.5-flash')
    
    @property
    def is_available(self) -> bool:
        return self.model is not None
    
    def review(self, diff: str, context: Dict[str, Any] = None) -> ReviewResult:
        """Gemini로 코드 리뷰 수행"""
        if not self.is_available:
            return ReviewResult(
                provider="gemini",
                success=False,
                error="Gemini API not configured"
            )
        
        try:
            prompt = self._build_prompt(diff, context)
            
            response = self.model.generate_content(
                prompt,
                generation_config=genai.GenerationConfig(
                    temperature=0.1,
                    max_output_tokens=4096
                )
            )
            
            raw_response = response.text

            # 디버깅: raw response 출력
            print(f"[DEBUG] Gemini raw response length: {len(raw_response)} chars")
            if len(raw_response) < 500:
                print(f"[DEBUG] Full response: {raw_response}")
            else:
                print(f"[DEBUG] Response preview: {raw_response[:500]}...")

            issues = self._parse_response(raw_response)
            
            return ReviewResult(
                provider="gemini",
                success=True,
                issues=issues,
                summary=self._extract_summary(raw_response),
                raw_response=raw_response
            )
            
        except Exception as e:
            return ReviewResult(
                provider="gemini",
                success=False,
                error=str(e)
            )
    
    def _build_prompt(self, diff: str, context: Dict[str, Any] = None) -> str:
        system_context = """You are an expert code reviewer for FanPulse, a K-POP fan platform.

Tech Stack:
- Backend: Kotlin + Spring Boot 3.2 + PostgreSQL + MongoDB + Redis  
- Frontend: Next.js + TypeScript + TailwindCSS
- Mobile: Android (Jetpack Compose), iOS (UIKit/SwiftUI)

Review Guidelines:
1. Security vulnerabilities (SQL injection, XSS, auth bypass)
2. Bug detection and edge cases
3. Performance issues (N+1, memory leaks, inefficient algorithms)
4. Code quality and maintainability
5. Best practices for the tech stack

Output Format (JSON):
{
  "summary": "Brief overall assessment",
  "issues": [
    {
      "file": "path/to/file.kt",
      "line": 42,
      "severity": "critical|high|medium|low",
      "category": "security|bug|performance|style|suggestion",
      "title": "Short issue title",
      "description": "Detailed explanation",
      "suggestion": "How to fix (optional)"
    }
  ]
}

Be thorough but focus on significant issues. Avoid nitpicks."""
        
        MAX_DIFF_SIZE = 50000
        truncated = len(diff) > MAX_DIFF_SIZE

        if truncated:
            print(f"⚠️  Gemini: Diff truncated ({len(diff)} → {MAX_DIFF_SIZE} chars)")
            diff_content = diff[:MAX_DIFF_SIZE] + "\n\n[... truncated, showing first 50KB ...]"
        else:
            diff_content = diff

        prompt = f"{system_context}\n\n---\n\nReview this code change:\n\n```diff\n{diff_content}\n```"

        if context:
            if context.get("pr_title"):
                prompt += f"\n\nPR Title: {context['pr_title']}"
            if context.get("pr_description"):
                prompt += f"\nPR Description: {context['pr_description']}"

        return prompt
    
    def _parse_response(self, response: str) -> List[ReviewIssue]:
        """응답에서 이슈 파싱"""
        issues = []
        data = self._extract_json(response)

        if data:
            for item in data.get("issues", []):
                try:
                    issues.append(ReviewIssue(
                        file_path=item.get("file", "unknown"),
                        line_number=item.get("line"),
                        severity=Severity(item.get("severity", "medium")),
                        category=item.get("category", "suggestion"),
                        title=item.get("title", ""),
                        description=item.get("description", ""),
                        suggestion=item.get("suggestion"),
                        source="gemini"
                    ))
                except (ValueError, KeyError) as e:
                    print(f"⚠️  Skipping invalid issue: {e}")

        return issues

    def _extract_json(self, response: str) -> Optional[dict]:
        """응답에서 JSON 안전하게 추출"""
        # 1. JSON 코드 블록 우선 시도
        json_block = re.search(r'```json\s*(\{[\s\S]*?\})\s*```', response)
        if json_block:
            try:
                return json.loads(json_block.group(1))
            except json.JSONDecodeError:
                pass

        # 2. 첫 번째 유효한 JSON 객체 찾기 (balanced braces)
        start = response.find('{')
        if start == -1:
            return None

        depth = 0
        for i in range(start, len(response)):
            if response[i] == '{':
                depth += 1
            elif response[i] == '}':
                depth -= 1
                if depth == 0:
                    try:
                        return json.loads(response[start:i + 1])
                    except json.JSONDecodeError:
                        start = response.find('{', i + 1)
                        if start == -1:
                            return None
                        depth = 0
                        continue
        return None

    def _extract_summary(self, response: str) -> str:
        """응답에서 요약 추출"""
        data = self._extract_json(response)
        if data:
            return data.get("summary", "")
        return ""


class HybridReviewer:
    """GLM + Gemini 하이브리드 리뷰어"""

    def __init__(self, glm_key: str = None, gemini_key: str = None):
        self.glm = GLMReviewer(glm_key)
        self.gemini = GeminiReviewer(gemini_key)

    def review(self, diff: str, context: Dict[str, Any] = None, parallel: bool = True) -> MergedReview:
        """두 AI로 동시에 리뷰하고 결과 병합"""

        glm_result = None
        gemini_result = None

        if parallel and self.glm.is_available and self.gemini.is_available:
            # 병렬 실행
            with ThreadPoolExecutor(max_workers=2) as executor:
                futures = {
                    executor.submit(self.glm.review, diff, context): "glm",
                    executor.submit(self.gemini.review, diff, context): "gemini"
                }
                
                for future in as_completed(futures):
                    provider = futures[future]
                    try:
                        result = future.result(timeout=120)
                        if provider == "glm":
                            glm_result = result
                        else:
                            gemini_result = result
                    except Exception as e:
                        print(f"[WARN] {provider} failed: {e}")
        else:
            # 순차 실행
            if self.glm.is_available:
                print("Running GLM review...")
                glm_result = self.glm.review(diff, context)

            if self.gemini.is_available:
                print("Running Gemini review...")
                gemini_result = self.gemini.review(diff, context)
        
        # 결과 병합
        return self._merge_results(glm_result, gemini_result)

    def _merge_results(self, glm: Optional[ReviewResult], gemini: Optional[ReviewResult]) -> MergedReview:
        """두 리뷰 결과 병합"""
        merged = MergedReview(
            glm_result=glm,
            gemini_result=gemini
        )

        all_issues: List[ReviewIssue] = []
        
        # 이슈 수집
        if glm and glm.success:
            all_issues.extend(glm.issues)
        if gemini and gemini.success:
            all_issues.extend(gemini.issues)
        
        # 중복 제거 및 합의 식별
        seen = {}
        for issue in all_issues:
            key = f"{issue.file_path}:{issue.line_number}:{issue.category}"
            
            if key in seen:
                # 두 AI가 같은 이슈 지적 = 합의
                existing = seen[key]
                if existing.source != issue.source:
                    # 심각도는 더 높은 것 선택
                    severity_order = [Severity.CRITICAL, Severity.HIGH, Severity.MEDIUM, Severity.LOW, Severity.INFO]
                    if severity_order.index(issue.severity) < severity_order.index(existing.severity):
                        existing.severity = issue.severity
                    existing.source = "consensus"
                    merged.consensus_issues.append(existing)
            else:
                seen[key] = issue
        
        # 정렬: 합의 이슈 > Critical > High > Medium > Low
        merged.merged_issues = sorted(
            seen.values(),
            key=lambda x: (
                x.source != "consensus",
                [Severity.CRITICAL, Severity.HIGH, Severity.MEDIUM, Severity.LOW, Severity.INFO].index(x.severity)
            )
        )
        
        # 통계
        merged.stats = {
            "total_issues": len(merged.merged_issues),
            "consensus_issues": len(merged.consensus_issues),
            "glm_only": len([i for i in merged.merged_issues if i.source == "glm"]),
            "gemini_only": len([i for i in merged.merged_issues if i.source == "gemini"]),
            "critical": len([i for i in merged.merged_issues if i.severity == Severity.CRITICAL]),
            "high": len([i for i in merged.merged_issues if i.severity == Severity.HIGH]),
            "medium": len([i for i in merged.merged_issues if i.severity == Severity.MEDIUM]),
            "low": len([i for i in merged.merged_issues if i.severity == Severity.LOW]),
        }
        
        # 요약 생성
        merged.summary = self._generate_summary(merged)
        
        return merged
    
    def _generate_summary(self, merged: MergedReview) -> str:
        """병합된 리뷰 요약 생성"""
        parts = []
        
        if merged.stats["critical"] > 0:
            parts.append(f"🔴 {merged.stats['critical']} critical issue(s)")
        if merged.stats["high"] > 0:
            parts.append(f"🟠 {merged.stats['high']} high priority issue(s)")
        if merged.stats["medium"] > 0:
            parts.append(f"🟡 {merged.stats['medium']} medium issue(s)")
        if merged.stats["low"] > 0:
            parts.append(f"🟢 {merged.stats['low']} suggestion(s)")
        
        if merged.stats["consensus_issues"] > 0:
            parts.append(f"⚠️ {merged.stats['consensus_issues']} issue(s) flagged by BOTH AI reviewers")
        
        if not parts:
            return "✅ No significant issues found. LGTM!"

        return " | ".join(parts)


class ChunkedReviewer:
    """
    파일별 청킹을 지원하는 리뷰어

    큰 diff를 파일별로 나눠서 병렬로 리뷰하고 결과를 병합합니다.
    """

    # 청킹 임계값 (이 크기 이상이면 파일별 분리)
    CHUNK_THRESHOLD = 40000  # 40KB
    MAX_CHUNK_SIZE = 30000   # 각 청크 최대 30KB
    MAX_PARALLEL_CHUNKS = 5  # 동시 리뷰 최대 청크 수

    def __init__(self, glm_key: str = None, gemini_key: str = None):
        self.hybrid = HybridReviewer(glm_key, gemini_key)
        self.glm = self.hybrid.glm
        self.gemini = self.hybrid.gemini

    def review(
        self,
        diff: str,
        context: Dict[str, Any] = None,
        use_glm: bool = True,
        use_gemini: bool = True
    ) -> MergedReview:
        """
        스마트 리뷰: 크기에 따라 일반/청킹 방식 선택

        Args:
            diff: Git diff 내용
            context: PR 정보 (title, description 등)
            use_glm: GLM 사용 여부
            use_gemini: Gemini 사용 여부
        """
        diff_size = len(diff)

        # 작은 diff는 일반 리뷰
        if diff_size < self.CHUNK_THRESHOLD:
            print(f"📝 일반 리뷰 모드 (diff: {diff_size:,} chars)")
            return self._single_review(diff, context, use_glm, use_gemini)

        # 큰 diff는 청킹 리뷰
        print(f"📦 청킹 리뷰 모드 (diff: {diff_size:,} chars, threshold: {self.CHUNK_THRESHOLD:,})")
        return self._chunked_review(diff, context, use_glm, use_gemini)

    def _single_review(
        self,
        diff: str,
        context: Dict[str, Any],
        use_glm: bool,
        use_gemini: bool
    ) -> MergedReview:
        """일반 리뷰 (청킹 없음)"""
        if use_glm and use_gemini:
            return self.hybrid.review(diff, context)
        elif use_glm:
            result = self.glm.review(diff, context)
            return self._wrap_single_result(result, "glm")
        elif use_gemini:
            result = self.gemini.review(diff, context)
            return self._wrap_single_result(result, "gemini")
        else:
            return MergedReview(glm_result=None, gemini_result=None)

    def _chunked_review(
        self,
        diff: str,
        context: Dict[str, Any],
        use_glm: bool,
        use_gemini: bool
    ) -> MergedReview:
        """파일별 청킹 리뷰"""
        # 1. 파일별로 diff 분리
        chunks = parse_diff_by_files(diff)
        code_chunks = [c for c in chunks if c.is_code_file]

        print(f"   📁 총 {len(chunks)}개 파일 중 {len(code_chunks)}개 코드 파일 발견")

        if not code_chunks:
            print("   ⚠️ 리뷰할 코드 파일이 없습니다")
            return MergedReview(
                glm_result=None,
                gemini_result=None,
                summary="No code files to review"
            )

        # 2. 크기 기준으로 그룹화
        groups = group_chunks_by_size(code_chunks, self.MAX_CHUNK_SIZE)
        print(f"   📦 {len(groups)}개 그룹으로 분할")

        # 3. 각 그룹별 병렬 리뷰
        all_issues: List[ReviewIssue] = []

        # 동시 실행 제한
        groups_to_review = groups[:self.MAX_PARALLEL_CHUNKS]
        if len(groups) > self.MAX_PARALLEL_CHUNKS:
            print(f"   ⚠️ {len(groups) - self.MAX_PARALLEL_CHUNKS}개 그룹 스킵 (최대 {self.MAX_PARALLEL_CHUNKS}개)")

        with ThreadPoolExecutor(max_workers=self.MAX_PARALLEL_CHUNKS) as executor:
            futures = {}

            for i, group in enumerate(groups_to_review):
                group_diff = merge_chunks_to_diff(group)
                file_names = [c.file_path for c in group]

                print(f"   🔍 그룹 {i+1}: {len(group)}개 파일 ({len(group_diff):,} chars)")
                for name in file_names[:3]:  # 최대 3개만 출력
                    print(f"      - {name}")
                if len(file_names) > 3:
                    print(f"      ... 외 {len(file_names) - 3}개")

                # 리뷰 제출
                future = executor.submit(
                    self._review_chunk,
                    group_diff,
                    context,
                    use_glm,
                    use_gemini
                )
                futures[future] = i

            # 결과 수집
            for future in as_completed(futures):
                group_idx = futures[future]
                try:
                    result = future.result(timeout=180)  # 3분 타임아웃
                    issues = result.merged_issues if hasattr(result, 'merged_issues') else []
                    all_issues.extend(issues)
                    print(f"   ✅ 그룹 {group_idx + 1} 완료: {len(issues)}개 이슈")
                except Exception as e:
                    print(f"   ❌ 그룹 {group_idx + 1} 실패: {e}")

        # 4. 결과 병합
        return self._merge_chunked_results(all_issues, use_glm, use_gemini)

    def _review_chunk(
        self,
        diff: str,
        context: Dict[str, Any],
        use_glm: bool,
        use_gemini: bool
    ) -> MergedReview:
        """단일 청크 리뷰"""
        return self._single_review(diff, context, use_glm, use_gemini)

    def _wrap_single_result(self, result: ReviewResult, provider: str) -> MergedReview:
        """단일 리뷰 결과를 MergedReview로 래핑"""
        merged = MergedReview(
            glm_result=result if provider == "glm" else None,
            gemini_result=result if provider == "gemini" else None
        )
        merged.merged_issues = result.issues if result.success else []
        merged.summary = result.summary if result.success else (result.error or "Review failed")
        merged.stats = {
            "total_issues": len(merged.merged_issues),
            "consensus_issues": 0,
            "glm_only": len(merged.merged_issues) if provider == "glm" else 0,
            "gemini_only": len(merged.merged_issues) if provider == "gemini" else 0,
            "critical": len([i for i in merged.merged_issues if i.severity == Severity.CRITICAL]),
            "high": len([i for i in merged.merged_issues if i.severity == Severity.HIGH]),
            "medium": len([i for i in merged.merged_issues if i.severity == Severity.MEDIUM]),
            "low": len([i for i in merged.merged_issues if i.severity == Severity.LOW]),
        }
        return merged

    def _merge_chunked_results(
        self,
        all_issues: List[ReviewIssue],
        use_glm: bool,
        use_gemini: bool
    ) -> MergedReview:
        """청킹된 모든 결과 병합"""
        # 중복 제거 (같은 파일, 같은 라인, 같은 카테고리)
        seen = {}
        for issue in all_issues:
            key = f"{issue.file_path}:{issue.line_number}:{issue.category}:{issue.title[:30]}"
            if key not in seen:
                seen[key] = issue

        unique_issues = list(seen.values())

        # 심각도순 정렬
        severity_order = [Severity.CRITICAL, Severity.HIGH, Severity.MEDIUM, Severity.LOW, Severity.INFO]
        sorted_issues = sorted(
            unique_issues,
            key=lambda x: severity_order.index(x.severity)
        )

        merged = MergedReview(glm_result=None, gemini_result=None)
        merged.merged_issues = sorted_issues
        merged.stats = {
            "total_issues": len(sorted_issues),
            "consensus_issues": len([i for i in sorted_issues if i.source == "consensus"]),
            "glm_only": len([i for i in sorted_issues if i.source == "glm"]),
            "gemini_only": len([i for i in sorted_issues if i.source == "gemini"]),
            "critical": len([i for i in sorted_issues if i.severity == Severity.CRITICAL]),
            "high": len([i for i in sorted_issues if i.severity == Severity.HIGH]),
            "medium": len([i for i in sorted_issues if i.severity == Severity.MEDIUM]),
            "low": len([i for i in sorted_issues if i.severity == Severity.LOW]),
        }

        # 요약 생성
        parts = []
        if merged.stats["critical"] > 0:
            parts.append(f"🔴 {merged.stats['critical']} critical")
        if merged.stats["high"] > 0:
            parts.append(f"🟠 {merged.stats['high']} high")
        if merged.stats["medium"] > 0:
            parts.append(f"🟡 {merged.stats['medium']} medium")
        if merged.stats["low"] > 0:
            parts.append(f"🟢 {merged.stats['low']} low")

        merged.summary = " | ".join(parts) if parts else "✅ No significant issues found. LGTM!"

        return merged


def format_review_markdown(merged: MergedReview) -> str:
    """리뷰 결과를 마크다운으로 포맷"""
    lines = [
        "# 🤖 AI Code Review Results",
        "",
        f"**Reviewers:** GLM-4-Flash + Gemini 2.5 Flash (Hybrid)",
        "",
        f"## Summary",
        f"{merged.summary}",
        "",
    ]
    
    # 통계
    lines.extend([
        "### 📊 Statistics",
        f"- Total Issues: {merged.stats['total_issues']}",
        f"- Consensus (Both AIs agree): {merged.stats['consensus_issues']}",
        f"- GLM only: {merged.stats['glm_only']}",
        f"- Gemini only: {merged.stats['gemini_only']}",
        "",
    ])
    
    # 합의된 이슈 (가장 중요)
    if merged.consensus_issues:
        lines.extend([
            "## ⚠️ Consensus Issues (Both AIs Flagged)",
            "_These issues were identified by both AI reviewers and should be prioritized._",
            "",
        ])
        for issue in merged.consensus_issues:
            lines.extend(format_issue_markdown(issue))
    
    # Critical 이슈
    critical = [i for i in merged.merged_issues if i.severity == Severity.CRITICAL and i.source != "consensus"]
    if critical:
        lines.extend([
            "## 🔴 Critical Issues",
            "",
        ])
        for issue in critical:
            lines.extend(format_issue_markdown(issue))
    
    # High 이슈
    high = [i for i in merged.merged_issues if i.severity == Severity.HIGH and i.source != "consensus"]
    if high:
        lines.extend([
            "## 🟠 High Priority",
            "",
        ])
        for issue in high:
            lines.extend(format_issue_markdown(issue))
    
    # Medium 이슈
    medium = [i for i in merged.merged_issues if i.severity == Severity.MEDIUM and i.source != "consensus"]
    if medium:
        lines.extend([
            "## 🟡 Medium Priority",
            "",
        ])
        for issue in medium:
            lines.extend(format_issue_markdown(issue))
    
    # Low/Suggestions
    low = [i for i in merged.merged_issues if i.severity in [Severity.LOW, Severity.INFO] and i.source != "consensus"]
    if low:
        lines.extend([
            "## 🟢 Suggestions",
            "",
        ])
        for issue in low:
            lines.extend(format_issue_markdown(issue))
    
    # Footer
    lines.extend([
        "",
        "---",
        "_Powered by GLM-4-Flash (Zhipu AI) + Gemini 2.5 Flash_",
        "_Issues flagged by both AIs have higher confidence._",
    ])
    
    return "\n".join(lines)


def format_issue_markdown(issue: ReviewIssue) -> List[str]:
    """개별 이슈를 마크다운으로 포맷"""
    source_badge = {
        "glm": "🤖 GLM",
        "gemini": "✨ Gemini",
        "consensus": "🔥 Consensus"
    }.get(issue.source, issue.source)
    
    location = f"`{issue.file_path}`"
    if issue.line_number:
        location += f" (line {issue.line_number})"
    
    lines = [
        f"### {issue.title}",
        f"**Location:** {location}",
        f"**Category:** {issue.category} | **Source:** {source_badge}",
        "",
        issue.description,
        "",
    ]
    
    if issue.suggestion:
        lines.extend([
            "**Suggestion:**",
            f"> {issue.suggestion}",
            "",
        ])
    
    return lines


def get_pr_diff(pr_number: int) -> Tuple[str, Dict[str, Any]]:
    """GitHub PR의 diff와 컨텍스트 가져오기"""
    try:
        # PR 정보 가져오기
        result = subprocess.run(
            ["gh", "pr", "view", str(pr_number), "--json", "title,body,files"],
            capture_output=True,
            text=True,
            timeout=30
        )
        
        context = {}
        if result.returncode == 0:
            data = json.loads(result.stdout)
            context = {
                "pr_title": data.get("title", ""),
                "pr_description": data.get("body", ""),
            }
        
        # Diff 가져오기
        diff_result = subprocess.run(
            ["gh", "pr", "diff", str(pr_number)],
            capture_output=True,
            text=True,
            timeout=60
        )
        
        return diff_result.stdout, context
        
    except Exception as e:
        print(f"⚠️  Error fetching PR: {e}")
        return "", {}


def post_review_comment(pr_number: int, body: str) -> bool:
    """PR에 리뷰 코멘트 게시"""
    try:
        result = subprocess.run(
            ["gh", "pr", "comment", str(pr_number), "--body", body],
            capture_output=True,
            text=True,
            timeout=30
        )
        return result.returncode == 0
    except Exception as e:
        print(f"⚠️  Error posting comment: {e}")
        return False


def main():
    parser = argparse.ArgumentParser(description="AI PR Code Reviewer (GLM + Gemini Hybrid)")
    parser.add_argument("--pr", type=int, help="GitHub PR number to review")
    parser.add_argument("--diff", type=str, help="Direct diff content to review")
    parser.add_argument("--diff-file", type=str, help="File containing diff to review")
    parser.add_argument("--output", "-o", type=str, help="Output file for review (markdown)")
    parser.add_argument("--json", type=str, help="Output file for raw JSON results")
    parser.add_argument("--post-comment", action="store_true", help="Post review as PR comment")
    parser.add_argument("--glm-only", action="store_true", help="Use only GLM")
    parser.add_argument("--gemini-only", action="store_true", help="Use only Gemini")
    parser.add_argument("--no-chunk", action="store_true", help="Disable file chunking for large diffs")
    parser.add_argument("--chunk-threshold", type=int, default=40000,
                       help="Diff size threshold for chunking (default: 40000 chars)")

    args = parser.parse_args()
    
    # Diff 가져오기
    diff = ""
    context = {}
    
    if args.pr:
        print(f"📥 Fetching PR #{args.pr}...")
        diff, context = get_pr_diff(args.pr)
    elif args.diff:
        diff = args.diff
    elif args.diff_file:
        diff = Path(args.diff_file).read_text(encoding='utf-8')
    else:
        # stdin에서 읽기
        print("📥 Reading diff from stdin...")
        import sys
        diff = sys.stdin.read()
    
    if not diff.strip():
        print("❌ No diff content provided")
        return 1
    
    print(f"📝 Diff size: {len(diff):,} chars")

    # 리뷰어 초기화 (청킹 지원)
    reviewer = ChunkedReviewer()

    # 청킹 설정 적용
    if args.no_chunk:
        reviewer.CHUNK_THRESHOLD = float('inf')  # 청킹 비활성화
        print("⚠️ 청킹 비활성화됨 (--no-chunk)")
    else:
        reviewer.CHUNK_THRESHOLD = args.chunk_threshold

    # 사용할 AI 결정
    use_glm = not args.gemini_only
    use_gemini = not args.glm_only

    # API 가용성 체크
    if use_glm and not reviewer.glm.is_available:
        if args.glm_only:
            print("❌ GLM API not configured (GLM_API_KEY 환경변수 필요)")
            return 1
        use_glm = False

    if use_gemini and not reviewer.gemini.is_available:
        if args.gemini_only:
            print("❌ Gemini API not configured (GEMINI_API_KEY 환경변수 필요)")
            return 1
        use_gemini = False

    if not use_glm and not use_gemini:
        print("❌ No AI providers configured. Set GLM_API_KEY or GEMINI_API_KEY")
        return 1

    # 사용할 AI 표시
    providers = []
    if use_glm:
        providers.append("GLM-4-Flash")
    if use_gemini:
        providers.append("Gemini 2.5 Flash")
    print(f"🔍 AI Reviewers: {', '.join(providers)}")

    # 리뷰 실행 (자동 청킹)
    merged = reviewer.review(diff, context, use_glm=use_glm, use_gemini=use_gemini)
    
    # 결과 출력
    print("\n" + "="*60)
    print(merged.summary)
    print("="*60 + "\n")
    
    # 마크다운 생성
    markdown = format_review_markdown(merged)
    
    # 파일 출력
    if args.output:
        Path(args.output).write_text(markdown)
        print(f"📄 Review saved to: {args.output}")
    
    if args.json:
        json_data = {
            "summary": merged.summary,
            "stats": merged.stats,
            "issues": [asdict(i) for i in merged.merged_issues],
            "consensus_issues": [asdict(i) for i in merged.consensus_issues],
        }
        # Enum 직렬화
        for issue in json_data["issues"] + json_data["consensus_issues"]:
            issue["severity"] = issue["severity"].value if isinstance(issue["severity"], Severity) else issue["severity"]
        
        Path(args.json).write_text(json.dumps(json_data, indent=2, ensure_ascii=False))
        print(f"📄 JSON saved to: {args.json}")
    
    # PR 코멘트 게시
    if args.post_comment and args.pr:
        print(f"💬 Posting comment to PR #{args.pr}...")
        if post_review_comment(args.pr, markdown):
            print("✅ Comment posted successfully")
        else:
            print("❌ Failed to post comment")
    
    # 기본 출력
    if not args.output and not args.json:
        print(markdown)
    
    # Exit code: critical 이슈가 있으면 1
    if merged.stats.get("critical", 0) > 0:
        return 1
    return 0


if __name__ == "__main__":
    exit(main())
