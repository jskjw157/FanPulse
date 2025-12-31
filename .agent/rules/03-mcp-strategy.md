---
trigger: always_on
---

# MCP Strategy (Short) — Current Stack Only
# MCPs available: deepcontext (wild-card.ai), docfork, context7, github, Sequential Thinking

## Objectives
- Max correctness with minimal context.
- Token efficiency via: internal-first, one-doc-tool-first, stop early.
- Avoid redundant MCP calls and repeated file reads.

## Source-of-Truth Priority
1) Internal codebase (our repo)
2) Public/official docs (docfork/context7)
3) Upstream references (github MCP)
4) Web Content (crawl4ai) — for user-provided URLs or scoped targets when docs are unavailable
5) Web search/browsing — only with explicit user permission (confirm scope + budget first)

## Router (Hard Guardrails)
### Internal codebase questions, CREATE, REFACTOR, & REVIEW requests
- Use deepcontext,docfork first to locate entry points and relevant modules.
- FOR REFACTOR/REVIEW: YOU MUST USE deepcontext,docfork FIRST.
- Only then read minimal sections needed.

### External documentation questions
- Default: docfork first.
- Fallback: context7 only if docfork is insufficient OR version-specific precision is required.
- Stop after first doc call if implementation is possible.

### GitHub MCP
- Use only for upstream issues/PRs/releases/examples when needed.
- Never as a substitute for internal search.

### Web Content (crawl4ai)
- Consent Gate:
  - If the user provides exact URL(s): you MAY use `scrape` without additional consent.
  - If URL discovery is required (e.g., “search for X”, “look up”, “find latest”):
    - You MUST ask for explicit permission before using `crawl` or crawling search/result pages.
    - You MUST confirm scope (allowed domains or seed URLs) and budget (max_pages) first.
- Prefer `scrape` (single URL) over `crawl` (multi-page).
- Budget: default to 1 `scrape` per turn; allow 1 `crawl` per turn only if strictly necessary.


## Practical Budgets
- Max doc MCP calls per task step: 2 (docfork → context7)
- Max total MCP calls per turn: 4
If exceeded: split into smaller steps and confirm priorities (TG.API vs TG.DESIGN).

## Workflow Binding (Procedural Standard)
When applicable, follow these workflow files:
- External docs lookup → workflows/WF.DOCS_LADDER.md
- Detail page generation → workflows/WF.DETAIL_PAGE_FACTORY.md
- Upstream verification → workflows/WF.GITHUB.UPSTREAM.md
- Web research (consent-gated crawl4ai) → workflows/WF.WEB_RESEARCH.md
- Rule generation / maintenance → workflows/setup-rules.md (and workflows/fetch-rule.md)