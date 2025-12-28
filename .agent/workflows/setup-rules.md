---
description: Auto-detect project stack and generate workspace rules using deepcontext + docfork/context7 + github + crawl4ai (consent-gated)
---


# Workflow: Setup Project Rules (Executable)

## Consent Gate (Required for Proactive Runs)
- If the user explicitly invoked `/setup-rules` or `/update-rules`, proceed.
- If the agent is proposing these commands proactively:
  - Ask for explicit approval first.
  - Confirm scope: which tech/domain rules to generate (e.g., only Cafe24 API + Cafe24 skin).
  - Confirm whether to update existing files or create new ones.

## Commands
- `/setup-rules`
- `/update-rules`
- `/list-rules`

## Objective
Generate a clean, minimal-token rule set aligned to the CURRENT stack and this workspace’s conventions.

Outputs:
- `.agent/rules/auto-generated.md`
- `.agent/rules/{tech}.md` for detected technologies and domain modules (Cafe24, detail pages)

---

# Command: /setup-rules

## Steps

### 1) Detect technologies (deepcontext; no shell commands)
Use deepcontext to read:
- `package.json` (dependencies/devDependencies)
- lockfiles if present
- `tsconfig.json`, eslint/prettier configs
- any Cafe24-related auth/API modules (keywords: `cafe24`, `oauth`, `redirect_uri`, `scope`, `token`)
- folder signals: `/src`, `/app`, `/pages`, `/components`, `/electron`, `/renderer`

Produce `DetectedTech[]` with versions if readily available.

### 2) Map dependencies to technologies
Use a minimal mapping table (extend as needed):
- `react` → `react`
- `next` → `nextjs`
- `typescript` → `typescript`
- `tailwindcss` → `tailwind`
- `electron` → `electron`
- Cafe24 signals → `cafe24-api` and/or `cafe24-skin`
- If project has a product detail pipeline → `detail-page`

### 3) Generate rule files (call /fetch-rule per tech)
For each tech in `DetectedTech[]`:
- Run `/fetch-rule {tech}`
- Stop early if user only asked for a subset.

### 4) Create `.agent/rules/auto-generated.md` (index + priority)
Include:
- detected stack summary
- list of generated rule files
- priority/conflict rules
- “how to use”:
  - when to consult WF.DOCS_LADDER
  - when to consult WF.GITHUB_UPSTREAM
  - when to consult WF.WEB_RESEARCH (consent required)

### 5) Present summary for review
Report:
- DetectedTech
- Rules created/updated
- Notable conventions discovered
- Any gaps requiring user input
- Do not claim “active” unless the user confirms the files are placed in `.agent/rules/`.
- Ask the user to confirm the final list of files to keep, and delete/ignore any drafts.

---

# Command: /update-rules

## When to use
- Dependency upgrades
- Major framework upgrade
- User says: “update rules”

## Steps
1) Re-run detection (Step 1–2 of /setup-rules).
2) For each existing `.agent/rules/{tech}.md`:
   - Re-run `/fetch-rule {tech}` in update mode.
3) Update `.agent/rules/auto-generated.md` with new versions/deltas.

---

# Command: /list-rules

## Objective
Show what rule files exist and what they cover.

## Steps
1) Use deepcontext to enumerate `.agent/rules/`.
2) For each rule file:
   - show filename
   - show scope line (first section)
   - show last-updated note if present
3) Output a compact list.

---

## Guardrails (must enforce)
- No web crawling without explicit user permission unless the user provides exact URL(s).
- Prefer GitHub templates and doc MCPs before crawl4ai.
- Do not dump long docs; synthesize into bullets and short patterns.
- Never include secrets in rule files.

