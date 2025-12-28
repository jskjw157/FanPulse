---
description: # WF.DOCS_LADDER — Minimal Documentation Injection Purpose: Resolve external documentation needs in 1–2 doc MCP calls with minimal tokens.
---

# WF.DOCS_LADDER — Minimal Documentation Injection
Purpose: Resolve external documentation needs in 1–2 doc MCP calls with minimal tokens.

Steps:
1) Define the missing facts precisely
   - Choose one: signature/params, response schema, auth scopes, error codes, version differences, migration notes.
   - Write ONE target question including required output form:
     “Give exact signature + minimal example + 3 common pitfalls.”

2) Call #1: docfork
   - Ask narrowly. Avoid multi-topic prompts.

3) Stop condition (mandatory)
   Stop if you have:
   - exact method/endpoint
   - required params/fields
   - minimal working example
   - at least one implementation-relevant pitfall

4) Call #2 (only if needed): context7
   - Ask ONLY for the missing item found in step 3.

5) Output synthesis (no doc dumps)
   - One-line decision
   - Checklist (5–10 bullets)
   - Minimal snippet
   - Pitfalls (3–5 bullets)
   - Verification plan