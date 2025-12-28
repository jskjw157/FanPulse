---
description: Fetch/synthesize a specific technology rule using the current MCP stack (deepcontext/github/docfork/context7/crawl4ai)
---

# Workflow: Fetch Rule (Executable)

## Command
`/fetch-rule {technology}`

## Objective
Create or refresh `.agent/rules/{technology}.md` with minimal tokens, using the source ladder:
existing → GitHub templates → antigravity.codes (scrape) → official docs (docfork→context7).

## Consent Gate (Required for Proactive Runs)
- If the user explicitly invoked `/fetch-rule`, proceed.
- If the agent is proposing `/fetch-rule` proactively:
  - Ask for explicit approval before fetching/synthesizing.
  - Confirm the target file name and scope (what will be enforced).

## Inputs
- technology: one of `react`, `typescript`, `nextjs`, `tailwind`, `electron`, `python`, `fastapi`, `django`,
  plus domain rules like `cafe24-api`, `cafe24-skin`, `detail-page`.

## Steps

### 1) Normalize the technology name
- Lowercase.
- Map aliases:
  - `next` → `nextjs`
  - `ts` → `typescript`
  - `cafe24` → ask user whether they mean `cafe24-api` or `cafe24-skin` (default: `cafe24-api` if request is OAuth/API)

### 2) Check existing rule first (deepcontext)
- If `.agent/rules/{technology}.md` exists:
  - If the user did not request an update: reuse and summarize.
  - If update requested: proceed to Step 3.

### 3) Fetch minimal source content (stop early)

#### 3A) GitHub templates (github MCP) — preferred
- Search within template repos for the tech keyword.
- Extract only the relevant sections (avoid full file dumps).

Stop if the extracted content is sufficient to build:
- Do/Don’t bullets
- Minimal patterns
- Verification checklist

#### 3B) antigravity.codes rule page (crawl4ai; consent-gated)
- If the user provided the exact URL: `scrape` that URL.
- If URL discovery is required (e.g., “find rules for X”):
  - Ask for explicit permission + allowed domains/seed URLs + page budget before crawling.
- Extract only the core rule content (remove nav/footer).

Stop if sufficient.

#### 3C) Official docs injection (WF.DOCS_LADDER)
- docfork: one narrow question to extract best practices that translate into rules.
- context7 only if docfork is insufficient or version-precision is required.

### 4) Synthesize the rule file (standard format)
Write `.agent/rules/{technology}.md` with this structure:

1) Title + Scope
2) Do (bullets)
3) Don’t (bullets)
4) Minimal patterns (short snippets)
5) Verification checklist
6) Escalation ladder:
   - internal-first (deepcontext)
   - docs ladder (docfork → context7)
   - upstream (github)
   - web research (crawl4ai, consent-gated)

### 5) Save & report
- Save/update `.agent/rules/{technology}.md`.
- Report:
  - sources used (GitHub/template vs scrape vs doc MCP)
  - what changed (delta)
  - any remaining gaps
- Ask for review if this is a new rule file.

## Error Handling
- If GitHub templates and antigravity.codes both fail:
  - Use docfork/context7 to produce a minimal “best-practice rule” from official docs.
- If all sources fail:
  - Produce a stub rule file with `[NEED: ...]` placeholders and ask the user for a URL or allowed domain scope.

