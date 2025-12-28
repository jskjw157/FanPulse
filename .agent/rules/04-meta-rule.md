---
trigger: always_on
---

# Meta-Rule: Automatic Rule Generator (Executable)

## Purpose
Automatically generate and maintain workspace rules using the CURRENT stack:
- deepcontext (wild-card.ai) for internal repo analysis
- docfork / context7 for public official docs injection
- github MCP for upstream templates / canonical references
- crawl4ai for web-only rule pages (consent-gated)
- Sequential Thinking only for large/ambiguous decomposition

Primary goals:
- Minimal tokens, maximal correctness.
- Internal-first. One-doc-tool-first. Stop early.

## Trigger Conditions
Activate when the user says any of:
- "generate rules for this project"
- "create custom rules"
- "setup project rules"
- "analyze and suggest rules"
- "/setup-rules" or "/fetch-rule"

## Agent-Proposed Trigger (Proactive)
The agent MAY propose rule generation when it detects:
- Repeated user requests in the same domain (e.g., Cafe24 OAuth flow, skin overrides, detail page templates)
- Recurring corrections to the same patterns (linting, naming, error handling, auth edge cases)
- Multiple doc MCP calls for the same library/framework across turns
- Token-heavy context repeatedly used to solve similar tasks

Consent requirement:
- The agent MUST ask for explicit approval before running `/setup-rules`, `/update-rules`, or `/fetch-rule` proactively.


## Rule Output Layout (Workspace)
- `.agent/rules/{technology}.md` (per-tech rules)
- `.agent/rules/auto-generated.md` (combined index + priorities + deltas)

## Commands Supported (must map to workflows)
- `/setup-rules` — detect stack, fetch/synthesize rules, create auto-generated index
- `/fetch-rule {tech}` — fetch/synthesize rules for a specific technology
- `/update-rules` — refresh existing rule files (same sources ladder)
- `/list-rules` — display currently present rule files + last update notes

## Generation Process (Executable)

### Step 1: Analyze Project Context (deepcontext)
Use deepcontext to:
- Identify stack: read `package.json`, `pnpm-lock.yaml` / `package-lock.json`, `pyproject.toml`, `requirements.txt`, `go.mod`
- Detect framework signals via file/folder patterns:
  - React/Next: `src/app`, `next.config.*`, `pages/`, `components/`
  - Electron: `electron.*`, `main.ts`, `preload.ts`
  - Cafe24: oauth callbacks, `cafe24`, `mall`, `scope`, `redirect_uri`, `token` keywords
- Identify local conventions:
  - lint/prettier/eslint configs
  - tsconfig settings
  - folder naming and import style

Output: a normalized list of technologies to cover:
- Core: `workspace`, `git`, `security`, `testing`
- Stack-specific: e.g., `typescript`, `react`, `nextjs`, `electron`, `tailwind`
- Domain-specific: `cafe24-api`, `cafe24-skin`, `detail-page`

### Step 2: Fetch Rules (Source Ladder; stop early)
For each technology, fetch using this order:

1) **Existing workspace rules** (deepcontext)
   - If `.agent/rules/{tech}.md` exists and is sufficient, reuse it.
   - If user asked update, proceed to step 2/3.

2) **GitHub templates** (github MCP)
   - Prefer organization-approved repos or known templates:
     - `study8677/antigravity-workspace-template`
     - `kinopeee/windsurf-antigravity-rules`
   - Pull only the minimal relevant sections for the target tech.

3) **antigravity.codes rule pages** (crawl4ai; consent-gated)
   - If the user provides exact URL(s), you MAY `scrape` without extra consent.
   - If URL discovery/search is required, get explicit permission and scope first.

4) **Official docs injection** (WF.DOCS_LADDER: docfork → context7)
   - Use docfork first with a single narrow query.
   - Use context7 only if docfork is insufficient or version precision is required.

Stop condition:
- If you can produce a clean, actionable rule file for that tech, do not fetch more sources.

### Step 3: Normalize into Rule Format (token-efficient)
For each `{tech}.md`, emit:
- Scope (what this rule covers)
- Do / Don’t (5–12 bullets total)
- Minimal patterns (short examples; avoid long docs dumps)
- Verification checklist (3–7 bullets)
- “When to consult docs” pointer (WF.DOCS_LADDER / WF.GITHUB_UPSTREAM / WF.WEB_RESEARCH)

### Step 4: Combine & Save
Create / update:
- `.agent/rules/auto-generated.md` containing:
  - detected stack summary
  - active rule files list
  - priority and conflict resolution
  - “known gaps” section

## Rule Priority (Conflict Resolution)
1) User custom rules: `.agent/rules/*.md` except `auto-generated.md` (highest)
2) Auto-generated index: `.agent/rules/auto-generated.md` (medium)
3) Global rules (lowest)

## Quality Gates (Must Pass)
- Markdown parses cleanly.
- No secrets/API keys in rule content.
- No contradictions with existing custom rules.
- File size guideline: each tech rule < 30KB; auto-generated index < 50KB.
- User review required before declaring “done”.
- No silent writes: never create/modify rule files without explicit user approval.

## Security/Consent Guardrails
- Never crawl arbitrary domains without user consent.
- Treat crawling search result pages as “web search/browsing” → explicit consent required.
- Prefer user-provided URLs and same-domain limited crawls.

